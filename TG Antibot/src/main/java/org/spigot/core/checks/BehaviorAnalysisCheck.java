package org.spigot.core.checks;

import org.spigot.Main;
import org.spigot.core.data.PlayerProfile;

public class BehaviorAnalysisCheck {
    private final Main plugin;

    public BehaviorAnalysisCheck(Main plugin) {
        this.plugin = plugin;
    }

    public boolean shouldBlock(PlayerProfile profile) {
        if (!plugin.getConfigManager().isBehaviorAnalysisEnabled()) {
            return false;
        }

        int suspicionScore = calculateSuspicionScore(profile);
        int threshold = plugin.getConfigManager().getSuspicionThreshold();

        if (suspicionScore >= threshold) {
            plugin.getLogger().info("§cBlocked IP " + profile.getIp() + " - High suspicion score: " + suspicionScore);
            return true;
        }

        return false;
    }

    private int calculateSuspicionScore(PlayerProfile profile) {
        int score = 0;

        // Multiple nicknames from same IP
        if (profile.getNicknames().size() > 3) {
            score += 20;
        }

        // Suspicious nickname patterns
        if (profile.hasSuspiciousNicknamePattern()) {
            score += 30;
        }

        // Rapid connections
        if (profile.hasRapidReconnections()) {
            score += 25;
        }

        // Short play sessions (if available)
        if (profile.getPlayTime() < 60000 && profile.getConnectionCount() > 5) { // Less than 1 minute playtime but many connections
            score += 15;
        }

        // Regular connection intervals (bot-like)
        long avgInterval = profile.getAverageConnectionInterval();
        if (avgInterval > 0 && avgInterval < 5000) { // Very regular, short intervals
            score += 20;
        }

        return score;
    }
}