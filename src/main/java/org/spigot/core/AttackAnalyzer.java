package org.spigot.core;

import org.bukkit.Bukkit;
import org.spigot.Main;
import org.spigot.core.data.AttackLog;
import org.spigot.enums.AttackType;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AttackAnalyzer {

    private final Main plugin;
    private final Queue<AttackLog> attackHistory;
    private final Map<AttackType, Integer> attackCounts;
    private final Map<String, Integer> ipAttackCounts;
    
    private long totalAttacksBlocked;
    private long totalConnectionsAnalyzed;

    public AttackAnalyzer(Main plugin) {
        this.plugin = plugin;
        this.attackHistory = new ConcurrentLinkedQueue<>();
        this.attackCounts = new HashMap<>();
        this.ipAttackCounts = new HashMap<>();
        this.totalAttacksBlocked = 0;
        this.totalConnectionsAnalyzed = 0;
        
        startAnalysisTask();
    }

    public void recordAttack(AttackType type, String sourceIP, long intensity) {
        AttackLog log = new AttackLog(type, sourceIP, intensity, System.currentTimeMillis());
        attackHistory.offer(log);
        
        // Update counters
        attackCounts.merge(type, 1, Integer::sum);
        ipAttackCounts.merge(sourceIP, 1, Integer::sum);
        totalAttacksBlocked++;
        
        // Keep only last 1000 attacks
        while (attackHistory.size() > 1000) {
            attackHistory.poll();
        }
        
        // Analyze patterns
        analyzeAttackPatterns(log);
    }

    public void recordConnectionAnalysis() {
        totalConnectionsAnalyzed++;
    }

    private void analyzeAttackPatterns(AttackLog newAttack) {
        // Check for coordinated attacks
        long recentAttacks = attackHistory.stream()
            .filter(log -> System.currentTimeMillis() - log.getTimestamp() < 60000) // Last minute
            .count();
            
        if (recentAttacks > plugin.getConfigManager().getCoordinatedAttackThreshold()) {
            plugin.getLogger().warning("§c[PATTERN DETECTED] Coordinated attack detected! " + 
                recentAttacks + " attacks in the last minute.");
        }
        
        // Check for repeat offenders
        String sourceIP = newAttack.getSourceIP();
        int ipAttacks = ipAttackCounts.getOrDefault(sourceIP, 0);
        
        if (ipAttacks > plugin.getConfigManager().getRepeatOffenderThreshold()) {
            plugin.getBotProtectionManager().addToBlacklist(sourceIP, 
                "Repeat offender - " + ipAttacks + " attacks");
        }
    }

    private void startAnalysisTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            generateHourlyReport();
        }, 72000L, 72000L); // Every hour
    }

    private void generateHourlyReport() {
        long currentTime = System.currentTimeMillis();
        long hourAgo = currentTime - 3600000; // 1 hour ago
        
        List<AttackLog> recentAttacks = attackHistory.stream()
            .filter(log -> log.getTimestamp() > hourAgo)
            .toList();
            
        if (recentAttacks.isEmpty()) return;
        
        Map<AttackType, Long> hourlyStats = new HashMap<>();
        for (AttackLog log : recentAttacks) {
            hourlyStats.merge(log.getType(), 1L, Long::sum);
        }
        
        plugin.getLogger().info("§e[HOURLY REPORT] Attacks blocked in the last hour:");
        for (Map.Entry<AttackType, Long> entry : hourlyStats.entrySet()) {
            plugin.getLogger().info("§7- " + entry.getKey().getDisplayName() + ": §c" + entry.getValue());
        }
    }

    public List<AttackLog> getRecentAttacks(int limit) {
        return attackHistory.stream()
            .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
            .limit(limit)
            .toList();
    }

    public Map<AttackType, Integer> getAttackStatistics() {
        return new HashMap<>(attackCounts);
    }

    public long getTotalAttacksBlocked() {
        return totalAttacksBlocked;
    }

    public long getTotalConnectionsAnalyzed() {
        return totalConnectionsAnalyzed;
    }

    public double getBlockRate() {
        if (totalConnectionsAnalyzed == 0) return 0.0;
        return (double) totalAttacksBlocked / totalConnectionsAnalyzed * 100.0;
    }

    public void shutdown() {
        // Save statistics if needed
    }
}