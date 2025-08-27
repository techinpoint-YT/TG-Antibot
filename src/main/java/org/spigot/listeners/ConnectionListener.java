package org.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.spigot.Main;
import org.spigot.enums.AttackType;

import java.util.UUID;

public class ConnectionListener implements Listener {

    private final Main plugin;

    public ConnectionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String name = event.getName();
        String ip = event.getAddress().getHostAddress();

        // Record connection analysis
        plugin.getAttackAnalyzer().recordConnectionAnalysis();

        // Check firewall first
        if (plugin.getFirewallManager().isBlocked(ip)) {
            event.disallow(Result.KICK_OTHER, plugin.getMessages().get(
                    "ip-blocked",
                    "&cYour IP has been blocked from this server.\n&7If you believe this is a mistake, please contact staff."
            ).replace("{player}", name).replace("{ip}", ip));
            plugin.getAttackAnalyzer().recordAttack(AttackType.JOIN_FLOOD, ip, 1);
            return;
        }

        // Check VPN if enabled
        if (plugin.getVPNChecker().isUsingVPN(ip)) {
            event.disallow(Result.KICK_OTHER, plugin.getMessages().get(
                    "vpn-kick-message",
                    "&cVPN/Proxy connections are not allowed on this server!"
            ));
            plugin.getAttackAnalyzer().recordAttack(AttackType.BEHAVIOR_ANOMALY, ip, 1);
            return;
        }

        // Run bot protection checks
        boolean blocked = plugin.getBotProtectionManager().shouldBlockConnection(
                uuid,
                event.getAddress(),
                name
        );

        if (blocked) {
            String kickMessage = plugin.getMessages().get("join-blocked-line1", "&cYou cannot join the server right now.") + "\n" +
                    plugin.getMessages().get("join-blocked-line2", "&7Please wait a few minutes before trying again.");
            
            event.disallow(Result.KICK_OTHER, kickMessage);
            
            // Determine attack type based on current protection mode
            AttackType attackType = determineAttackType();
            plugin.getAttackAnalyzer().recordAttack(attackType, ip, 1);
        }
    }

    private AttackType determineAttackType() {
        switch (plugin.getBotProtectionManager().getCurrentMode()) {
            case LOCKDOWN:
                return AttackType.JOIN_FLOOD;
            case STRICT:
                return AttackType.RECONNECT_SPAM;
            default:
                return AttackType.BEHAVIOR_ANOMALY;
        }
    }
}