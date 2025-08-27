package org.spigot.core;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.spigot.Main;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Spigot-friendly, single-file port of ArkFlame's AntiBot checks:
 * - accounts, blacklist, fastchat, nickname, ratelimit, reconnect,
 *   password, runtime, settings, whitelist
 *
 * Reads your merged config.yml sections (e.g., accounts.enabled, ratelimit.throttle, etc.)
 * and runs them inside one shouldBlock(...) call.
 *
 * NOTE: Some original Bungee packet/threshold details aren't available on plain Spigot PreLogin,
 * so this adapts the logic using join/reconnect timing and rolling windows that closely match behavior
 * of the original modules. (Based on ArkFlame modules’ fields and flow.)
 */
public class ProtectionModule {

    private final Main plugin;
    private final FileConfiguration cfg;

    // -------- Per-IP state bucket (similar to BotPlayer in ArkFlame) --------
    private static final class IpState {
        // Accounts
        final Set<String> accounts = Collections.newSetFromMap(new ConcurrentHashMap<>());
        int totalAccountsSeen = 0;

        // Timing
        long lastConnection = 0L;
        long lastPing = 0L;           // we approximate "ping" as another touch
        String lastNickname = "";

        // Reconnect / reping counters
        int reconnects = 0;
        int repings = 0;

        // Rate limit (approximate packets/joins using attempt timestamps)
        final Deque<Long> joinWindow = new ArrayDeque<>();
        final Deque<Long> packetWindow = new ArrayDeque<>();

        // Chat / settings flags (best-effort on Spigot side)
        long lastChatMillis = 0L;
        boolean settingsOK = true; // You can flip this from elsewhere if you do a settings handshake.
    }

    // Master maps
    private final Map<String, IpState> ipMap = new ConcurrentHashMap<>();
    private final Set<String> blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> whitelist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // Nickname blacklist pattern (from ArkFlame NicknameModule defaults)
    private static final String MCSPAM_WORDS = "(Craft|Beach|Actor|Games|Tower|Elder|Mine|Nitro|Worms|Build|Plays|Hyper|Crazy|Super|_Itz|Slime)";
    private static final String MCSPAM_SUFFIX = "(11|50|69|99|88|HD|LP|XD|YT)";
    private static final Pattern NICK_PATTERN = Pattern.compile("^" + MCSPAM_WORDS + MCSPAM_WORDS + MCSPAM_SUFFIX); // :contentReference[oaicite:1]{index=1}

    public ProtectionModule(Main plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
        // If you want to preload blacklist/whitelist from disk, do it here to mirror ArkFlame’s modules. :contentReference[oaicite:2]{index=2}
    }

    /**
     * Call this on AsyncPlayerPreLoginEvent (pre-login) with the user's data.
     * Returns true to block the connection.
     */
    public boolean shouldBlock(UUID uuid, InetAddress ip, String name) {
        final String ipStr = ip.getHostAddress();
        final long now = System.currentTimeMillis();
        final IpState st = ipMap.computeIfAbsent(ipStr, k -> new IpState());

        // Maintain quick stats
        st.lastPing = now; // treat this call as a ping-like touch
        // bump accounts seen
        st.accounts.add(name);
        st.totalAccountsSeen = Math.max(st.totalAccountsSeen, st.accounts.size());

        // rolling window for join attempts (we treat prelogin as an attempt)
        pushToWindow(st.joinWindow, now, 5_000L); // keep 5s for burst checks

        // ---------- Checks (order roughly mirrors ArkFlame flow) ----------
        if (checkBlacklist(ipStr)) return punish("blacklist", name, cfg.getStringList("blacklist.commands"));

        if (checkWhitelistGate(ipStr)) return punish("whitelist", name, cfg.getStringList("whitelist.commands"));

        if (checkNickname(name)) return punish("nickname", name, cfg.getStringList("nickname.commands"));

        if (checkAccounts(st)) return punish("accounts", name, cfg.getStringList("accounts.commands")); // :contentReference[oaicite:3]{index=3}

        if (checkRateLimit(st)) return punish("ratelimit", name, cfg.getStringList("ratelimit.commands")); // :contentReference[oaicite:4]{index=4}

        if (checkReconnect(st, name)) return punish("reconnect", name, cfg.getStringList("reconnect.commands")); // :contentReference[oaicite:5]{index=5}

        if (checkFastChat(st)) return punish("fastchat", name, cfg.getStringList("fastchat.commands")); // :contentReference[oaicite:6]{index=6}

        if (checkPassword(name)) return punish("password", name, cfg.getStringList("password.commands"));

        if (checkSettings(st, name)) return punish("settings", name, cfg.getStringList("settings.commands"));

        if (checkRuntime(ipStr)) {
            // Runtime module in ArkFlame also adds to OS firewall; here we execute configured shell lines. :contentReference[oaicite:7]{index=7}
            runRuntimeAdd(ipStr);
            // Runtime by itself doesn’t always imply kick; return false to allow other checks to decide.
        }

        // Passed → update last connection
        st.lastConnection = now;
        return false;
    }

    // =========================================================
    // Individual checks (enable flags + config fields expected)
    // =========================================================

    private boolean checkAccounts(IpState st) {
        if (!cfg.getBoolean("accounts.enabled", false)) return false;
        final int limit = cfg.getInt("accounts.limit", 2);
        return st.totalAccountsSeen >= limit; // like ArkFlame's getTotalAccounts() vs limit. :contentReference[oaicite:8]{index=8}
    }

    private boolean checkRateLimit(IpState st) {
        if (!cfg.getBoolean("ratelimit.enabled", false)) return false;

        // ArkFlame has throttle + max_online + thresholds; we approximate using join window and a short throttle. :contentReference[oaicite:9]{index=9}
        final int maxOnline = cfg.getInt("ratelimit.max_online", 3);
        final int throttleMs = cfg.getInt("ratelimit.throttle", 800);

        final boolean throttled = (System.currentTimeMillis() - st.lastConnection) < throttleMs;
        final boolean tooManyAccountsOnline = st.accounts.size() > maxOnline;

        // Optionally consider thresholds.threshold.pps/cps/jps if present:
        final int pps = cfg.getInt("ratelimit.threshold.pps", 0);
        final int jps = cfg.getInt("ratelimit.threshold.jps", 0);
        boolean thresholdMet = false;
        if (pps > 0) thresholdMet |= countInWindow(st.packetWindow, 1_000L) >= pps;
        if (jps > 0) thresholdMet |= countInWindow(st.joinWindow, 1_000L) >= jps;

        return thresholdMet || throttled || tooManyAccountsOnline;
    }

    private boolean checkReconnect(IpState st, String currentName) {
        if (!cfg.getBoolean("reconnect.enabled", false)) return false;

        final int timesPing = cfg.getInt("reconnect.times.ping", 1);
        final int timesConnect = cfg.getInt("reconnect.times.connect", 3);
        final long throttle = cfg.getLong("reconnect.throttle", 800L);

        final long now = System.currentTimeMillis();
        boolean nicknameChanged = !currentName.equals(st.lastNickname);
        boolean withinThrottle = (now - st.lastConnection) < throttle;

        if (nicknameChanged || (timesPing > 0 && (now - st.lastPing < 550)) || withinThrottle) {
            // Reset like ArkFlame does when conditions break the sequence. :contentReference[oaicite:10]{index=10}
            st.reconnects = 0;
            st.repings = 0;
            st.lastNickname = currentName;
            return false;
        } else {
            // continuing same nickname & under thresholds window
            st.reconnects += 1;
            // We can’t detect actual server list ping counts reliably here; treat quick successive calls as "repings".
            st.repings += 1;

            return !(st.reconnects < timesConnect || st.repings < timesPing); // block when limits exceeded
        }
    }

    private boolean checkFastChat(IpState st) {
        if (!cfg.getBoolean("fastchat.enabled", false)) return false;
        final long time = cfg.getLong("fastchat.time", 1000L); // ArkFlame default is 1000ms. :contentReference[oaicite:11]{index=11}
        // On pre-login we approximate: if lastConnection is too recent OR settings not set, block.
        return (System.currentTimeMillis() - st.lastConnection < time) || !st.settingsOK;
    }

    private boolean checkNickname(String name) {
        if (!cfg.getBoolean("nickname.enabled", false)) return false;
        final List<String> list = cfg.getStringList("nickname.blacklist");
        final String lower = name.toLowerCase(Locale.ROOT);

        for (String bad : list) {
            if (lower.contains(bad.toLowerCase(Locale.ROOT))) return true;
        }
        return NICK_PATTERN.matcher(name).find(); // matches common MCSpam style. :contentReference[oaicite:12]{index=12}
    }

    private boolean checkWhitelistGate(String ipStr) {
        if (!cfg.getBoolean("whitelist.enabled", false)) return false;

        // When enabled, only whitelisted IPs are allowed during lockout/whitelist window,
        // similar to ArkFlame WhitelistModule behavior (time windows & switch handled externally). :contentReference[oaicite:13]{index=13}
        final boolean requireSwitch = cfg.getBoolean("whitelist.switch", true);
        final int tWhitelist = cfg.getInt("whitelist.time.whitelist", 15000);
        final int tLockout = cfg.getInt("whitelist.time.lockout", 20000);

        // Basic gate: if "switch" is required, we just rely on explicit whitelist collection.
        // (You can wire a timed state from elsewhere; here we only check membership.)
        if (requireSwitch) {
            return !whitelist.contains(ipStr);
        } else {
            // If switch not required, allow during "whitelist window".
            // Without a central scheduler here, we treat enabled=true as active window.
            return !whitelist.contains(ipStr);
        }
    }

    private boolean checkBlacklist(String ipStr) {
        if (!cfg.getBoolean("blacklist.enabled", false)) return false;
        return blacklist.contains(ipStr);
    }

    private boolean checkPassword(String name) {
        if (!cfg.getBoolean("password.enabled", false)) return false;
        // In ArkFlame this integrates with auth commands. On PreLogin we can't verify commands yet.
        // Return false here; you can enforce in a PlayerJoin/CommandPreprocess listener if desired.
        return false;
    }

    private boolean checkSettings(IpState st, String name) {
        if (!cfg.getBoolean("settings.enabled", false)) return false;

        // ArkFlame defers to a settings "pending" set and kicks after a delay if not confirmed. :contentReference[oaicite:14]{index=14}
        // We approximate: if settingsOK flag is false and a delay has passed since last connection, block.
        final long delay = cfg.getLong("settings.delay", 10_000L);
        return !st.settingsOK && (System.currentTimeMillis() - st.lastConnection >= delay);
    }

    private boolean checkRuntime(String ipStr) {
        if (!cfg.getBoolean("runtime.enabled", false)) return false;
        // We always perform runtime adds/removes via commands; decision to kick belongs to other modules.
        return true;
    }

    // ===================================
    // Helpers: punish / runtime / windows
    // ===================================

    private boolean punish(String type, String playerName, List<String> commands) {
        if (commands != null) {
            for (String cmd : commands) {
                String out = cmd
                        .replace("%player%", playerName)
                        .replace("%kick_" + type + "%", playerName); // supports your placeholders like %kick_accounts%
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), out);
            }
        }
        plugin.getLogger().info("[TG-AntiBot] Blocked " + playerName + " by " + type + " check");
        return true;
    }

    private void runRuntimeAdd(String ip) {
        final List<String> add = cfg.getStringList("runtime.add");
        final List<String> remove = cfg.getStringList("runtime.remove"); // keep for later if you need to undo
        for (String raw : add) {
            final String cmd = raw.replace("%address%", ip);
            try {
                Runtime.getRuntime().exec(cmd);
            } catch (Exception e) {
                plugin.getLogger().warning("Runtime exec failed: " + cmd + " (" + e.getMessage() + ")");
            }
        }
    }

    private static void pushToWindow(Deque<Long> dq, long now, long windowMs) {
        dq.addLast(now);
        while (!dq.isEmpty() && (now - dq.peekFirst()) > windowMs) dq.removeFirst();
    }

    private static int countInWindow(Deque<Long> dq, long windowMs) {
        final long now = System.currentTimeMillis();
        while (!dq.isEmpty() && (now - dq.peekFirst()) > windowMs) dq.removeFirst();
        return dq.size();
    }

    // ===================================
    // Optional mutators you can call from other places (commands/listeners)
    // ===================================

    public void setWhitelisted(String ip, boolean value) {
        if (value) {
            blacklist.remove(ip);
            whitelist.add(ip);
        } else {
            whitelist.remove(ip);
        }
    }

    public void setBlacklisted(String ip, boolean value) {
        if (value) {
            whitelist.remove(ip);
            blacklist.add(ip);
            // also add to runtime if enabled (like ArkFlame Blacklist->Runtime). :contentReference[oaicite:15]{index=15}
            if (cfg.getBoolean("runtime.enabled", false)) runRuntimeAdd(ip);
        } else {
            blacklist.remove(ip);
        }
    }

    // If you add a settings handshake elsewhere, call this to mark OK.
    public void setSettingsOk(String ip, boolean ok) {
        ipMap.computeIfAbsent(ip, k -> new IpState()).settingsOK = ok;
    }

    // For testing/cleanup
    public void clearState(String ip) {
        ipMap.remove(ip);
    }
}
