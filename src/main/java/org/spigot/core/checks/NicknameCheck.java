package org.spigot.core.checks;

import org.spigot.Main;

import java.util.List;
import java.util.regex.Pattern;

public class NicknameCheck {
    private final Main plugin;
    private final Pattern suspiciousPattern;

    public NicknameCheck(Main plugin) {
        this.plugin = plugin;
        // Common bot nickname patterns
        this.suspiciousPattern = Pattern.compile(
            "^(Player|Bot|Test|Spam|Hack|Cheat)\\d+$|" +
            "^[a-zA-Z]{1,3}\\d{4,}$|" +
            "^\\w*(bot|hack|spam|test)\\w*$",
            Pattern.CASE_INSENSITIVE
        );
    }

    public boolean shouldBlock(String nickname) {
        if (!plugin.getConfigManager().isNicknameCheckEnabled()) {
            return false;
        }

        // Check blacklisted nicknames
        List<String> blacklistedNames = plugin.getConfigManager().getBlacklistedNicknames();
        for (String blacklisted : blacklistedNames) {
            if (nickname.toLowerCase().contains(blacklisted.toLowerCase())) {
                plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Contains blacklisted word: " + blacklisted);
                return true;
            }
        }

        // Check suspicious patterns
        if (suspiciousPattern.matcher(nickname).find()) {
            plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Matches suspicious pattern");
            return true;
        }

        // Check for non-ASCII characters if configured
        if (plugin.getConfigManager().isBlockNonAsciiNicknames() && !isAscii(nickname)) {
            plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Contains non-ASCII characters");
            return true;
        }

        return false;
    }

    private boolean isAscii(String str) {
        return str.chars().allMatch(c -> c < 128);
    }
}