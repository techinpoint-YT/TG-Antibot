package org.spigot.core;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spigot.Main;
import org.spigot.core.checks.*;
import org.spigot.core.data.PlayerProfile;
import org.spigot.core.data.AttackData;
import org.spigot.enums.ProtectionMode;
import org.spigot.enums.AttackType;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class BotProtectionManager {

    private final Main plugin;
    private final Map<String, PlayerProfile> playerProfiles;
    private final Map<String, Long> joinTimestamps;
    private final Map<String, Integer> connectionAttempts;
    
    // Protection modes
    private ProtectionMode currentMode;
    private long lastModeChange;
    
    // Attack detection
    private final AtomicLong joinsPerSecond;
    private final AtomicLong pingsPerSecond;
    private final AtomicLong packetsPerSecond;
    private final AttackData currentAttack;
    
    // Security checks
    private final ConnectionSpeedCheck speedCheck;
    private final NicknameCheck nicknameCheck;
    private final AccountLimitCheck accountCheck;
    private final ReconnectCheck reconnectCheck;
    private final GeoLocationCheck geoCheck;
    private final BehaviorAnalysisCheck behaviorCheck;
    
    // Whitelists and blacklists
    private final Set<String> whitelist;
    private final Set<String> blacklist;
    private final Set<String> tempBlacklist;

    public BotProtectionManager(Main plugin) {
        this.plugin = plugin;
        this.playerProfiles = new ConcurrentHashMap<>();
        this.joinTimestamps = new ConcurrentHashMap<>();
        this.connectionAttempts = new ConcurrentHashMap<>();
        
        this.currentMode = ProtectionMode.NORMAL;
        this.lastModeChange = System.currentTimeMillis();
        
        this.joinsPerSecond = new AtomicLong(0);
        this.pingsPerSecond = new AtomicLong(0);
        this.packetsPerSecond = new AtomicLong(0);
        this.currentAttack = new AttackData();
        
        // Initialize checks
        this.speedCheck = new ConnectionSpeedCheck(plugin);
        this.nicknameCheck = new NicknameCheck(plugin);
        this.accountCheck = new AccountLimitCheck(plugin);
        this.reconnectCheck = new ReconnectCheck(plugin);
        this.geoCheck = new GeoLocationCheck(plugin);
        this.behaviorCheck = new BehaviorAnalysisCheck(plugin);
        
        this.whitelist = ConcurrentHashMap.newKeySet();
        this.blacklist = ConcurrentHashMap.newKeySet();
        this.tempBlacklist = ConcurrentHashMap.newKeySet();
        
        startCounterResetTask();
        loadData();
    }

    public boolean shouldBlockConnection(UUID uuid, InetAddress address, String name) {
        String ip = address.getHostAddress();
        long currentTime = System.currentTimeMillis();
        
        // Update counters
        joinsPerSecond.incrementAndGet();
        
        // Check whitelist first
        if (whitelist.contains(ip)) {
            return false;
        }
        
        // Check blacklists
        if (blacklist.contains(ip) || tempBlacklist.contains(ip)) {
            plugin.getLogger().info("§cBlocked connection from blacklisted IP: " + ip + " (Player: " + name + ")");
            return true;
        }
        
        // Get or create player profile
        PlayerProfile profile = playerProfiles.computeIfAbsent(ip, k -> new PlayerProfile(ip));
        profile.addConnection(name, currentTime);
        
        // Run security checks
        if (speedCheck.shouldBlock(profile, currentTime)) {
            addToTempBlacklist(ip, "Connection speed violation");
            return true;
        }
        
        if (nicknameCheck.shouldBlock(name)) {
            addToTempBlacklist(ip, "Suspicious nickname pattern");
            return true;
        }
        
        if (accountCheck.shouldBlock(profile)) {
            addToTempBlacklist(ip, "Too many accounts from IP");
            return true;
        }
        
        if (reconnectCheck.shouldBlock(profile, currentTime)) {
            addToTempBlacklist(ip, "Rapid reconnection detected");
            return true;
        }
        
        if (geoCheck.shouldBlock(ip)) {
            addToTempBlacklist(ip, "Geo-location restriction");
            return true;
        }
        
        if (behaviorCheck.shouldBlock(profile)) {
            addToTempBlacklist(ip, "Suspicious behavior pattern");
            return true;
        }
        
        // Check protection mode
        if (currentMode == ProtectionMode.LOCKDOWN) {
            return true;
        }
        
        if (currentMode == ProtectionMode.STRICT && !isPlayerTrusted(profile)) {
            return true;
        }
        
        // Update attack detection
        updateAttackDetection();
        
        return false;
    }

    public void handleServerPing(InetAddress address) {
        pingsPerSecond.incrementAndGet();
        String ip = address.getHostAddress();
        
        PlayerProfile profile = playerProfiles.get(ip);
        if (profile != null) {
            profile.addPing(System.currentTimeMillis());
        }
    }

    public void handlePacket(String ip) {
        packetsPerSecond.incrementAndGet();
    }

    private void updateAttackDetection() {
        long joins = joinsPerSecond.get();
        long pings = pingsPerSecond.get();
        
        // Detect attack patterns
        if (joins > plugin.getConfigManager().getJoinThreshold()) {
            if (currentMode != ProtectionMode.LOCKDOWN) {
                setProtectionMode(ProtectionMode.LOCKDOWN);
                currentAttack.startAttack(AttackType.JOIN_FLOOD, joins);
                plugin.getNotificationManager().broadcastAttackAlert(AttackType.JOIN_FLOOD, joins);
            }
        } else if (pings > plugin.getConfigManager().getPingThreshold()) {
            if (currentMode != ProtectionMode.STRICT) {
                setProtectionMode(ProtectionMode.STRICT);
                currentAttack.startAttack(AttackType.PING_FLOOD, pings);
                plugin.getNotificationManager().broadcastAttackAlert(AttackType.PING_FLOOD, pings);
            }
        } else if (currentMode != ProtectionMode.NORMAL && 
                   System.currentTimeMillis() - lastModeChange > plugin.getConfigManager().getCooldownTime()) {
            setProtectionMode(ProtectionMode.NORMAL);
            currentAttack.endAttack();
            plugin.getNotificationManager().broadcastAttackEnd();
        }
    }

    private void setProtectionMode(ProtectionMode mode) {
        if (currentMode != mode) {
            ProtectionMode oldMode = currentMode;
            currentMode = mode;
            lastModeChange = System.currentTimeMillis();
            
            plugin.getLogger().info("§eProtection mode changed from §c" + oldMode + " §eto §a" + mode);
            
            // Kick players if switching to lockdown
            if (mode == ProtectionMode.LOCKDOWN && plugin.getConfigManager().isKickOnLockdown()) {
                kickSuspiciousPlayers();
            }
        }
    }

    private void kickSuspiciousPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tga.bypass")) continue;
            
            String ip = player.getAddress().getAddress().getHostAddress();
            PlayerProfile profile = playerProfiles.get(ip);
            
            if (profile != null && !isPlayerTrusted(profile)) {
                player.kickPlayer(plugin.getMessages().get("lockdown-kick", 
                    "§cServer is under attack protection. Please try again later."));
            }
        }
    }

    private boolean isPlayerTrusted(PlayerProfile profile) {
        return profile.getPlayTime() > plugin.getConfigManager().getTrustedPlayerTime() ||
               profile.getConnectionCount() > plugin.getConfigManager().getTrustedConnectionCount();
    }

    public void addToWhitelist(String ip) {
        whitelist.add(ip);
        blacklist.remove(ip);
        tempBlacklist.remove(ip);
        saveData();
    }

    public void addToBlacklist(String ip, String reason) {
        blacklist.add(ip);
        whitelist.remove(ip);
        plugin.getLogger().info("§cAdded IP to blacklist: " + ip + " (Reason: " + reason + ")");
        saveData();
    }

    private void addToTempBlacklist(String ip, String reason) {
        tempBlacklist.add(ip);
        plugin.getLogger().info("§eAdded IP to temporary blacklist: " + ip + " (Reason: " + reason + ")");
        
        // Remove from temp blacklist after configured time
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            tempBlacklist.remove(ip);
        }, plugin.getConfigManager().getTempBlacklistDuration() * 20L);
    }

    public void removeFromBlacklist(String ip) {
        blacklist.remove(ip);
        tempBlacklist.remove(ip);
        saveData();
    }

    private void startCounterResetTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            joinsPerSecond.set(0);
            pingsPerSecond.set(0);
            packetsPerSecond.set(0);
        }, 20L, 20L); // Reset every second
    }

    public void reload() {
        loadData();
    }

    public void shutdown() {
        saveData();
    }

    private void loadData() {
        // Load whitelist and blacklist from config
        whitelist.clear();
        blacklist.clear();
        
        whitelist.addAll(plugin.getConfig().getStringList("security.whitelist"));
        blacklist.addAll(plugin.getConfig().getStringList("security.blacklist"));
    }

    private void saveData() {
        plugin.getConfig().set("security.whitelist", new ArrayList<>(whitelist));
        plugin.getConfig().set("security.blacklist", new ArrayList<>(blacklist));
        plugin.saveConfig();
    }

    // Getters
    public ProtectionMode getCurrentMode() { return currentMode; }
    public long getJoinsPerSecond() { return joinsPerSecond.get(); }
    public long getPingsPerSecond() { return pingsPerSecond.get(); }
    public long getPacketsPerSecond() { return packetsPerSecond.get(); }
    public AttackData getCurrentAttack() { return currentAttack; }
    public Set<String> getWhitelist() { return new HashSet<>(whitelist); }
    public Set<String> getBlacklist() { return new HashSet<>(blacklist); }
    public Map<String, PlayerProfile> getPlayerProfiles() { return new HashMap<>(playerProfiles); }
}