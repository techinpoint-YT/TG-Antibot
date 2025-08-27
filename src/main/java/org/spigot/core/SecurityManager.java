package org.spigot.core;

import org.bukkit.entity.Player;
import org.spigot.Main;
import org.spigot.core.data.SecurityProfile;
import org.spigot.enums.SecurityLevel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SecurityManager {

    private final Main plugin;
    private final Map<String, SecurityProfile> securityProfiles;

    public SecurityManager(Main plugin) {
        this.plugin = plugin;
        this.securityProfiles = new ConcurrentHashMap<>();
    }

    public SecurityLevel evaluatePlayer(Player player) {
        String ip = player.getAddress().getAddress().getHostAddress();
        SecurityProfile profile = securityProfiles.computeIfAbsent(ip, k -> new SecurityProfile(ip));
        
        int riskScore = calculateRiskScore(player, profile);
        profile.updateRiskScore(riskScore);
        
        if (riskScore >= plugin.getConfigManager().getHighRiskThreshold()) {
            return SecurityLevel.HIGH_RISK;
        } else if (riskScore >= plugin.getConfigManager().getMediumRiskThreshold()) {
            return SecurityLevel.MEDIUM_RISK;
        } else {
            return SecurityLevel.LOW_RISK;
        }
    }

    private int calculateRiskScore(Player player, SecurityProfile profile) {
        int score = 0;
        
        // VPN/Proxy check
        if (plugin.getVPNChecker().isUsingVPN(player)) {
            score += 30;
        }
        
        // New player check
        if (!player.hasPlayedBefore()) {
            score += 15;
        }
        
        // Connection frequency
        if (profile.getConnectionsInLastHour() > 5) {
            score += 20;
        }
        
        // Suspicious timing patterns
        if (profile.hasSuspiciousTimingPattern()) {
            score += 25;
        }
        
        // Geographic anomalies
        if (profile.hasGeographicAnomalies()) {
            score += 10;
        }
        
        return score;
    }

    public void updateSecurityProfile(String ip, String action) {
        SecurityProfile profile = securityProfiles.get(ip);
        if (profile != null) {
            profile.recordAction(action, System.currentTimeMillis());
        }
    }

    public SecurityProfile getSecurityProfile(String ip) {
        return securityProfiles.get(ip);
    }
}