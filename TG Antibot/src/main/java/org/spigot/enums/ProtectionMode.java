package org.spigot.enums;

public enum ProtectionMode {
    NORMAL("Normal", "§aNormal"),
    STRICT("Strict", "§eStrict"),
    LOCKDOWN("Lockdown", "§cLockdown");

    private final String name;
    private final String displayName;

    ProtectionMode(String name, String displayName) {
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