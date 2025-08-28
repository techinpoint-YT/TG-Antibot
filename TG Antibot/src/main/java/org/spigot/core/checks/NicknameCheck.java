package org.spigot.core.checks;

import org.spigot.Main;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class NicknameCheck {
    private final Main plugin;
    private final Pattern suspiciousPattern;
    private final Pattern asciiPattern;

    public NicknameCheck(Main plugin) {
        this.plugin = plugin;
        
        // Initialize patterns with error handling
        Pattern tempSuspiciousPattern;
        Pattern tempAsciiPattern;
        
        try {
            // Common bot nickname patterns
            tempSuspiciousPattern = Pattern.compile(
                "^(Player|Bot|Test|Spam|Hack|Cheat|Guest|User)\\d+$|" +
                "^[a-zA-Z]{1,3}\\d{4,}$|" +
                "^\\w*(bot|hack|spam|test|cheat|exploit)\\w*$|" +
                "^[a-zA-Z]+_\\d+$|" +
                "^\\d+[a-zA-Z]+\\d+$",
                Pattern.CASE_INSENSITIVE
            );
        } catch (PatternSyntaxException e) {
            plugin.getLogger().severe("Invalid suspicious nickname pattern: " + e.getMessage());
            tempSuspiciousPattern = Pattern.compile("^(Bot|Test)\\d+$", Pattern.CASE_INSENSITIVE);
        }
        
        try {
            tempAsciiPattern = Pattern.compile("^[\\x00-\\x7F]*$");
        } catch (PatternSyntaxException e) {
            plugin.getLogger().severe("Invalid ASCII pattern: " + e.getMessage());
            tempAsciiPattern = null;
        }
        
        this.suspiciousPattern = tempSuspiciousPattern;
        this.asciiPattern = tempAsciiPattern;
    }

    public boolean shouldBlock(String nickname) {
        if (!plugin.getConfigManager().isNicknameCheckEnabled()) {
            return false;
        }
        
        if (nickname == null || nickname.trim().isEmpty()) {
            plugin.getLogger().warning("NicknameCheck: null or empty nickname provided");
            return true;
        }
        
        // Sanitize nickname
        nickname = nickname.trim();
        
        // Check length limits
        if (nickname.length() < 3 || nickname.length() > 16) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Invalid length: " + nickname.length());
            }
            return true;
        }

        // Check blacklisted nicknames
        List<String> blacklistedNames = plugin.getConfigManager().getBlacklistedNicknames();
        for (String blacklisted : blacklistedNames) {
            if (blacklisted == null || blacklisted.trim().isEmpty()) continue;
            if (nickname.toLowerCase().contains(blacklisted.toLowerCase())) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Contains blacklisted word: " + blacklisted);
                }
                return true;
            }
        }

        // Check suspicious patterns
        try {
            if (suspiciousPattern.matcher(nickname).find()) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Matches suspicious pattern");
                }
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking suspicious pattern for nickname " + nickname + ": " + e.getMessage());
        }

        // Check for non-ASCII characters if configured
        if (plugin.getConfigManager().isBlockNonAsciiNicknames()) {
            if (!isAscii(nickname)) {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Contains non-ASCII characters");
                }
                return true;
            }
        }
        
        // Check for excessive special characters
        if (hasExcessiveSpecialChars(nickname)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Excessive special characters");
            }
            return true;
        }
        
        // Check for repeated characters
        if (hasExcessiveRepeatedChars(nickname)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("§cBlocked nickname: " + nickname + " - Excessive repeated characters");
            }
            return true;
        }

        return false;
    }

    private boolean isAscii(String str) {
        if (asciiPattern != null) {
            return asciiPattern.matcher(str).matches();
        }
        // Fallback method
        return str.chars().allMatch(c -> c >= 32 && c < 127);
    }
    
    private boolean hasExcessiveSpecialChars(String nickname) {
        long specialCharCount = nickname.chars()
            .filter(c -> !Character.isLetterOrDigit(c) && c != '_')
            .count();
        return specialCharCount > nickname.length() / 2; // More than 50% special chars
    }
    
    private boolean hasExcessiveRepeatedChars(String nickname) {
        if (nickname.length() < 4) return false;
        
        int maxRepeated = 0;
        int currentRepeated = 1;
        char lastChar = nickname.charAt(0);
        
        for (int i = 1; i < nickname.length(); i++) {
            char currentChar = nickname.charAt(i);
            if (currentChar == lastChar) {
                currentRepeated++;
            } else {
                maxRepeated = Math.max(maxRepeated, currentRepeated);
                currentRepeated = 1;
                lastChar = currentChar;
            }
        }
        maxRepeated = Math.max(maxRepeated, currentRepeated);
        
        return maxRepeated > 3; // More than 3 consecutive identical characters
    }
}