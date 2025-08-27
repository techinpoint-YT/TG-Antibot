package org.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.spigot.Main;

import java.util.UUID;

public class JoinListener implements Listener {

    private final Main plugin;

    public JoinListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        String name = event.getName();
        String ip = event.getAddress().getHostAddress();

        // 1️⃣ Firewall IP check first
        if (plugin.getFirewallChecker().isBlocked(ip)) {
            event.disallow(Result.KICK_OTHER, plugin.getMessages().get(
                    "ip-blocked",
                    "&cYour IP has been blocked from this server.\n&7If you believe this is a mistake, please contact staff."
            ).replace("{player}", name).replace("{ip}", ip));
            return; // Don't run further checks
        }

        // 2️⃣ Run your normal AntiBot / cooldown protection
        boolean blocked = plugin.getProtectionManager().shouldBlock(
                uuid,
                event.getAddress(),
                name
        );

        if (blocked) {
            event.disallow(Result.KICK_OTHER, plugin.getMessages().get("join-blocked-line1") + "\n"
                    + plugin.getMessages().get("join-blocked-line2"));
        }
    }
}
