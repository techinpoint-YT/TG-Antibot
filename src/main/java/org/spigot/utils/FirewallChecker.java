package org.spigot.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigot.Main;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FirewallChecker {

    private final Main plugin;
    private final Set<String> dynamicBlockedIPs;

    public FirewallChecker(Main plugin) {
        this.plugin = plugin;
        this.dynamicBlockedIPs = new HashSet<>();
    }

    public boolean isBlocked(Player player) {
        if (!plugin.getConfigManager().isFirewallEnabled()) return false;

        InetAddress address = player.getAddress().getAddress();
        String ip = normalizeIP(address.getHostAddress());

        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[FirewallChecker] Checking IP: " + ip);
        }

        // Whitelist IPs
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (whitelistIps.contains(ip)) return false;

        // Static blocked IPs from config
        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(ip) || dynamicBlockedIPs.contains(ip)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[FirewallChecker] Blocked connection from IP: " + ip);
            }
            player.kickPlayer(plugin.getMessages().get(
                    "ip-blocked",
                    "&cYour IP has been blocked from this server.\n&7If you believe this is a mistake, please contact staff."
            ));
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
    }

    public void unblockIP(String ip) {
        ip = normalizeIP(ip);
        dynamicBlockedIPs.remove(ip);

        List<String> configBlockedIps = normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips"));
        if (configBlockedIps.contains(ip)) {
            configBlockedIps.remove(ip);
            plugin.getConfig().set("firewall.blocked-ips", configBlockedIps);
            plugin.saveConfig();
        }
    }

    public void whitelistIP(String ip) {
        ip = normalizeIP(ip);
        List<String> whitelistIps = normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips"));
        if (!whitelistIps.contains(ip)) {
            whitelistIps.add(ip);
            plugin.getConfig().set("firewall.whitelist-ips", whitelistIps);
            plugin.saveConfig();
        }
    }

    public void blockPlayer(String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getAddress() != null) {
            String ip = normalizeIP(target.getAddress().getAddress().getHostAddress());
            blockIP(ip);
        }
    }

    public void whitelistPlayer(String playerName) {
        Player target = Bukkit.getPlayerExact(playerName);
        if (target != null && target.getAddress() != null) {
            String ip = normalizeIP(target.getAddress().getAddress().getHostAddress());
            whitelistIP(ip);
        }
    }

    public boolean isBlocked(String ip) {
        ip = normalizeIP(ip);
        if (normalizeList(plugin.getConfig().getStringList("firewall.whitelist-ips")).contains(ip)) return false;
        if (normalizeList(plugin.getConfig().getStringList("firewall.blocked-ips")).contains(ip)) return true;
        return dynamicBlockedIPs.contains(ip);
    }

    public Set<String> getBlockedIPs() {
        return new HashSet<>(dynamicBlockedIPs);
    }

    // --- Helper methods ---
    private String normalizeIP(String ip) {
        return ip.trim().toLowerCase();
    }

    private List<String> normalizeList(List<String> list) {
        return list.stream().map(this::normalizeIP).toList();
    }
}
