package org.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigot.Main;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FirewallManager {

    private final Main plugin;
    private final Set<String> dynamicBlockedIPs;
    private final Set<String> tempBlockedIPs;

    public FirewallManager(Main plugin) {
        this.plugin = plugin;
        this.dynamicBlockedIPs = ConcurrentHashMap.newKeySet();
        this.tempBlockedIPs = ConcurrentHashMap.newKeySet();
    }

    public boolean isBlocked(Player player) {
        if (!plugin.getConfigManager().isFirewallEnabled()) return false;

        InetAddress address = player.getAddress().getAddress();
        String ip = normalizeIP(address.getHostAddress());

        return isBlocked(ip);
    }

    public boolean isBlocked(String ip) {
        if (!plugin.getConfigManager().isFirewallEnabled()) return false;
        
        ip = normalizeIP(ip);

        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[FirewallManager] Checking IP: " + ip);
        }

        // Check whitelist first
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (whitelistIps.contains(ip)) return false;

        // Check static blocked IPs from config
        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(ip) || dynamicBlockedIPs.contains(ip) || tempBlockedIPs.contains(ip)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[FirewallManager] Blocked connection from IP: " + ip);
            }
            return true;
        }

        return false;
    }

    public void blockIP(String ip) {
        ip = normalizeIP(ip);
        dynamicBlockedIPs.add(ip);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (!configBlockedIps.contains(ip)) {
            configBlockedIps.add(ip);
            plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
            plugin.saveConfig();
        }

        plugin.getLogger().info("§cBlocked IP: " + ip);
    }

    public void blockIPTemporary(String ip, long durationSeconds) {
        ip = normalizeIP(ip);
        tempBlockedIPs.add(ip);
        
        plugin.getLogger().info("§eTemporarily blocked IP: " + ip + " for " + durationSeconds + " seconds");
        
        // Remove after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tempBlockedIPs.remove(ip);
            plugin.getLogger().info("§aTemporary block expired for IP: " + ip);
        }, durationSeconds * 20L);
    }

    public void unblockIP(String ip) {
        ip = normalizeIP(ip);
        dynamicBlockedIPs.remove(ip);
        tempBlockedIPs.remove(ip);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(ip)) {
            configBlockedIps.remove(ip);
            plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
            plugin.saveConfig();
        }

        plugin.getLogger().info("§aUnblocked IP: " + ip);
    }

    public void whitelistIP(String ip) {
        ip = normalizeIP(ip);
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (!whitelistIps.contains(ip)) {
            whitelistIps.add(ip);
            plugin.getConfig().set("firewall.whitelist-ips", whitelistIps);
            plugin.saveConfig();
        }

        // Remove from blocked lists
        unblockIP(ip);
        
        plugin.getLogger().info("§aWhitelisted IP: " + ip);
    }

    public void blockPlayer(String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getAddress() != null) {
            String ip = normalizeIP(target.getAddress().getAddress().getHostAddress());
            blockIP(ip);
            target.kickPlayer(plugin.getMessages().get("ip-blocked", 
                "§cYour IP has been blocked from this server."));
        }
    }

    public void whitelistPlayer(String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getAddress() != null) {
            String ip = normalizeIP(target.getAddress().getAddress().getHostAddress());
            whitelistIP(ip);
        }
    }

    public Set<String> getBlockedIPs() {
        Set<String> allBlocked = new HashSet<>(dynamicBlockedIPs);
        allBlocked.addAll(tempBlockedIPs);
        allBlocked.addAll(normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips")));
        return allBlocked;
    }

    public Set<String> getWhitelistedIPs() {
        return new HashSet<>(normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips")));
    }

    // Helper methods
    private String normalizeIP(String ip) {
        return ip.trim().toLowerCase();
    }

    private List<String> normalizeList(List<String> list) {
        return list.stream().map(this::normalizeIP).toList();
    }
}