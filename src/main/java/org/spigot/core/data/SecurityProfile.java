package org.spigot.core.data;

import java.util.*;

public class SecurityProfile {
    private final String ip;
    private final List<String> actionHistory;
    private final List<Long> connectionTimestamps;
    private int riskScore;
    private long lastUpdate;
    private String lastKnownLocation;
    private boolean hasGeographicAnomalies;
    private boolean hasSuspiciousTimingPattern;

    public SecurityProfile(String ip) {
        this.ip = ip;
        this.actionHistory = new ArrayList<>();
        this.connectionTimestamps = new ArrayList<>();
        this.riskScore = 0;
        this.lastUpdate = System.currentTimeMillis();
        this.hasGeographicAnomalies = false;
        this.hasSuspiciousTimingPattern = false;
    }

    public void recordAction(String action, long timestamp) {
        actionHistory.add(action);
        lastUpdate = timestamp;
        
        // Keep only last 100 actions
        while (actionHistory.size() > 100) {
            actionHistory.remove(0);
        }
        
        analyzeTimingPatterns();
    }

    public void recordConnection(long timestamp) {
        connectionTimestamps.add(timestamp);
        
        // Keep only last 50 connections
        while (connectionTimestamps.size() > 50) {
            connectionTimestamps.remove(0);
        }
        
        analyzeTimingPatterns();
    }

    private void analyzeTimingPatterns() {
        if (connectionTimestamps.size() < 3) return;
        
        // Check for regular intervals (bot-like behavior)
        List<Long> intervals = new ArrayList<>();
        for (int i = 1; i < connectionTimestamps.size(); i++) {
            intervals.add(connectionTimestamps.get(i) - connectionTimestamps.get(i - 1));
        }
        
        // Calculate standard deviation of intervals
        double mean = intervals.stream().mapToLong(Long::longValue).average().orElse(0);
        double variance = intervals.stream()
            .mapToDouble(interval -> Math.pow(interval - mean, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        // If standard deviation is very low, it might be bot behavior
        hasSuspiciousTimingPattern = stdDev < 1000; // Less than 1 second variation
    }

    public int getConnectionsInLastHour() {
        long oneHourAgo = System.currentTimeMillis() - 3600000;
        return (int) connectionTimestamps.stream()
            .filter(timestamp -> timestamp > oneHourAgo)
            .count();
    }

    public void updateRiskScore(int newScore) {
        this.riskScore = newScore;
        this.lastUpdate = System.currentTimeMillis();
    }

    public void setGeographicAnomalies(boolean hasAnomalies) {
        this.hasGeographicAnomalies = hasAnomalies;
    }

    public void setLastKnownLocation(String location) {
        this.lastKnownLocation = location;
    }

    // Getters
    public String getIp() { return ip; }
    public List<String> getActionHistory() { return new ArrayList<>(actionHistory); }
    public int getRiskScore() { return riskScore; }
    public long getLastUpdate() { return lastUpdate; }
    public String getLastKnownLocation() { return lastKnownLocation; }
    public boolean hasGeographicAnomalies() { return hasGeographicAnomalies; }
    public boolean hasSuspiciousTimingPattern() { return hasSuspiciousTimingPattern; }
}