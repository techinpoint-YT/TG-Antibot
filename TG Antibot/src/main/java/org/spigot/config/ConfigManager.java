package org.spigot.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.spigot.Main;

import java.util.List;

public class ConfigManager {

    private final Main plugin;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig(); // Saves config.yml if not exists
    }

    public void reload() {
        plugin.reloadConfig();
    }

    public void load() {
        reload(); // Alias to reload(), can be expanded if needed
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    // ================================
    // CORE SYSTEM SETTINGS
    // ================================
    
    public boolean isSystemActive() {
        return getConfig().getBoolean("system.active", true);
    }
    
    public boolean isVerboseLogging() {
        return getConfig().getBoolean("system.verbose-logging", false);
    }
    
    public boolean isPerformanceMode() {
        return getConfig().getBoolean("system.performance-mode", false);
    }

    // ================================
    // SHIELD PROTECTION SYSTEM
    // ================================
    
    public int getConnectionBurstLimit() {
        return getConfig().getInt("shield.connection-burst-limit", 10);
    }
    
    public int getServerQueryLimit() {
        return getConfig().getInt("shield.server-query-limit", 50);
    }
    
    public int getDataStreamLimit() {
        return getConfig().getInt("shield.data-stream-limit", 100);
    }
    
    public boolean isAutoDisconnectThreats() {
        return getConfig().getBoolean("shield.auto-disconnect-threats", true);
    }
    
    public long getRecoveryDelay() {
        return getConfig().getLong("shield.recovery-delay", 60000);
    }
    
    public long getTemporaryBanDuration() {
        return getConfig().getLong("shield.temporary-ban-duration", 300);
    }
    
    public long getVeteranPlaytime() {
        return getConfig().getLong("shield.veteran-playtime", 3600000);
    }
    
    public int getTrustedSessionCount() {
        return getConfig().getInt("shield.trusted-session-count", 5);
    }

    // ================================
    // VALIDATION MODULES
    // ================================
    
    public boolean isTimingAnalysisActive() {
        return getConfig().getBoolean("validation.timing-analysis.active", true);
    }
    
    public int getMaxAttemptsPerMinute() {
        return getConfig().getInt("validation.timing-analysis.max-attempts-per-minute", 5);
    }
    
    public long getMinimumDelay() {
        return getConfig().getLong("validation.timing-analysis.minimum-delay", 1000);
    }
    
    public boolean isUsernameFilterActive() {
        return getConfig().getBoolean("validation.username-filter.active", true);
    }
    
    public boolean isBlockSpecialChars() {
        return getConfig().getBoolean("validation.username-filter.block-special-chars", false);
    }
    
    public List<String> getForbiddenPatterns() {
        return getConfig().getStringList("validation.username-filter.forbidden-patterns");
    }
    
    public boolean isSessionControlActive() {
        return getConfig().getBoolean("validation.session-control.active", true);
    }
    
    public int getMaxSessions() {
        return getConfig().getInt("validation.session-control.max-sessions", 3);
    }
    
    public boolean isReconnectMonitorActive() {
        return getConfig().getBoolean("validation.reconnect-monitor.active", true);
    }
    
    public long getMinReconnectTime() {
        return getConfig().getLong("validation.reconnect-monitor.min-reconnect-time", 2000);
    }
    
    public boolean isGeoRestrictionsActive() {
        return getConfig().getBoolean("validation.geo-restrictions.active", false);
    }
    
    public List<String> getRestrictedRegions() {
        return getConfig().getStringList("validation.geo-restrictions.restricted-regions");
    }
    
    public boolean isBehaviorScannerActive() {
        return getConfig().getBoolean("validation.behavior-scanner.active", true);
    }
    
    public int getThreatThreshold() {
        return getConfig().getInt("validation.behavior-scanner.threat-threshold", 50);
    }

    // ================================
    // THREAT DETECTION ENGINE
    // ================================
    
    public int getCoordinatedAssaultThreshold() {
        return getConfig().getInt("threat-detection.coordinated-assault-threshold", 20);
    }
    
    public int getPersistentOffenderThreshold() {
        return getConfig().getInt("threat-detection.persistent-offender-threshold", 5);
    }

    // ================================
    // RISK ASSESSMENT SYSTEM
    // ================================
    
    public int getHighDangerThreshold() {
        return getConfig().getInt("risk-assessment.high-danger-threshold", 70);
    }
    
    public int getModerateDangerThreshold() {
        return getConfig().getInt("risk-assessment.moderate-danger-threshold", 40);
    }
    
    public List<String> getSafeList() {
        return getConfig().getStringList("risk-assessment.safe-list");
    }
    
    public List<String> getDangerList() {
        return getConfig().getStringList("risk-assessment.danger-list");
    }

    // ================================
    // PROXY/VPN SHIELD
    // ================================
    
    public boolean isProxyShieldActive() {
        return getConfig().getBoolean("proxy-shield.active", true);
    }
    
    public boolean isAggressiveMode() {
        return getConfig().getBoolean("proxy-shield.aggressive-mode", true);
    }
    
    public int getCacheLifetimeMinutes() {
        return getConfig().getInt("proxy-shield.cache-lifetime-minutes", 1440);
    }
    
    public String getProxyCheckAccessKey() {
        return getConfig().getString("proxy-shield.api-providers.proxycheck.access-key", "");
    }
    
    public int getRequestTimeout() {
        return getConfig().getInt("proxy-shield.api-providers.proxycheck.request-timeout", 5000);
    }

    // ================================
    // NETWORK BARRIER SYSTEM
    // ================================
    
    public boolean isNetworkBarrierActive() {
        return getConfig().getBoolean("network-barrier.active", true);
    }
    
    public long getAutoBlockDuration() {
        return getConfig().getLong("network-barrier.auto-block-duration", 3600);
    }
    
    public List<String> getBlockedAddresses() {
        return getConfig().getStringList("network-barrier.blocked-addresses");
    }
    
    public List<String> getSafeAddresses() {
        return getConfig().getStringList("network-barrier.safe-addresses");
    }
    
    public boolean isBlockTorNodes() {
        return getConfig().getBoolean("network-barrier.block-tor-nodes", false);
    }
    
    public boolean isBlockHostingServices() {
        return getConfig().getBoolean("network-barrier.block-hosting-services", false);
    }

    // ================================
    // CONNECTION FLOW CONTROL
    // ================================
    
    public int getMaxConnections() {
        return getConfig().getInt("flow-control.max-connections", 5);
    }
    
    public int getTimeWindowSeconds() {
        return getConfig().getInt("flow-control.time-window-seconds", 1);
    }
    
    public boolean isBurstShield() {
        return getConfig().getBoolean("flow-control.burst-shield", true);
    }

    // ================================
    // USER DELAY SYSTEM
    // ================================
    
    public long getConnectionDelayMs() {
        return getConfig().getLong("user-delays.connection-delay-ms", 3000);
    }
    
    public long getActionDelaySeconds() {
        return getConfig().getLong("user-delays.action-delay-seconds", 3);
    }
    
    public String getOverridePermission() {
        return getConfig().getString("user-delays.override-permission", "tga.bypass");
    }

    // ================================
    // ALERT & NOTIFICATION SYSTEM
    // ================================
    
    public String getStaffAlertPermission() {
        return getConfig().getString("notifications.staff-alert-permission", "tga.alerts");
    }
    
    public boolean isAnnounceThreats() {
        return getConfig().getBoolean("notifications.announce-threats", true);
    }
    
    public boolean isFileLogging() {
        return getConfig().getBoolean("notifications.file-logging", true);
    }
    
    public String getLogFilename() {
        return getConfig().getString("notifications.log-filename", "tga-security.log");
    }
    
    public boolean isStatusBarUpdates() {
        return getConfig().getBoolean("notifications.status-bar-updates", true);
    }
    
    public boolean isPopupNotifications() {
        return getConfig().getBoolean("notifications.popup-notifications", true);
    }
    
    public boolean isProgressBarStatus() {
        return getConfig().getBoolean("notifications.progress-bar-status", true);
    }

    // ================================
    // EXPERIMENTAL FEATURES
    // ================================
    
    public boolean isAiThreatDetection() {
        return getConfig().getBoolean("experimental.ai-threat-detection", false);
    }
    
    public boolean isAdaptiveLearning() {
        return getConfig().getBoolean("experimental.adaptive-learning", false);
    }
    
    public String getDiscordAlertWebhook() {
        return getConfig().getString("experimental.discord-alert-webhook", "");
    }
    
    public boolean isDatabaseStorage() {
        return getConfig().getBoolean("experimental.database-storage", false);
    }
    
    public boolean isAsyncValidation() {
        return getConfig().getBoolean("experimental.async-validation", true);
    }
    
    public int getProfileCacheSize() {
        return getConfig().getInt("experimental.profile-cache-size", 10000);
    }
    
    public int getMaintenanceInterval() {
        return getConfig().getInt("experimental.maintenance-interval", 3600);
    }

    // ================================
    // PLUGIN COMPATIBILITY
    // ================================
    
    public boolean isAuthmeSupport() {
        return getConfig().getBoolean("integrations.authme-support", true);
    }
    
    public boolean isEssentialsSupport() {
        return getConfig().getBoolean("integrations.essentials-support", true);
    }
    
    public boolean isLuckpermsSupport() {
        return getConfig().getBoolean("integrations.luckperms-support", true);
    }
    
    public boolean isViaversionCompatibility() {
        return getConfig().getBoolean("integrations.viaversion-compatibility", true);
    }
    
    public boolean isProtocollibIntegration() {
        return getConfig().getBoolean("integrations.protocollib-integration", true);
    }

    // ================================
    // LEGACY COMPATIBILITY METHODS
    // ================================
    
    // These methods maintain compatibility with existing code
    public boolean isVpnProtectionEnabled() {
        return isProxyShieldActive();
    }
    
    public String getVpnApiKey() {
        return getProxyCheckAccessKey();
    }
    
    public boolean isFirewallEnabled() {
        return isNetworkBarrierActive();
    }
    
    public long getCommandCooldownMs() {
        return getActionDelaySeconds() * 1000L;
    }
    
    public boolean isDebugMode() {
        return isVerboseLogging();
    }
    
    public int getJoinThreshold() {
        return getConnectionBurstLimit();
    }
    
    public int getPingThreshold() {
        return getServerQueryLimit();
    }
    
    public long getCooldownTime() {
        return getRecoveryDelay();
    }
    
    public boolean isKickOnLockdown() {
        return isAutoDisconnectThreats();
    }
    
    public long getTrustedPlayerTime() {
        return getVeteranPlaytime();
    }
    
    public long getTempBlacklistDuration() {
        return getTemporaryBanDuration();
    }
    
    public boolean isConnectionSpeedCheckEnabled() {
        return isTimingAnalysisActive();
    }
    
    public int getMaxConnectionsPerMinute() {
        return getMaxAttemptsPerMinute();
    }
    
    public long getMinConnectionInterval() {
        return getMinimumDelay();
    }
    
    public boolean isNicknameCheckEnabled() {
        return isUsernameFilterActive();
    }
    
    public List<String> getBlacklistedNicknames() {
        return getForbiddenPatterns();
    }
    
    public boolean isBlockNonAsciiNicknames() {
        return isBlockSpecialChars();
    }
    
    public boolean isAccountLimitCheckEnabled() {
        return isSessionControlActive();
    }
    
    public int getMaxAccountsPerIP() {
        return getMaxSessions();
    }
    
    public boolean isReconnectCheckEnabled() {
        return isReconnectMonitorActive();
    }
    
    public boolean isGeoLocationCheckEnabled() {
        return isGeoRestrictionsActive();
    }
    
    public List<String> getBlockedCountries() {
        return getRestrictedRegions();
    }
    
    public boolean isBehaviorAnalysisEnabled() {
        return isBehaviorScannerActive();
    }
    
    public int getSuspicionThreshold() {
        return getThreatThreshold();
    }
    
    public int getCoordinatedAttackThreshold() {
        return getCoordinatedAssaultThreshold();
    }
    
    public int getRepeatOffenderThreshold() {
        return getPersistentOffenderThreshold();
    }
    
    public int getHighRiskThreshold() {
        return getHighDangerThreshold();
    }
    
    public int getMediumRiskThreshold() {
        return getModerateDangerThreshold();
    }
    
    public long getJoinCooldown() {
        return getConnectionDelayMs();
    }
}