package org.spigot.core.checks;

import org.spigot.Main;
import org.spigot.core.data.PlayerProfile;

public class ConnectionSpeedCheck {
    private final Main plugin;

    public ConnectionSpeedCheck(Main plugin) {
        this.plugin = plugin;
    }

    public boolean shouldBlock(PlayerProfile profile, long currentTime) {
        if (!plugin.getConfigManager().isConnectionSpeedCheckEnabled()) {
            return false;
        }

        // Check for rapid connections
        int connectionsInLastMinute = profile.getConnectionsInTimeframe(60000);
        if (connectionsInLastMinute > plugin.getConfigManager().getMaxConnectionsPerMinute()) {
            plugin.getLogger().info("§cBlocked IP " + profile.getIp() + " - Too many connections: " + connectionsInLastMinute);
            return true;
        }

        // Check connection interval
        long averageInterval = profile.getAverageConnectionInterval();
        if (averageInterval > 0 && averageInterval < plugin.getConfigManager().getMinConnectionInterval()) {
            plugin.getLogger().info("§cBlocked IP " + profile.getIp() + " - Connection interval too short: " + averageInterval + "ms");
            return true;
        }

        return false;
    }
}