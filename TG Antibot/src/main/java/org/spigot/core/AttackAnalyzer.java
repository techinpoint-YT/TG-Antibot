package org.spigot.core;

import org.bukkit.Bukkit;
import org.spigot.Main;
import org.spigot.core.data.AttackLog;
import org.spigot.enums.AttackType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class AttackAnalyzer {

    private final Main plugin;
    private final Queue<AttackLog> attackHistory;
    private final Map<AttackType, Integer> attackCounts;
    private final Map<String, Integer> ipAttackCounts;

    private final AtomicLong totalAttacksBlocked;
    private final AtomicLong totalConnectionsAnalyzed;

    // Pattern detection
    private final Map<String, List<Long>> ipTimestamps;
    private final Set<String> notifiedIPs;

    public AttackAnalyzer(Main plugin) {
        this.plugin = plugin;
        this.attackHistory = new ConcurrentLinkedQueue<>();
        this.attackCounts = new ConcurrentHashMap<>();
        this.ipAttackCounts = new ConcurrentHashMap<>();
        this.totalAttacksBlocked = new AtomicLong(0);
        this.totalConnectionsAnalyzed = new AtomicLong(0);
        this.ipTimestamps = new ConcurrentHashMap<>();
        this.notifiedIPs = ConcurrentHashMap.newKeySet();

        startAnalysisTask();
    }

    public void recordAttack(AttackType type, String sourceIP, long intensity) {
        long currentTime = System.currentTimeMillis();
        AttackLog log = new AttackLog(type, sourceIP, intensity, currentTime);
        attackHistory.offer(log);

        // Update counters thread-safely
        attackCounts.merge(type, 1, Integer::sum);
        ipAttackCounts.merge(sourceIP, 1, Integer::sum);
        totalAttacksBlocked.incrementAndGet();

        // Track IP timestamps for pattern detection
        ipTimestamps.computeIfAbsent(sourceIP, k -> new ArrayList<>()).add(currentTime);

        // Keep only last 1000 attacks to prevent memory issues
        while (attackHistory.size() > 1000) {
            attackHistory.poll();
        }

        // Analyze patterns
        analyzeAttackPatterns(log);

        // Clean old timestamps periodically
        cleanOldTimestamps(sourceIP, currentTime);
    }

    public void recordConnectionAnalysis() {
        totalConnectionsAnalyzed.incrementAndGet();
    }

    private void analyzeAttackPatterns(AttackLog newAttack) {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;

        // Check for coordinated attacks using stream with collect instead of toList()
        long recentAttacks = attackHistory.stream()
                .filter(log -> log.getTimestamp() > oneMinuteAgo)
                .count();

        if (recentAttacks > plugin.getConfigManager().getCoordinatedAttackThreshold()) {
            plugin.getLogger().warning("§c[PATTERN DETECTED] Coordinated attack detected! " +
                    recentAttacks + " attacks in the last minute.");

            // Trigger enhanced protection mode
            triggerEnhancedProtection();
        }

        // Check for repeat offenders
        String sourceIP = newAttack.getSourceIP();
        int ipAttacks = ipAttackCounts.getOrDefault(sourceIP, 0);

        if (ipAttacks > plugin.getConfigManager().getRepeatOffenderThreshold()) {
            if (!notifiedIPs.contains(sourceIP)) {
                plugin.getBotProtectionManager().addToBlacklist(sourceIP,
                        "Repeat offender - " + ipAttacks + " attacks");
                notifiedIPs.add(sourceIP);

                plugin.getLogger().warning("§c[REPEAT OFFENDER] IP " + sourceIP +
                        " blocked after " + ipAttacks + " attacks");
            }
        }

        // Check for rapid-fire attacks from single IP
        analyzeRapidFirePattern(sourceIP, currentTime);
    }

    private void analyzeRapidFirePattern(String sourceIP, long currentTime) {
        List<Long> timestamps = ipTimestamps.get(sourceIP);
        if (timestamps == null || timestamps.size() < 5) return;

        // Check if last 5 attacks were within 10 seconds
        List<Long> recentTimestamps = timestamps.stream()
                .filter(t -> currentTime - t < 10000)
                .collect(Collectors.toList());

        if (recentTimestamps.size() >= 5) {
            plugin.getLogger().warning("§c[RAPID FIRE] Detected rapid-fire attack from " + sourceIP);
            plugin.getBotProtectionManager().addToBlacklist(sourceIP, "Rapid-fire attack pattern");
        }
    }

    private void cleanOldTimestamps(String sourceIP, long currentTime) {
        List<Long> timestamps = ipTimestamps.get(sourceIP);
        if (timestamps != null && timestamps.size() > 20) {
            // Remove timestamps older than 1 hour
            long oneHourAgo = currentTime - 3600000;
            timestamps.removeIf(timestamp -> timestamp < oneHourAgo);

            // Remove empty entries
            if (timestamps.isEmpty()) {
                ipTimestamps.remove(sourceIP);
                notifiedIPs.remove(sourceIP);
            }
        }
    }

    private void triggerEnhancedProtection() {
        // Could trigger temporary stricter rules
        plugin.getLogger().info("§e[PROTECTION] Enhanced protection mode activated");
        // Implementation depends on your protection system
    }

    private void startAnalysisTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::generateHourlyReport,
                72000L, 72000L); // Every hour (72000 ticks = 1 hour)
    }

    private void generateHourlyReport() {
        long currentTime = System.currentTimeMillis();
        long hourAgo = currentTime - 3600000; // 1 hour ago

        // Use collect() instead of toList() for Java 8+ compatibility
        List<AttackLog> recentAttacks = attackHistory.stream()
                .filter(log -> log.getTimestamp() > hourAgo)
                .collect(Collectors.toList());

        if (recentAttacks.isEmpty()) {
            plugin.getLogger().info("§e[HOURLY REPORT] No attacks detected in the last hour");
            return;
        }

        // Calculate statistics
        Map<AttackType, Long> hourlyStats = new HashMap<>();
        Map<String, Long> topAttackers = new HashMap<>();

        for (AttackLog log : recentAttacks) {
            hourlyStats.merge(log.getType(), 1L, Long::sum);
            topAttackers.merge(log.getSourceIP(), 1L, Long::sum);
        }

        // Log the report
        plugin.getLogger().info("§e[HOURLY REPORT] === Attack Summary ===");
        plugin.getLogger().info("§7Total attacks blocked: §c" + recentAttacks.size());

        plugin.getLogger().info("§7Attack types:");
        for (Map.Entry<AttackType, Long> entry : hourlyStats.entrySet()) {
            plugin.getLogger().info("§7- " + entry.getKey().getDisplayName() + ": §c" + entry.getValue());
        }

        // Show top 3 attacking IPs
        List<Map.Entry<String, Long>> sortedAttackers = topAttackers.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        if (!sortedAttackers.isEmpty()) {
            plugin.getLogger().info("§7Top attacking IPs:");
            for (int i = 0; i < sortedAttackers.size(); i++) {
                Map.Entry<String, Long> entry = sortedAttackers.get(i);
                plugin.getLogger().info("§7" + (i + 1) + ". " + entry.getKey() + ": §c" + entry.getValue() + " attacks");
            }
        }

        plugin.getLogger().info("§e[HOURLY REPORT] === End Summary ===");
    }

    public List<AttackLog> getRecentAttacks(int limit) {
        // Use collect() instead of toList() for Java 8+ compatibility
        return attackHistory.stream()
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Map<AttackType, Integer> getAttackStatistics() {
        return new HashMap<>(attackCounts);
    }

    public Map<String, Integer> getTopAttackingIPs(int limit) {
        return ipAttackCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public List<AttackLog> getAttacksByType(AttackType type, int limit) {
        return attackHistory.stream()
                .filter(log -> log.getType() == type)
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<AttackLog> getAttacksByIP(String sourceIP, int limit) {
        return attackHistory.stream()
                .filter(log -> log.getSourceIP().equals(sourceIP))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long getTotalAttacksBlocked() {
        return totalAttacksBlocked.get();
    }

    public long getTotalConnectionsAnalyzed() {
        return totalConnectionsAnalyzed.get();
    }

    public double getBlockRate() {
        long analyzed = totalConnectionsAnalyzed.get();
        if (analyzed == 0) return 0.0;
        return (double) totalAttacksBlocked.get() / analyzed * 100.0;
    }

    public void clearStatistics() {
        attackHistory.clear();
        attackCounts.clear();
        ipAttackCounts.clear();
        ipTimestamps.clear();
        notifiedIPs.clear();
        totalAttacksBlocked.set(0);
        totalConnectionsAnalyzed.set(0);

        plugin.getLogger().info("§aAttack analyzer statistics cleared");
    }

    public int getUniqueAttackingIPs() {
        return ipAttackCounts.size();
    }

    public long getAttacksInLastMinute() {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000;

        return attackHistory.stream()
                .filter(log -> log.getTimestamp() > oneMinuteAgo)
                .count();
    }

    public void shutdown() {
        // Save statistics to file if needed
        plugin.getLogger().info("§7Attack analyzer shutting down...");
        plugin.getLogger().info("§7Final statistics - Total attacks blocked: " + getTotalAttacksBlocked());
        plugin.getLogger().info("§7Total connections analyzed: " + getTotalConnectionsAnalyzed());
        plugin.getLogger().info("§7Block rate: " + String.format("%.2f%%", getBlockRate()));
    }
}