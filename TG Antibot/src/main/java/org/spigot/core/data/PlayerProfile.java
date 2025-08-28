package org.spigot.core.data;

import java.util.*;

public class PlayerProfile {
    private final String ip;
    private final Set<String> nicknames;
    private final List<Long> connectionTimes;
    private final List<Long> pingTimes;
    private long totalPlayTime;
    private long firstConnection;
    private long lastConnection;
    private int suspicionScore;

    public PlayerProfile(String ip) {
        this.ip = ip;
        this.nicknames = new HashSet<>();
        this.connectionTimes = new ArrayList<>();
        this.pingTimes = new ArrayList<>();
        this.totalPlayTime = 0;
        this.firstConnection = System.currentTimeMillis();
        this.lastConnection = System.currentTimeMillis();
        this.suspicionScore = 0;
    }

    public void addConnection(String nickname, long timestamp) {
        nicknames.add(nickname);
        connectionTimes.add(timestamp);
        lastConnection = timestamp;
        
        // Keep only last 50 connections
        while (connectionTimes.size() > 50) {
            connectionTimes.remove(0);
        }
    }

    public void addPing(long timestamp) {
        pingTimes.add(timestamp);
        
        // Keep only last 100 pings
        while (pingTimes.size() > 100) {
            pingTimes.remove(0);
        }
    }

    public void addPlayTime(long milliseconds) {
        totalPlayTime += milliseconds;
    }

    public void increaseSuspicion(int points) {
        suspicionScore += points;
    }

    public void decreaseSuspicion(int points) {
        suspicionScore = Math.max(0, suspicionScore - points);
    }

    // Connection analysis methods
    public long getAverageConnectionInterval() {
        if (connectionTimes.size() < 2) return 0;
        
        long totalInterval = 0;
        for (int i = 1; i < connectionTimes.size(); i++) {
            totalInterval += connectionTimes.get(i) - connectionTimes.get(i - 1);
        }
        return totalInterval / (connectionTimes.size() - 1);
    }

    public int getConnectionsInTimeframe(long timeframe) {
        long cutoff = System.currentTimeMillis() - timeframe;
        return (int) connectionTimes.stream().filter(time -> time > cutoff).count();
    }

    public int getPingsInTimeframe(long timeframe) {
        long cutoff = System.currentTimeMillis() - timeframe;
        return (int) pingTimes.stream().filter(time -> time > cutoff).count();
    }

    public boolean hasRapidReconnections() {
        if (connectionTimes.size() < 3) return false;
        
        // Check for 3+ connections within 10 seconds
        long tenSecondsAgo = System.currentTimeMillis() - 10000;
        return getConnectionsInTimeframe(10000) >= 3;
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
        // Simple similarity check - can be enhanced
        if (name1.length() != name2.length()) return false;
        
        int differences = 0;
        for (int i = 0; i < name1.length(); i++) {
            if (name1.charAt(i) != name2.charAt(i)) {
                differences++;
            }
        }
        
        return differences <= 2; // Allow up to 2 character differences
    }

    // Getters
    public String getIp() { return ip; }
    public Set<String> getNicknames() { return new HashSet<>(nicknames); }
    public int getConnectionCount() { return connectionTimes.size(); }
    public long getPlayTime() { return totalPlayTime; }
    public long getFirstConnection() { return firstConnection; }
    public long getLastConnection() { return lastConnection; }
    public int getSuspicionScore() { return suspicionScore; }
    public List<Long> getConnectionTimes() { return new ArrayList<>(connectionTimes); }
    public List<Long> getPingTimes() { return new ArrayList<>(pingTimes); }
}