package org.spigot.core.data;

import org.spigot.enums.AttackType;

public class AttackData {
    private AttackType currentAttackType;
    private long attackStartTime;
    private long attackEndTime;
    private long peakIntensity;
    private long totalBlocked;
    private boolean isActive;

    public AttackData() {
        this.isActive = false;
        this.totalBlocked = 0;
    }

    public void startAttack(AttackType type, long intensity) {
        this.currentAttackType = type;
        this.attackStartTime = System.currentTimeMillis();
        this.peakIntensity = intensity;
        this.isActive = true;
        this.totalBlocked = 0;
    }

    public void updateIntensity(long intensity) {
        if (intensity > peakIntensity) {
            peakIntensity = intensity;
        }
    }

    public void incrementBlocked() {
        totalBlocked++;
    }

    public void endAttack() {
        this.attackEndTime = System.currentTimeMillis();
        this.isActive = false;
    }

    public long getDuration() {
        if (isActive) {
            return System.currentTimeMillis() - attackStartTime;
        } else {
            return attackEndTime - attackStartTime;
        }
    }

    // Getters
    public AttackType getCurrentAttackType() { return currentAttackType; }
    public long getAttackStartTime() { return attackStartTime; }
    public long getAttackEndTime() { return attackEndTime; }
    public long getPeakIntensity() { return peakIntensity; }
    public long getTotalBlocked() { return totalBlocked; }
    public boolean isActive() { return isActive; }
}