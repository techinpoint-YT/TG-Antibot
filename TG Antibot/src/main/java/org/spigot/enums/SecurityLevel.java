package org.spigot.enums;

public enum SecurityLevel {
    LOW_RISK("Low Risk", "§aLow Risk"),
    MEDIUM_RISK("Medium Risk", "§eMedium Risk"),
    HIGH_RISK("High Risk", "§cHigh Risk");

    private final String name;
    private final String displayName;

    SecurityLevel(String name, String displayName) {
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