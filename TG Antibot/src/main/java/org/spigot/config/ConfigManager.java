package org.spigot.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.spigot.Main;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConfigManager {

    private final Main plugin;
    private final ConcurrentMap<String, Object> configCache;
    private volatile long lastReload;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.configCache = new ConcurrentHashMap<>();
        this.lastReload = System.currentTimeMillis();
        plugin.saveDefaultConfig(); // Saves config.yml if not exists
        validateConfiguration();
    }

    public void reload() {
        plugin.reloadConfig();
        configCache.clear();
        lastReload = System.currentTimeMillis();
        validateConfiguration();
    }

    private void validateConfiguration() {
        FileConfiguration config = getConfig();
        
        // Validate critical settings
        if (config.getInt("shield.connection-burst-limit", -1) < 1) {
            plugin.getLogger().warning("Invalid connection-burst-limit, using default: 10");
            config.set("shield.connection-burst-limit", 10);
        }
        
        if (config.getInt("shield.server-query-limit", -1) < 1) {
            plugin.getLogger().warning("Invalid server-query-limit, using default: 50");
            config.set("shield.server-query-limit", 50);
        }
        
        // Validate time values
        if (config.getLong("shield.recovery-delay", -1) < 1000) {
            plugin.getLogger().warning("Invalid recovery-delay, using default: 60000ms");
            config.set("shield.recovery-delay", 60000L);
        }
        
        plugin.saveConfig();
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getCachedValue(String key, Class<T> type, T defaultValue) {
        return (T) configCache.computeIfAbsent(key, k -> {
            FileConfiguration config = getConfig();
            if (type == String.class) {
                return config.getString(k, (String) defaultValue);
            } else if (type == Integer.class) {
                return config.getInt(k, (Integer) defaultValue);
            } else if (type == Long.class) {
                return config.getLong(k, (Long) defaultValue);
            } else if (type == Boolean.class) {
                return config.getBoolean(k, (Boolean) defaultValue);
            } else if (type == Double.class) {
                return config.getDouble(k, (Double) defaultValue);
            }
            return defaultValue;
        });
    }

    // ================================
    // CORE SYSTEM SETTINGS
    // ================================
    
    public boolean isSystemActive() {
        return getCachedValue("system.active", Boolean.class, true);
    }
    
    public boolean isVerboseLogging() {
        return getCachedValue("system.verbose-logging", Boolean.class, false);
    }
    
    public boolean isPerformanceMode() {
        return getCachedValue("system.performance-mode", Boolean.class, false);
    }

    // ================================
    // SHIELD PROTECTION SYSTEM
    // ================================
    
    public int getConnectionBurstLimit() {
        return getCachedValue("shield.connection-burst-limit", Integer.class, 10);
    }
    
    public int getServerQueryLimit() {
        return getCachedValue("shield.server-query-limit", Integer.class, 50);
    }
    
    public int getDataStreamLimit() {
        return getCachedValue("shield.data-stream-limit", Integer.class, 100);
    }
    
    public boolean isAutoDisconnectThreats() {
        return getCachedValue("shield.auto-disconnect-threats", Boolean.class, true);
    }
    
    public long getRecoveryDelay() {
        return getCachedValue("shield.recovery-delay", Long.class, 60000L);
    }
    
    public long getTemporaryBanDuration() {
        return getCachedValue("shield.temporary-ban-duration", Long.class, 300L);
    }
    
    public long getVeteranPlaytime() {
        return getCachedValue("shield.veteran-playtime", Long.class, 3600000L);
    }
    
    public int getTrustedSessionCount() {
        return getCachedValue("shield.trusted-session-count", Integer.class, 5);
    }

    // ================================
    // VALIDATION MODULES
    // ================================
    
    public boolean isTimingAnalysisActive() {
        return getCachedValue("validation.timing-analysis.active", Boolean.class, true);
    }
    
    public int getMaxAttemptsPerMinute() {
        return getCachedValue("validation.timing-analysis.max-attempts-per-minute", Integer.class, 5);
    }
    
    public long getMinimumDelay() {
        return getCachedValue("validation.timing-analysis.minimum-delay", Long.class, 1000L);
    }
    
    public boolean isUsernameFilterActive() {
        return getCachedValue("validation.username-filter.active", Boolean.class, true);
    }
    
    public boolean isBlockSpecialChars() {
        return getCachedValue("validation.username-filter.block-special-chars", Boolean.class, false);
    }
    
    public List<String> getForbiddenPatterns() {
        return getConfig().getStringList("validation.username-filter.forbidden-patterns");
    }
    
    public boolean isSessionControlActive() {
        return getCachedValue("validation.session-control.active", Boolean.class, true);
    }
    
    public int getMaxSessions() {
        return getCachedValue("validation.session-control.max-sessions", Integer.class, 3);
    }
    
    public boolean isReconnectMonitorActive() {
        return getCachedValue("validation.reconnect-monitor.active", Boolean.class, true);
    }
    
    public long getMinReconnectTime() {
        return getCachedValue("validation.reconnect-monitor.min-reconnect-time", Long.class, 2000L);
    }
    
    public boolean isGeoRestrictionsActive() {
        return getCachedValue("validation.geo-restrictions.active", Boolean.class, false);
    }
    
    public List<String> getRestrictedRegions() {
        return getConfig().getStringList("validation.geo-restrictions.restricted-regions");
    }
    
    public boolean isBehaviorScannerActive() {
        return getCachedValue("validation.behavior-scanner.active", Boolean.class, true);
    }
    
    public int getThreatThreshold() {
        return getCachedValue("validation.behavior-scanner.threat-threshold", Integer.class, 50);
    }

    // ================================
    // THREAT DETECTION ENGINE
    // ================================
    
    public int getCoordinatedAssaultThreshold() {
        return getCachedValue("threat-detection.coordinated-assault-threshold", Integer.class, 20);
    }
    
    public int getPersistentOffenderThreshold() {
        return getCachedValue("threat-detection.persistent-offender-threshold", Integer.class, 5);
    }

    // ================================
    // RISK ASSESSMENT SYSTEM
    // ================================
    
    public int getHighDangerThreshold() {
        return getCachedValue("risk-assessment.high-danger-threshold", Integer.class, 70);
    }
    
    public int getModerateDangerThreshold() {
        return getCachedValue("risk-assessment.moderate-danger-threshold", Integer.class, 40);
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
        return getCachedValue("proxy-shield.active", Boolean.class, true);
    }
    
    public boolean isAggressiveMode() {
        return getCachedValue("proxy-shield.aggressive-mode", Boolean.class, true);
    }
    
    public int getCacheLifetimeMinutes() {
        return getCachedValue("proxy-shield.cache-lifetime-minutes", Integer.class, 1440);
    }
    
    public String getProxyCheckAccessKey() {
        return getCachedValue("proxy-shield.api-providers.proxycheck.access-key", String.class, "");
    }
    
    public int getRequestTimeout() {
        return getCachedValue("proxy-shield.api-providers.proxycheck.request-timeout", Integer.class, 5000);
    }

    // ================================
    // NETWORK BARRIER SYSTEM
    // ================================
    
    public boolean isNetworkBarrierActive() {
        return getCachedValue("network-barrier.active", Boolean.class, true);
    }
    
    public long getAutoBlockDuration() {
        return getCachedValue("network-barrier.auto-block-duration", Long.class, 3600L);
    }
    
    public List<String> getBlockedAddresses() {
        return getConfig().getStringList("network-barrier.blocked-addresses");
    }
    
    public List<String> getSafeAddresses() {
        return getConfig().getStringList("network-barrier.safe-addresses");
    }
    
    public boolean isBlockTorNodes() {
        return getCachedValue("network-barrier.block-tor-nodes", Boolean.class, false);
    }
    
    public boolean isBlockHostingServices() {
        return getCachedValue("network-barrier.block-hosting-services", Boolean.class, false);
    }

    // ================================
    // CONNECTION FLOW CONTROL
    // ================================
    
    public int getMaxConnections() {
        return getCachedValue("flow-control.max-connections", Integer.class, 5);
    }
    
    public int getTimeWindowSeconds() {
        return getCachedValue("flow-control.time-window-seconds", Integer.class, 1);
    }
    
    public boolean isBurstShield() {
        return getCachedValue("flow-control.burst-shield", Boolean.class, true);
    }

    // ================================
    // USER DELAY SYSTEM
    // ================================
    
    public long getConnectionDelayMs() {
        return getCachedValue("user-delays.connection-delay-ms", Long.class, 3000L);
    }
    
    public long getActionDelaySeconds() {
        return getCachedValue("user-delays.action-delay-seconds", Long.class, 3L);
    }
    
    public String getOverridePermission() {
        return getCachedValue("user-delays.override-permission", String.class, "tga.bypass");
    }

    // ================================
    // ALERT & NOTIFICATION SYSTEM
    // ================================
    
    public String getStaffAlertPermission() {
        return getCachedValue("notifications.staff-alert-permission", String.class, "tga.alerts");
    }
    
    public boolean isAnnounceThreats() {
        return getCachedValue("notifications.announce-threats", Boolean.class, true);
    }
    
    public boolean isFileLogging() {
        return getCachedValue("notifications.file-logging", Boolean.class, true);
    }
    
    public String getLogFilename() {
        return getCachedValue("notifications.log-filename", String.class, "tga-security.log");
    }
    
    public boolean isStatusBarUpdates() {
        return getCachedValue("notifications.status-bar-updates", Boolean.class, true);
    }
    
    public boolean isPopupNotifications() {
        return getCachedValue("notifications.popup-notifications", Boolean.class, true);
    }
    
    public boolean isProgressBarStatus() {
        return getCachedValue("notifications.progress-bar-status", Boolean.class, true);
    }

    // ================================
    // EXPERIMENTAL FEATURES
    // ================================
    
    public boolean isAiThreatDetection() {
        return getCachedValue("experimental.ai-threat-detection", Boolean.class, false);
    }
    
    public boolean isAdaptiveLearning() {
        return getCachedValue("experimental.adaptive-learning", Boolean.class, false);
    }
    
    public String getDiscordAlertWebhook() {
        return getCachedValue("experimental.discord-alert-webhook", String.class, "");
    }
    
    public boolean isDatabaseStorage() {
        return getCachedValue("experimental.database-storage", Boolean.class, false);
    }
    
    public boolean isAsyncValidation() {
        return getCachedValue("experimental.async-validation", Boolean.class, true);
    }
    
    public int getProfileCacheSize() {
        return getCachedValue("experimental.profile-cache-size", Integer.class, 10000);
    }
    
    public int getMaintenanceInterval() {
        return getCachedValue("experimental.maintenance-interval", Integer.class, 3600);
    }

    // ================================
    // PLUGIN COMPATIBILITY
    // ================================
    
    public boolean isAuthmeSupport() {
        return getCachedValue("integrations.authme-support", Boolean.class, true);
    }
    
    public boolean isEssentialsSupport() {
        return getCachedValue("integrations.essentials-support", Boolean.class, true);
    }
    
    public boolean isLuckpermsSupport() {
        return getCachedValue("integrations.luckperms-support", Boolean.class, true);
    }
    
    public boolean isViaversionCompatibility() {
        return getCachedValue("integrations.viaversion-compatibility", Boolean.class, true);
    }
    
    public boolean isProtocollibIntegration() {
        return getCachedValue("integrations.protocollib-integration", Boolean.class, true);
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
    
    // ================================
    // UTILITY METHODS
    // ================================
    
    public long getLastReloadTime() {
        return lastReload;
    }
    
    public void clearCache() {
        configCache.clear();
    }
    
    public int getCacheSize() {
        return configCache.size();
    }
}