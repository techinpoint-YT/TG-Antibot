package org.spigot.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.spigot.TGAntiBotPlugin;

import java.util.concurrent.TimeUnit;

/**
 * Advanced caching system using Caffeine
 * Provides high-performance caching for various data types
 */
public class CacheManager {

    private final TGAntiBotPlugin plugin;
    
    // VPN/Proxy check cache
    private final Cache<String, Boolean> vpnCache;
    
    // Player reputation cache
    private final Cache<String, Integer> reputationCache;
    
    // Rate limiting cache
    private final Cache<String, Long> rateLimitCache;
    
    // GeoIP cache
    private final Cache<String, String> geoCache;

    public CacheManager(TGAntiBotPlugin plugin) {
        this.plugin = plugin;
        
        int cacheSize = plugin.getConfig().getInt("advanced.cache-size", 10000);
        int cacheDuration = plugin.getConfig().getInt("advanced.cache-duration-minutes", 60);
        
        this.vpnCache = Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(cacheDuration, TimeUnit.MINUTES)
            .recordStats()
            .build();
            
        this.reputationCache = Caffeine.newBuilder()
            .maximumSize(cacheSize / 2)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();
            
        this.rateLimitCache = Caffeine.newBuilder()
            .maximumSize(cacheSize * 2)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
            
        this.geoCache = Caffeine.newBuilder()
            .maximumSize(cacheSize)
            .expireAfterWrite(24, TimeUnit.HOURS)
            .recordStats()
            .build();
    }

    // VPN Cache Methods
    public Boolean getVPNStatus(String ip) {
        return vpnCache.getIfPresent(ip);
    }

    public void cacheVPNStatus(String ip, boolean isVPN) {
        vpnCache.put(ip, isVPN);
    }

    // Reputation Cache Methods
    public Integer getReputation(String ip) {
        return reputationCache.getIfPresent(ip);
    }

    public void cacheReputation(String ip, int reputation) {
        reputationCache.put(ip, reputation);
    }

    // Rate Limit Cache Methods
    public Long getLastAction(String key) {
        return rateLimitCache.getIfPresent(key);
    }

    public void recordAction(String key, long timestamp) {
        rateLimitCache.put(key, timestamp);
    }

    // GeoIP Cache Methods
    public String getCountry(String ip) {
        return geoCache.getIfPresent(ip);
    }

    public void cacheCountry(String ip, String country) {
        geoCache.put(ip, country);
    }

    // Cache Statistics
    public void logCacheStats() {
        plugin.getLogger().info("§e[Cache] VPN Cache - Size: " + vpnCache.estimatedSize() + 
            ", Hit Rate: " + String.format("%.2f%%", vpnCache.stats().hitRate() * 100));
        plugin.getLogger().info("§e[Cache] Reputation Cache - Size: " + reputationCache.estimatedSize() + 
            ", Hit Rate: " + String.format("%.2f%%", reputationCache.stats().hitRate() * 100));
        plugin.getLogger().info("§e[Cache] Rate Limit Cache - Size: " + rateLimitCache.estimatedSize() + 
            ", Hit Rate: " + String.format("%.2f%%", rateLimitCache.stats().hitRate() * 100));
        plugin.getLogger().info("§e[Cache] GeoIP Cache - Size: " + geoCache.estimatedSize() + 
            ", Hit Rate: " + String.format("%.2f%%", geoCache.stats().hitRate() * 100));
    }

    public void clearAllCaches() {
        vpnCache.invalidateAll();
        reputationCache.invalidateAll();
        rateLimitCache.invalidateAll();
        geoCache.invalidateAll();
        
        plugin.getLogger().info("§a[Cache] All caches cleared");
    }

    public void clearCache(String cacheType) {
        switch (cacheType.toLowerCase()) {
            case "vpn":
                vpnCache.invalidateAll();
                break;
            case "reputation":
                reputationCache.invalidateAll();
                break;
            case "ratelimit":
                rateLimitCache.invalidateAll();
                break;
            case "geo":
                geoCache.invalidateAll();
                break;
            default:
                plugin.getLogger().warning("§c[Cache] Unknown cache type: " + cacheType);
                return;
        }
        
        plugin.getLogger().info("§a[Cache] " + cacheType + " cache cleared");
    }
}