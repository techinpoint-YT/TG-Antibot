package org.spigot.core.data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PlayerProfile {
    private final String ip;
    private final Set<String> nicknames;
    private final List<Long> connectionTimes;
    private final List<Long> pingTimes;
    private final AtomicLong totalPlayTime;
    private long firstConnection;
    private volatile long lastConnection;
    private final AtomicInteger suspicionScore;
    
    // Constants for memory management
    private static final int MAX_CONNECTION_HISTORY = 50;
    private static final int MAX_PING_HISTORY = 100;
    private static final int MAX_NICKNAMES = 10;

    public PlayerProfile(String ip) {
        this.ip = ip;
        this.nicknames = ConcurrentHashMap.newKeySet();
        this.connectionTimes = new CopyOnWriteArrayList<>();
        this.pingTimes = new CopyOnWriteArrayList<>();
        this.totalPlayTime = new AtomicLong(0);
        this.firstConnection = System.currentTimeMillis();
        this.lastConnection = System.currentTimeMillis();
        this.suspicionScore = new AtomicInteger(0);
    }

    public synchronized void addConnection(String nickname, long timestamp) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return;
        }
        
        nicknames.add(nickname);
        connectionTimes.add(timestamp);
        lastConnection = timestamp;
        
        // Keep only recent connections to prevent memory issues
        while (connectionTimes.size() > MAX_CONNECTION_HISTORY) {
            connectionTimes.remove(0);
        }
        
        // Limit nickname history
        if (nicknames.size() > MAX_NICKNAMES) {
            // Remove oldest nickname (this is approximate since Set doesn't maintain order)
            Iterator<String> iterator = nicknames.iterator();
            if (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }
        }
    }

    public synchronized void addPing(long timestamp) {
        pingTimes.add(timestamp);
        
        // Keep only recent pings to prevent memory issues
        while (pingTimes.size() > MAX_PING_HISTORY) {
            pingTimes.remove(0);
        }
    }

    public void addPlayTime(long milliseconds) {
        if (milliseconds > 0) {
            totalPlayTime.addAndGet(milliseconds);
        }
    }

    public void increaseSuspicion(int points) {
        if (points > 0) {
            suspicionScore.addAndGet(points);
        }
    }

    public void decreaseSuspicion(int points) {
        if (points > 0) {
            suspicionScore.updateAndGet(current -> Math.max(0, current - points));
        }
    }

    // Connection analysis methods
    public long getAverageConnectionInterval() {
        List<Long> connections = new ArrayList<>(connectionTimes);
        if (connections.size() < 2) return 0;
        
        long totalInterval = 0;
        for (int i = 1; i < connections.size(); i++) {
            totalInterval += connections.get(i) - connections.get(i - 1);
        }
        return totalInterval / (connections.size() - 1);
    }

    public int getConnectionsInTimeframe(long timeframe) {
        if (timeframe <= 0) return 0;
        
        long cutoff = System.currentTimeMillis() - timeframe;
        return (int) connectionTimes.stream()
            .filter(time -> time > cutoff)
            .count();
    }

    public int getPingsInTimeframe(long timeframe) {
        if (timeframe <= 0) return 0;
        
        long cutoff = System.currentTimeMillis() - timeframe;
        return (int) pingTimes.stream()
            .filter(time -> time > cutoff)
            .count();
    }

    public boolean hasRapidReconnections() {
        return getConnectionsInTimeframe(10000) >= 3; // 3+ connections within 10 seconds
    }

    public boolean hasSuspiciousNicknamePattern() {
        if (nicknames.size() < 2) return false;
        
        // Check for similar patterns in nicknames
        List<String> nicknameList = new ArrayList<>(nicknames);
        for (int i = 0; i < nicknameList.size(); i++) {
            for (int j = i + 1; j < nicknameList.size(); j++) {
                if (areSimilarNicknames(nicknameList.get(i), nicknameList.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean areSimilarNicknames(String name1, String name2) {
        if (name1 == null || name2 == null) return false;
        if (name1.equals(name2)) return true;
        
        // Check for similar length and pattern
        if (Math.abs(name1.length() - name2.length()) > 2) return false;
        
        // Simple Levenshtein distance check
        int distance = calculateLevenshteinDistance(name1.toLowerCase(), name2.toLowerCase());
        return distance <= 2; // Allow up to 2 character differences
    }
    
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Clean up old data to prevent memory leaks
     */
    public synchronized void cleanup(long retentionTime) {
        long cutoff = System.currentTimeMillis() - retentionTime;
        
        // Remove old connection times
        connectionTimes.removeIf(time -> time < cutoff);
        
        // Remove old ping times
        pingTimes.removeIf(time -> time < cutoff);
        
        // If no recent activity, reset suspicion score gradually
        if (connectionTimes.isEmpty() || (lastConnection < cutoff)) {
            decreaseSuspicion(1);
        }
    }

    // Getters
    public String getIp() { return ip; }
    public Set<String> getNicknames() { return new HashSet<>(nicknames); }
    public int getConnectionCount() { return connectionTimes.size(); }
    public long getPlayTime() { return totalPlayTime.get(); }
    public long getFirstConnection() { return firstConnection; }
    public long getLastConnection() { return lastConnection; }
    public int getSuspicionScore() { return suspicionScore.get(); }
    public List<Long> getConnectionTimes() { return new ArrayList<>(connectionTimes); }
    public List<Long> getPingTimes() { return new ArrayList<>(pingTimes); }
    
    /**
     * Get memory usage statistics for this profile
     */
    public String getMemoryStats() {
        return String.format("Nicknames: %d/%d, Connections: %d/%d, Pings: %d/%d",
            nicknames.size(), MAX_NICKNAMES,
            connectionTimes.size(), MAX_CONNECTION_HISTORY,
            pingTimes.size(), MAX_PING_HISTORY);
    }
}