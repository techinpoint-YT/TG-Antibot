package org.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.spigot.Main;
import org.spigot.enums.AttackType;

public class ServerPingListener implements Listener {

    private final Main plugin;

    public ServerPingListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        String ip = event.getAddress().getHostAddress();
        
        // Handle server ping in bot protection manager
        plugin.getBotProtectionManager().handleServerPing(event.getAddress());
        
        // Check if IP is blocked
        if (plugin.getFirewallManager().isBlocked(ip)) {
            // Don't reveal server info to blocked IPs
            event.setMotd("§cAccess Denied");
            event.setMaxPlayers(0);
            plugin.getAttackAnalyzer().recordAttack(AttackType.PING_FLOOD, ip, 1);
            return;
        }
        
        // Customize MOTD based on protection mode
        switch (plugin.getBotProtectionManager().getCurrentMode()) {
            case LOCKDOWN:
                event.setMotd("§c§lSERVER UNDER PROTECTION\n§7Please wait before connecting");
                break;
            case STRICT:
                event.setMotd("§e§lENHANCED SECURITY MODE\n§7Connection verification active");
                break;
            default:
                // Keep original MOTD for normal mode
                break;
        }
    }
}