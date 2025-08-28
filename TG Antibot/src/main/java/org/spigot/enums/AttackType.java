package org.spigot.enums;

public enum AttackType {
    JOIN_FLOOD("Join Flood", "§cJoin Flood"),
    PING_FLOOD("Ping Flood", "§ePing Flood"),
    PACKET_FLOOD("Packet Flood", "§6Packet Flood"),
    RECONNECT_SPAM("Reconnect Spam", "§dReconnect Spam"),
    NICKNAME_SPAM("Nickname Spam", "§bNickname Spam"),
    ACCOUNT_SPAM("Account Spam", "§5Account Spam"),
    GEO_ANOMALY("Geographic Anomaly", "§9Geo Anomaly"),
    BEHAVIOR_ANOMALY("Behavior Anomaly", "§aBehavior Anomaly");

    private final String name;
    private final String displayName;

    AttackType(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }
}