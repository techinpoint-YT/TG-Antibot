package org.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigot.Main;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FirewallManager {

    private final Main plugin;
    private final ConcurrentMap<String, Long> dynamicBlockedIPs;
    private final ConcurrentMap<String, Long> tempBlockedIPs;
    private final ScheduledExecutorService cleanupExecutor;
    private volatile boolean isShutdown = false;

    public FirewallManager(Main plugin) {
        this.plugin = plugin;
        this.dynamicBlockedIPs = new ConcurrentHashMap<>();
        this.tempBlockedIPs = new ConcurrentHashMap<>();
        
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TGA-Firewall-Cleanup");
            t.setDaemon(true);
            return t;
        });
        
        startCleanupTask();
    }

    public boolean isBlocked(Player player) {
        if (!plugin.getConfigManager().isFirewallEnabled() || player == null) {
            return false;
        }

        // Null-safety check for player address
        if (player.getAddress() == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has null address");
            return false;
        }

        InetAddress address = player.getAddress().getAddress();
        if (address == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has null InetAddress");
            return false;
        }
        
        String ip = normalizeIP(address.getHostAddress());

        return isBlocked(ip);
    }

    public boolean isBlocked(String ip) {
        if (!plugin.getConfigManager().isFirewallEnabled() || ip == null) {
            return false;
        }

        ip = normalizeIP(ip);
        
        if (ip.isEmpty()) {
            return false;
        }

        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[FirewallManager] Checking IP: " + ip);
        }

        // Check whitelist first
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (whitelistIps.contains(ip)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[FirewallManager] IP is whitelisted: " + ip);
            }
            return false;
        }

        // Check static blocked IPs from config
        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(ip) || dynamicBlockedIPs.containsKey(ip) || tempBlockedIPs.containsKey(ip)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[FirewallManager] Blocked connection from IP: " + ip);
            }
            return true;
        }

        return false;
    }

    public void blockIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to block null or empty IP");
            return;
        }
        
        final String normalizedIP = normalizeIP(ip);
        if (normalizedIP.isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to block invalid IP: " + ip);
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        dynamicBlockedIPs.put(normalizedIP, currentTime);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (!configBlockedIps.contains(normalizedIP)) {
            configBlockedIps.add(normalizedIP);
            
            try {
                plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
                plugin.saveConfig();
            } catch (Exception e) {
                plugin.getLogger().severe("[FirewallManager] Failed to save blocked IP to config: " + e.getMessage());
            }
        }

        plugin.getLogger().info("§cBlocked IP: " + normalizedIP);
    }

    public void blockIPTemporary(String ip, long durationSeconds) {
        if (ip == null || ip.trim().isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to temp block null or empty IP");
            return;
        }
        
        if (durationSeconds <= 0) {
            plugin.getLogger().warning("[FirewallManager] Invalid duration for temp block: " + durationSeconds);
            return;
        }
        
        final String normalizedIP = normalizeIP(ip);
        if (normalizedIP.isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to temp block invalid IP: " + ip);
            return;
        }
        
        long expirationTime = System.currentTimeMillis() + (durationSeconds * 1000);
        tempBlockedIPs.put(normalizedIP, expirationTime);

        plugin.getLogger().info("§eTemporarily blocked IP: " + normalizedIP + " for " + durationSeconds + " seconds");

        // The cleanup task will handle removal
    }

    public void unblockIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return;
        }
        
        final String normalizedIP = normalizeIP(ip);
        if (normalizedIP.isEmpty()) {
            return;
        }
        
        dynamicBlockedIPs.remove(normalizedIP);
        tempBlockedIPs.remove(normalizedIP);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(normalizedIP)) {
            configBlockedIps.remove(normalizedIP);
            
            try {
                plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
                plugin.saveConfig();
            } catch (Exception e) {
                plugin.getLogger().severe("[FirewallManager] Failed to save unblocked IP to config: " + e.getMessage());
            }
        }

        plugin.getLogger().info("§aUnblocked IP: " + normalizedIP);
    }

    public void whitelistIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to whitelist null or empty IP");
            return;
        }
        
        final String normalizedIP = normalizeIP(ip);
        if (normalizedIP.isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to whitelist invalid IP: " + ip);
            return;
        }
        
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (!whitelistIps.contains(normalizedIP)) {
            whitelistIps.add(normalizedIP);
            
            try {
                plugin.getConfig().set("firewall.whitelist-ips", whitelistIps);
                plugin.saveConfig();
            } catch (Exception e) {
                plugin.getLogger().severe("[FirewallManager] Failed to save whitelisted IP to config: " + e.getMessage());
            }
        }

        // Remove from blocked lists
        unblockIP(normalizedIP);

        plugin.getLogger().info("§aWhitelisted IP: " + normalizedIP);
    }

    public void blockPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to block player with null or empty name");
            return;
        }
        
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getAddress() != null) {
            String ip = normalizeIP(target.getAddress().getAddress().getHostAddress());
            blockIP(ip);
            
            String kickMessage = plugin.getMessages().get("ip-blocked",
                "§cYour IP has been blocked from this server.");
            target.kickPlayer(kickMessage);
        } else {
            plugin.getLogger().warning("[FirewallManager] Could not block player " + playerName + " - not found or no address");
        }
    }

    public void whitelistPlayer(String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            plugin.getLogger().warning("[FirewallManager] Attempted to whitelist player with null or empty name");
            return;
        }
        
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getAddress() != null) {
            String ip = normalizeIP(target.getAddress().getAddress().getHostAddress());
            whitelistIP(ip);
        } else {
            plugin.getLogger().warning("[FirewallManager] Could not whitelist player " + playerName + " - not found or no address");
        }
    }

    public Set<String> getBlockedIPs() {
        Set<String> allBlocked = new HashSet<>(dynamicBlockedIPs.keySet());
        allBlocked.addAll(tempBlockedIPs.keySet());
        allBlocked.addAll(normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips")));
        return allBlocked;
    }

    public Set<String> getWhitelistedIPs() {
        return new HashSet<>(normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips")));
    }
    
    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            if (isShutdown) return;
            
            long currentTime = System.currentTimeMillis();
            
            // Clean up expired temporary blocks
            tempBlockedIPs.entrySet().removeIf(entry -> {
                if (entry.getValue() < currentTime) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("§aTemporary block expired for IP: " + entry.getKey());
                    }
                    return true;
                }
                return false;
            });
            
            // Clean up old dynamic blocks (older than auto-block duration)
            long autoBlockDuration = plugin.getConfigManager().getAutoBlockDuration() * 1000L;
            dynamicBlockedIPs.entrySet().removeIf(entry -> {
                if (currentTime - entry.getValue() > autoBlockDuration) {
                    if (plugin.getConfigManager().isDebugMode()) {
                        plugin.getLogger().info("§aAuto-unblocked expired IP: " + entry.getKey());
                    }
                    return true;
                }
                return false;
            });
            
        }, 60, 60, TimeUnit.SECONDS); // Run every minute
    }
    
    public void shutdown() {
        isShutdown = true;
        
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        plugin.getLogger().info("[FirewallManager] Shutdown completed");
    }

    // Helper methods
    private String normalizeIP(String ip) {
        if (ip == null) return "";
        return ip.replace("/", "").trim().toLowerCase();
    }

    private List<String> normalizeList(List<String> list) {
        if (list == null) return new ArrayList<>();
        return list.stream()
                .filter(ip -> ip != null && !ip.trim().isEmpty())
                .map(this::normalizeIP)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    public String getStats() {
        return String.format("Dynamic blocks: %d, Temp blocks: %d, Config blocks: %d, Whitelist: %d",
            dynamicBlockedIPs.size(),
            tempBlockedIPs.size(),
            plugin.getConfig().getStringList("firewall.blocked-ips").size(),
            plugin.getConfig().getStringList("firewall.whitelist-ips").size());
    }
}