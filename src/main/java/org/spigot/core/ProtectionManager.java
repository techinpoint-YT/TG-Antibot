package org.spigot.core;

import org.bukkit.entity.Player;
import org.spigot.Main;

import java.net.InetAddress;
import java.util.*;

public class ProtectionManager {

    private final Main plugin;

    // Per-player cooldown tracker
    private final Map<UUID, Long> joinTimestamps = new HashMap<>();

    // Global join tracker
    private final Deque<Long> globalJoins = new ArrayDeque<>();

    public ProtectionManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Compatibility method — allows older code using Player objects.
     */
    public boolean shouldBlock(Player player) {
        if (player == null || player.getAddress() == null) return false;
        return shouldBlock(player.getUniqueId(), player.getAddress().getAddress(), player.getName());
    }

    /**
     * Main check method for AsyncPlayerPreLoginEvent and raw IP checks.
     */
    public boolean shouldBlock(UUID uuid, InetAddress ip, String name) {
        long now = System.currentTimeMillis();

        // --- Per-player cooldown ---
        long cooldown = plugin.getConfig().getLong("cooldowns.join-delay-ms", 3000);
        if (joinTimestamps.containsKey(uuid)) {
            long lastJoin = joinTimestamps.get(uuid);
            if (now - lastJoin < cooldown) {
                plugin.getLogger().info("[TG-AntiBot] Player " + name +
                        " blocked due to per-player join cooldown.");
                return true;
            }
        }

        // --- Global join limit ---
        int maxJoins = plugin.getConfig().getInt("join-limit.max-joins", 5);
        int intervalSec = plugin.getConfig().getInt("join-limit.interval-seconds", 1);
        long intervalMillis = intervalSec * 1000L;

        while (!globalJoins.isEmpty() && now - globalJoins.peekFirst() > intervalMillis) {
            globalJoins.pollFirst();
        }

        if (globalJoins.size() >= maxJoins) {
            plugin.getLogger().info("[TG-AntiBot] Player " + name +
                    " blocked due to global join limit (" + maxJoins + " joins in " + intervalSec + "s).");
            return true;
        }

        // --- VPN check ---
        String ipStr = ip.getHostAddress();
        if (plugin.getVPNChecker().isUsingVPN(ipStr)) {
            plugin.getLogger().info("[TG-AntiBot] Player " + name +
                    " blocked due to VPN detection.");
            return true;
        }

        // --- Firewall check ---
        if (plugin.getFirewallChecker().isBlocked(ipStr)) {
            plugin.getLogger().info("[TG-AntiBot] Player " + name +
                    " blocked due to firewall rules.");
            return true;
        }

        // Passed all checks → record join
        joinTimestamps.put(uuid, now);
        globalJoins.addLast(now);

        return false; // Player allowed
    }
}
