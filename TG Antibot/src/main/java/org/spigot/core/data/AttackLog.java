package org.spigot.core.data;

import org.spigot.enums.AttackType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AttackLog {
    private final AttackType type;
    private final String sourceIP;
    private final long intensity;
    private final long timestamp;
    private final String formattedDate;

    public AttackLog(AttackType type, String sourceIP, long intensity, long timestamp) {
        this.type = type;
        this.sourceIP = sourceIP;
        this.intensity = intensity;
        this.timestamp = timestamp;
        this.formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    public String getDetailedInfo() {
        return String.format("§7[%s] §c%s §7from §e%s §7(Intensity: §c%d§7)", 
            formattedDate, type.getDisplayName(), sourceIP, intensity);
    }

    // Getters
    public AttackType getType() { return type; }
    public String getSourceIP() { return sourceIP; }
    public long getIntensity() { return intensity; }
    public long getTimestamp() { return timestamp; }
    public String getFormattedDate() { return formattedDate; }
}