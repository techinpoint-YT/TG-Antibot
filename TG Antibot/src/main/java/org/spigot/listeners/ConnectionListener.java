package org.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.spigot.Main;
import org.spigot.enums.AttackType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ConnectionListener implements Listener {

    private final Main plugin;

    public ConnectionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != Result.ALLOWED) {
            return; // Already denied by another plugin
        }
        
        UUID uuid = event.getUniqueId();
        String name = event.getName();
        String ip = event.getAddress().getHostAddress();
        
        // Input validation
        if (name == null || name.trim().isEmpty()) {
            event.disallow(Result.KICK_OTHER, plugin.getMessages().get("invalid-username", "&cInvalid username"));
            return;
        }
        
        if (ip == null || ip.trim().isEmpty()) {
            event.disallow(Result.KICK_OTHER, plugin.getMessages().get("invalid-connection", "&cInvalid connection"));
            return;
        }
        
        // Sanitize inputs
        name = name.trim();
        ip = ip.trim();

        // Record connection analysis
        plugin.getAttackAnalyzer().recordConnectionAnalysis();

        // Check firewall first
        if (plugin.getFirewallManager().isBlocked(ip)) {
            String message = plugin.getMessages().get(
                "ip-blocked",
                "&cYour IP has been blocked from this server.\n&7If you believe this is a mistake, please contact staff.",
                name, ip, null, null, null
            );
            event.disallow(Result.KICK_OTHER, message);
            plugin.getAttackAnalyzer().recordAttack(AttackType.JOIN_FLOOD, ip, 1);
            return;
        }

        // Check VPN if enabled (async to avoid blocking)
        if (plugin.getConfigManager().isAsyncValidation()) {
            CompletableFuture.supplyAsync(() -> plugin.getVPNChecker().isUsingVPN(ip))
                .orTimeout(plugin.getConfigManager().getRequestTimeout(), TimeUnit.MILLISECONDS)
                .whenComplete((isVPN, throwable) -> {
                    if (throwable != null) {
                        if (plugin.getConfigManager().isDebugMode()) {
                            plugin.getLogger().warning("VPN check failed for " + ip + ": " + throwable.getMessage());
                        }
                        return;
                    }
                    
                    if (isVPN && event.getLoginResult() == Result.ALLOWED) {
                        String message = plugin.getMessages().get(
                            "vpn-kick-message",
                            "&cVPN/Proxy connections are not allowed on this server!"
                        );
                        event.disallow(Result.KICK_OTHER, message);
                        plugin.getAttackAnalyzer().recordAttack(AttackType.BEHAVIOR_ANOMALY, ip, 1);
                    }
                });
        } else {
            // Synchronous VPN check
            if (plugin.getVPNChecker().isUsingVPN(ip)) {
                String message = plugin.getMessages().get(
                    "vpn-kick-message",
                    "&cVPN/Proxy connections are not allowed on this server!"
                );
                event.disallow(Result.KICK_OTHER, message);
                plugin.getAttackAnalyzer().recordAttack(AttackType.BEHAVIOR_ANOMALY, ip, 1);
                return;
            }
        }

        // Run bot protection checks
        boolean blocked = plugin.getBotProtectionManager().shouldBlockConnection(
                uuid,
                event.getAddress(),
                name
        );

        if (blocked) {
            String kickMessage = plugin.getMessages().get("join-blocked-line1", "&cYou cannot join the server right now.") + 
                "\n" + plugin.getMessages().get("join-blocked-line2", "&7Please wait a few minutes before trying again.");
            
            event.disallow(Result.KICK_OTHER, kickMessage);
            
            // Determine attack type based on current protection mode
            AttackType attackType = determineAttackType();
            plugin.getAttackAnalyzer().recordAttack(attackType, ip, 1);
            
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("Blocked connection from " + name + " (" + ip + ") - Attack type: " + attackType);
            }
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