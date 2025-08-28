package org.spigot.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spigot.Main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class Messages {

    private final Main plugin;
    private FileConfiguration config;
    private File file;
    private final ConcurrentMap<String, String> messageCache;
    private volatile long lastReload;
    
    // Common placeholder patterns for better performance
    private static final Pattern PLAYER_PATTERN = Pattern.compile("\\{player\\}", Pattern.LITERAL);
    private static final Pattern IP_PATTERN = Pattern.compile("\\{ip\\}", Pattern.LITERAL);
    private static final Pattern TYPE_PATTERN = Pattern.compile("\\{type\\}", Pattern.LITERAL);
    private static final Pattern INTENSITY_PATTERN = Pattern.compile("\\{intensity\\}", Pattern.LITERAL);
    private static final Pattern MODE_PATTERN = Pattern.compile("\\{mode\\}", Pattern.LITERAL);

    public Messages(Main plugin) {
        this.plugin = plugin;
        this.messageCache = new ConcurrentHashMap<>();
        this.lastReload = System.currentTimeMillis();
        load();
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        
        // Create default messages.yml if it doesn't exist
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(file);
        messageCache.clear();
        lastReload = System.currentTimeMillis();
        
        // Validate critical messages
        validateMessages();
    }

    public void reload() {
        load();
    }
    
    private void validateMessages() {
        // Check for critical messages and provide defaults if missing
        String[] criticalMessages = {
            "system-prefix",
            "access-denied", 
            "ip-blocked",
            "vpn-kick-message",
            "join-blocked-line1",
            "join-blocked-line2"
        };
        
        boolean needsSave = false;
        for (String key : criticalMessages) {
            if (!config.contains(key)) {
                plugin.getLogger().warning("Missing message key: " + key + ", adding default");
                config.set(key, getDefaultMessage(key));
                needsSave = true;
            }
        }
        
        if (needsSave) {
            save();
        }
    }
    
    private String getDefaultMessage(String key) {
        switch (key) {
            case "system-prefix":
                return "&7[&c&lTG-AntiBot&7] ";
            case "access-denied":
                return "&cAccess denied! Insufficient permissions.";
            case "ip-blocked":
                return "&cYour IP has been blocked from this server.";
            case "vpn-kick-message":
                return "&cVPN/Proxy connections are not allowed on this server!";
            case "join-blocked-line1":
                return "&cYou cannot join the server right now.";
            case "join-blocked-line2":
                return "&7Please wait a few minutes before trying again.";
            default:
                return "&cMissing message: " + key;
        }
    }

    public String get(String path) {
        return get(path, "&cMissing message: " + path);
    }

    public String get(String path, String def) {
        // Use cache for better performance
        String cacheKey = path + ":" + def;
        return messageCache.computeIfAbsent(cacheKey, k -> {
            String message = config.getString(path, def);
            if (message == null) {
                plugin.getLogger().warning("Null message for path: " + path + ", using default");
                message = def;
            }
            return message.replace("&", "§");
        });
    }
    
    /**
     * Get message with placeholder replacement
     */
    public String get(String path, String def, String player, String ip, String type, String intensity, String mode) {
        String message = get(path, def);
        
        // Replace placeholders efficiently
        if (player != null) {
            message = PLAYER_PATTERN.matcher(message).replaceAll(player);
        }
        if (ip != null) {
            message = IP_PATTERN.matcher(message).replaceAll(ip);
        }
        if (type != null) {
            message = TYPE_PATTERN.matcher(message).replaceAll(type);
        }
        if (intensity != null) {
            message = INTENSITY_PATTERN.matcher(message).replaceAll(intensity);
        }
        if (mode != null) {
            message = MODE_PATTERN.matcher(message).replaceAll(mode);
        }
        
        return message;
    }
    
    /**
     * Get message with single placeholder replacement
     */
    public String get(String path, String placeholder, String value) {
        String message = get(path);
        if (placeholder != null && value != null) {
            message = message.replace(placeholder, value);
        }
        return message;
    }
    
    /**
     * Check if a message path exists
     */
    public boolean exists(String path) {
        return config.contains(path);
    }
    
    /**
     * Get all message keys
     */
    public java.util.Set<String> getKeys() {
        return config.getKeys(true);
    }
    
    /**
     * Clear the message cache
     */
    public void clearCache() {
        messageCache.clear();
        plugin.getLogger().info("Message cache cleared");
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("Cache size: %d, Last reload: %d seconds ago",
            messageCache.size(),
            (System.currentTimeMillis() - lastReload) / 1000);
    }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save messages.yml: " + e.getMessage());
            if (plugin.getConfigManager().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
}
