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
import java.util.stream.Collectors;

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

        // Null-safety check for player address
        if (player.getAddress() == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has null address");
            return false;
        }

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
        final String normalizedIP = normalizeIP(ip); // Make effectively final for lambda
        dynamicBlockedIPs.add(normalizedIP);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (!configBlockedIps.contains(normalizedIP)) {
            configBlockedIps.add(normalizedIP);
            plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
            plugin.saveConfig();
        }

        plugin.getLogger().info("§cBlocked IP: " + normalizedIP);
    }

    public void blockIPTemporary(String ip, long durationSeconds) {
        final String normalizedIP = normalizeIP(ip); // Make effectively final for lambda
        tempBlockedIPs.add(normalizedIP);

        plugin.getLogger().info("§eTemporarily blocked IP: " + normalizedIP + " for " + durationSeconds + " seconds");

        // Remove after duration
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tempBlockedIPs.remove(normalizedIP);
            plugin.getLogger().info("§aTemporary block expired for IP: " + normalizedIP);
        }, durationSeconds * 20L);
    }

    public void unblockIP(String ip) {
        final String normalizedIP = normalizeIP(ip); // Make effectively final for lambda
        dynamicBlockedIPs.remove(normalizedIP);
        tempBlockedIPs.remove(normalizedIP);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(normalizedIP)) {
            configBlockedIps.remove(normalizedIP);
            plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
            plugin.saveConfig();
        }

        plugin.getLogger().info("§aUnblocked IP: " + normalizedIP);
    }

    public void whitelistIP(String ip) {
        final String normalizedIP = normalizeIP(ip); // Make effectively final for lambda
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (!whitelistIps.contains(normalizedIP)) {
            whitelistIps.add(normalizedIP);
            plugin.getConfig().set("firewall.whitelist-ips", whitelistIps);
            plugin.saveConfig();
        }

        // Remove from blocked lists
        unblockIP(normalizedIP);

        plugin.getLogger().info("§aWhitelisted IP: " + normalizedIP);
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
        // Replace .toList() with collect(Collectors.toList()) for Java 8+ compatibility
        return list.stream()
                .map(this::normalizeIP)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}