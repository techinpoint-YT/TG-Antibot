package org.spigot.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigot.TGAntiBotPlugin;
import org.spigot.core.data.PlayerProfile;

/**
 * PlaceholderAPI integration for TG-AntiBot
 * Provides placeholders for other plugins to use
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final TGAntiBotPlugin plugin;

    public PlaceholderAPIHook(TGAntiBotPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "tgantibot";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Techinpoint Gamerz";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        String ip = player.getAddress().getAddress().getHostAddress();
        PlayerProfile profile = plugin.getThreatDetectionEngine().getPlayerProfiles().get(ip);

        switch (params.toLowerCase()) {
            case "protection_mode":
                return plugin.getThreatDetectionEngine().getCurrentMode().getName();
                
            case "joins_per_second":
                return String.valueOf(plugin.getThreatDetectionEngine().getJoinsPerSecond());
                
            case "pings_per_second":
                return String.valueOf(plugin.getThreatDetectionEngine().getPingsPerSecond());
                
            case "is_whitelisted":
                return plugin.getThreatDetectionEngine().getWhitelist().contains(ip) ? "true" : "false";
                
            case "is_blacklisted":
                return plugin.getThreatDetectionEngine().getBlacklist().contains(ip) ? "true" : "false";
                
            case "suspicion_score":
                return profile != null ? String.valueOf(profile.getSuspicionScore()) : "0";
                
            case "connection_count":
                return profile != null ? String.valueOf(profile.getConnectionCount()) : "0";
                
            case "total_attacks_blocked":
                return String.valueOf(plugin.getThreatAnalytics().getTotalAttacksBlocked());
                
            case "block_rate":
                return String.format("%.2f", plugin.getThreatAnalytics().getBlockRate());
                
            case "is_under_attack":
                return plugin.getThreatDetectionEngine().getCurrentAttack().isActive() ? "true" : "false";
                
            case "attack_type":
                if (plugin.getThreatDetectionEngine().getCurrentAttack().isActive()) {
                    return plugin.getThreatDetectionEngine().getCurrentAttack().getCurrentAttackType().getName();
                }
                return "None";
                
            default:
                return null;
        }
    }
}