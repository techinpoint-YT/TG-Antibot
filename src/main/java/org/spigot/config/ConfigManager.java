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

    public long getJoinCooldown() {
        return getConfig().getLong("cooldowns.join-delay-ms", 3000);
    }


    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    // VPN Protection Enabled
    public boolean isVpnProtectionEnabled() {
        return getConfig().getBoolean("antivpn.enabled", false);
    }

    // ProxyCheck API Key
    public String getVpnApiKey() {
        return getConfig().getString("antivpn.providers.proxycheck.api-key", "");
    }

    // Firewall Enabled
    public boolean isFirewallEnabled() {
        return getConfig().getBoolean("firewall.enabled", false);
    }

    // Cooldown for commands (converted from seconds to ms)
    public long getCommandCooldownMs() {
        int seconds = getConfig().getInt("cooldowns.command-delay-seconds", 3);
        return seconds * 1000L;
    }

    // Debug Mode
    public boolean isDebugMode() {
        return getConfig().getBoolean("debug", false);
    }

    // Bot Protection Settings
    public int getJoinThreshold() {
        return getConfig().getInt("protection.join-threshold", 10);
    }

    public int getPingThreshold() {
        return getConfig().getInt("protection.ping-threshold", 50);
    }

    public long getCooldownTime() {
        return getConfig().getLong("protection.cooldown-time", 60000);
    }

    public boolean isKickOnLockdown() {
        return getConfig().getBoolean("protection.kick-on-lockdown", true);
    }

    public long getTrustedPlayerTime() {
        return getConfig().getLong("protection.trusted-player-time", 3600000);
    }

    public int getTrustedConnectionCount() {
        return getConfig().getInt("protection.trusted-connection-count", 5);
    }

    public long getTempBlacklistDuration() {
        return getConfig().getLong("protection.temp-blacklist-duration", 300);
    }

    // Security Check Settings
    public boolean isConnectionSpeedCheckEnabled() {
        return getConfig().getBoolean("checks.connection-speed.enabled", true);
    }

    public int getMaxConnectionsPerMinute() {
        return getConfig().getInt("checks.connection-speed.max-per-minute", 5);
    }

    public long getMinConnectionInterval() {
        return getConfig().getLong("checks.connection-speed.min-interval", 1000);
    }

    public boolean isNicknameCheckEnabled() {
        return getConfig().getBoolean("checks.nickname.enabled", true);
    }

    public List<String> getBlacklistedNicknames() {
        return getConfig().getStringList("checks.nickname.blacklist");
    }

    public boolean isBlockNonAsciiNicknames() {
        return getConfig().getBoolean("checks.nickname.block-non-ascii", false);
    }

    public boolean isAccountLimitCheckEnabled() {
        return getConfig().getBoolean("checks.account-limit.enabled", true);
    }

    public int getMaxAccountsPerIP() {
        return getConfig().getInt("checks.account-limit.max-accounts", 3);
    }

    public boolean isReconnectCheckEnabled() {
        return getConfig().getBoolean("checks.reconnect.enabled", true);
    }

    public long getMinReconnectTime() {
        return getConfig().getLong("checks.reconnect.min-time", 2000);
    }

    public boolean isGeoLocationCheckEnabled() {
        return getConfig().getBoolean("checks.geolocation.enabled", false);
    }

    public List<String> getBlockedCountries() {
        return getConfig().getStringList("checks.geolocation.blocked-countries");
    }

    public boolean isBehaviorAnalysisEnabled() {
        return getConfig().getBoolean("checks.behavior-analysis.enabled", true);
    }

    public int getSuspicionThreshold() {
        return getConfig().getInt("checks.behavior-analysis.suspicion-threshold", 50);
    }

    // Attack Analysis Settings
    public int getCoordinatedAttackThreshold() {
        return getConfig().getInt("analysis.coordinated-attack-threshold", 20);
    }

    public int getRepeatOffenderThreshold() {
        return getConfig().getInt("analysis.repeat-offender-threshold", 5);
    }

    // Security Risk Settings
    public int getHighRiskThreshold() {
        return getConfig().getInt("security.high-risk-threshold", 70);
    }

    public int getMediumRiskThreshold() {
        return getConfig().getInt("security.medium-risk-threshold", 40);
    }
}
