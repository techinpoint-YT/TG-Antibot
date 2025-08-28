package org.spigot.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.entity.Player;
import org.spigot.Main;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class VPNChecker {

    private final Main plugin;
    private final OkHttpClient httpClient;
    private final ObjectMapper jsonMapper;
    private final ConcurrentMap<String, CacheEntry> vpnCache;
    private final long cacheExpirationTime;
    
    private static class CacheEntry {
        final boolean isVPN;
        final long timestamp;
        
        CacheEntry(boolean isVPN) {
            this.isVPN = isVPN;
            this.timestamp = System.currentTimeMillis();
        }
        
        boolean isExpired(long expirationTime) {
            return System.currentTimeMillis() - timestamp > expirationTime;
        }
    }

    public VPNChecker(Main plugin) {
        this.plugin = plugin;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(plugin.getConfigManager().getRequestTimeout(), TimeUnit.MILLISECONDS)
            .readTimeout(plugin.getConfigManager().getRequestTimeout(), TimeUnit.MILLISECONDS)
            .writeTimeout(plugin.getConfigManager().getRequestTimeout(), TimeUnit.MILLISECONDS)
            .build();
        this.jsonMapper = new ObjectMapper();
        this.vpnCache = new ConcurrentHashMap<>();
        this.cacheExpirationTime = TimeUnit.MINUTES.toMillis(plugin.getConfigManager().getCacheLifetimeMinutes());
    }

    /**
     * Check if a Player is using a VPN.
     */
    public boolean isUsingVPN(Player player) {
        if (player == null || player.getAddress() == null) {
            return false;
        }
        return isUsingVPN(player.getAddress().getAddress().getHostAddress());
    }

    /**
     * Check if an IP address is using a VPN (for AsyncPlayerPreLoginEvent or raw IP checks).
     */
    public boolean isUsingVPN(String ip) {
        if (!plugin.getConfigManager().isVpnProtectionEnabled()) {
            return false;
        }
        
        if (ip == null || ip.trim().isEmpty()) {
            plugin.getLogger().warning("[VPNChecker] Invalid IP address provided: " + ip);
            return false;
        }
        
        // Normalize IP address
        ip = ip.replace("/", "").trim();
        
        // Check if IP is whitelisted
        if (plugin.getConfigManager().getSafeAddresses().contains(ip)) {
            return false;
        }
        
        // Check cache first
        CacheEntry cached = vpnCache.get(ip);
        if (cached != null && !cached.isExpired(cacheExpirationTime)) {
            if (plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().info("[VPNChecker] Using cached result for IP: " + ip + " -> " + cached.isVPN);
            }
            return cached.isVPN;
        }

        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[VPNChecker] Checking IP: " + ip);
        }

        boolean isVPN = false;
        try {
            isVPN = checkVPNWithAPI(ip);
            
            // Cache the result
            vpnCache.put(ip, new CacheEntry(isVPN));
            
            // Clean up expired cache entries periodically
            if (vpnCache.size() > 1000) {
                cleanupCache();
            }
            
        } catch (IOException e) {
            plugin.getLogger().warning("[VPNChecker] Error checking VPN for IP " + ip + ": " + e.getMessage());
            if (plugin.getConfigManager().isDebugMode()) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[VPNChecker] Unexpected error for IP " + ip + ": " + e.getMessage());
            if (plugin.getConfigManager().isDebugMode()) {
                e.printStackTrace();
            }
        }

        return isVPN;
    }
    
    private boolean checkVPNWithAPI(String ip) throws IOException {
        String apiKey = plugin.getConfigManager().getVpnApiKey();
        String url = "https://proxycheck.io/v2/" + ip + "?vpn=1&asn=1" +
                (apiKey.isEmpty() ? "" : "&key=" + apiKey);

        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "TG-AntiBot/" + plugin.getDescription().getVersion())
            .get()
            .build();
            
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                plugin.getLogger().warning("[VPNChecker] API request failed with code: " + response.code());
                return false;
            }

            ResponseBody body = response.body();
            if (body == null) {
                plugin.getLogger().warning("[VPNChecker] Empty response body from API");
                return false;
            }
            
            String responseText = body.string();
            if (responseText.trim().isEmpty()) {
                plugin.getLogger().warning("[VPNChecker] Empty response from API");
                return false;
            }
            
            JsonNode json = jsonMapper.readTree(responseText);

            if (json.has(ip)) {
                JsonNode ipNode = json.get(ip);
                String proxy = ipNode.path("proxy").asText();
                
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[VPNChecker] API response for " + ip + ": proxy=" + proxy);
                }
                
                return "yes".equalsIgnoreCase(proxy);
            } else {
                if (plugin.getConfigManager().isDebugMode()) {
                    plugin.getLogger().info("[VPNChecker] No data found for IP: " + ip);
                }
            }
        }
        
        return false;
    }
    
    private void cleanupCache() {
        vpnCache.entrySet().removeIf(entry -> entry.getValue().isExpired(cacheExpirationTime));
        
        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[VPNChecker] Cache cleanup completed. Current size: " + vpnCache.size());
        }
    }
    
    /**
     * Clear the VPN cache
     */
    public void clearCache() {
        vpnCache.clear();
        plugin.getLogger().info("[VPNChecker] VPN cache cleared");
    }
    
    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        long expired = vpnCache.values().stream()
            .mapToLong(entry -> entry.isExpired(cacheExpirationTime) ? 1 : 0)
            .sum();
            
        return String.format("Cache size: %d, Expired: %d, Valid: %d", 
            vpnCache.size(), expired, vpnCache.size() - expired);
    }
    
    /**
     * Shutdown the VPN checker and cleanup resources
     */
    public void shutdown() {
        vpnCache.clear();
        // OkHttpClient will be garbage collected
    }
}
