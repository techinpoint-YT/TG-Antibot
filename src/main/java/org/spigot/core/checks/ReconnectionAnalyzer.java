package org.spigot.core.checks;

import org.spigot.TGAntiBotPlugin;
import org.spigot.core.data.PlayerProfile;

public class ReconnectionAnalyzer {
    private final TGAntiBotPlugin plugin;

    public ReconnectionAnalyzer(TGAntiBotPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean shouldBlock(PlayerProfile profile, long currentTime) {
        if (!plugin.getConfigManager().isReconnectCheckEnabled()) {
            return false;
        }

        // Check for rapid reconnections
        if (profile.hasRapidReconnections()) {
            plugin.getLogger().info("§cBlocked IP " + profile.getIp() + " - Rapid reconnections detected");
            return true;
        }

        // Check minimum time between connections
        long timeSinceLastConnection = currentTime - profile.getLastConnection();
        long minReconnectTime = plugin.getConfigManager().getMinReconnectTime();

        if (timeSinceLastConnection < minReconnectTime) {
            plugin.getLogger().info("§cBlocked IP " + profile.getIp() + " - Reconnected too quickly: " + timeSinceLastConnection + "ms");
            return true;
        }

        return false;
    }
}