package org.spigot.core;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.spigot.Main;
import org.spigot.enums.AttackType;

import java.util.HashSet;
import java.util.Set;

public class NotificationManager {

    private final Main plugin;
    private final Set<Player> actionBarSubscribers;
    private final Set<Player> titleSubscribers;
    private final Set<Player> bossBarSubscribers;
    private BossBar attackBossBar;

    public NotificationManager(Main plugin) {
        this.plugin = plugin;
        this.actionBarSubscribers = new HashSet<>();
        this.titleSubscribers = new HashSet<>();
        this.bossBarSubscribers = new HashSet<>();
        
        initializeBossBar();
        startNotificationTask();
    }

    private void initializeBossBar() {
        attackBossBar = Bukkit.createBossBar(
            "§aTG-AntiBot - Server Protected", 
            BarColor.GREEN, 
            BarStyle.SOLID
        );
    }

    public void broadcastAttackAlert(AttackType attackType, long intensity) {
        String message = plugin.getMessages().get("attack-detected", 
            "§c[ALERT] Attack detected: {type} - Intensity: {intensity}")
            .replace("{type}", attackType.getDisplayName())
            .replace("{intensity}", String.valueOf(intensity));

        // Notify staff
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tga.alerts")) {
                player.sendMessage(plugin.getMessages().get("prefix") + message);
            }
        }

        // Update boss bar
        attackBossBar.setTitle("§c[UNDER ATTACK] " + attackType.getDisplayName());
        attackBossBar.setColor(BarColor.RED);
        attackBossBar.setProgress(Math.min(intensity / 100.0, 1.0));

        plugin.getLogger().warning("[ATTACK DETECTED] " + attackType.getDisplayName() + " - Intensity: " + intensity);
    }

    public void broadcastAttackEnd() {
        String message = plugin.getMessages().get("attack-ended", "§a[INFO] Attack has ended. Protection normalized.");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("tga.alerts")) {
                player.sendMessage(plugin.getMessages().get("prefix") + message);
            }
        }

        // Reset boss bar
        attackBossBar.setTitle("§aTG-AntiBot - Server Protected");
        attackBossBar.setColor(BarColor.GREEN);
        attackBossBar.setProgress(1.0);

        plugin.getLogger().info("[ATTACK ENDED] Protection returned to normal mode.");
    }

    public void toggleActionBar(Player player) {
        if (actionBarSubscribers.contains(player)) {
            actionBarSubscribers.remove(player);
            player.sendMessage(plugin.getMessages().get("prefix") + 
                plugin.getMessages().get("actionbar-disabled", "§cAction bar notifications disabled."));
        } else {
            actionBarSubscribers.add(player);
            player.sendMessage(plugin.getMessages().get("prefix") + 
                plugin.getMessages().get("actionbar-enabled", "§aAction bar notifications enabled."));
        }
    }

    public void toggleTitle(Player player) {
        if (titleSubscribers.contains(player)) {
            titleSubscribers.remove(player);
            player.sendMessage(plugin.getMessages().get("prefix") + 
                plugin.getMessages().get("title-disabled", "§cTitle notifications disabled."));
        } else {
            titleSubscribers.add(player);
            player.sendMessage(plugin.getMessages().get("prefix") + 
                plugin.getMessages().get("title-enabled", "§aTitle notifications enabled."));
        }
    }

    public void toggleBossBar(Player player) {
        if (bossBarSubscribers.contains(player)) {
            bossBarSubscribers.remove(player);
            attackBossBar.removePlayer(player);
            player.sendMessage(plugin.getMessages().get("prefix") + 
                plugin.getMessages().get("bossbar-disabled", "§cBoss bar notifications disabled."));
        } else {
            bossBarSubscribers.add(player);
            attackBossBar.addPlayer(player);
            player.sendMessage(plugin.getMessages().get("prefix") + 
                plugin.getMessages().get("bossbar-enabled", "§aBoss bar notifications enabled."));
        }
    }

    public void enableAutoNotifications(Player player) {
        if (player.hasPermission("tga.notifications.auto")) {
            actionBarSubscribers.add(player);
            attackBossBar.addPlayer(player);
            bossBarSubscribers.add(player);
        }
    }

    public void removePlayer(Player player) {
        actionBarSubscribers.remove(player);
        titleSubscribers.remove(player);
        bossBarSubscribers.remove(player);
        attackBossBar.removePlayer(player);
    }

    private void startNotificationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            updateActionBars();
            updateTitles();
        }, 0L, 20L); // Update every second
    }

    private void updateActionBars() {
        if (actionBarSubscribers.isEmpty()) return;

        String message = buildStatusMessage();
        TextComponent component = new TextComponent(message);

        for (Player player : actionBarSubscribers) {
            if (player.isOnline()) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
            }
        }
    }

    private void updateTitles() {
        if (titleSubscribers.isEmpty()) return;

        String title = plugin.getMessages().get("title-protection", "§c§lTG-ANTIBOT");
        String subtitle = buildStatusMessage();

        for (Player player : titleSubscribers) {
            if (player.isOnline()) {
                player.sendTitle(title, subtitle, 0, 25, 5);
            }
        }
    }

    private String buildStatusMessage() {
        return plugin.getMessages().get("status-format", 
            "§7Mode: §e{mode} §7| Joins: §c{joins}/s §7| Pings: §b{pings}/s")
            .replace("{mode}", plugin.getBotProtectionManager().getCurrentMode().getDisplayName())
            .replace("{joins}", String.valueOf(plugin.getBotProtectionManager().getJoinsPerSecond()))
            .replace("{pings}", String.valueOf(plugin.getBotProtectionManager().getPingsPerSecond()));
    }

    public void shutdown() {
        if (attackBossBar != null) {
            attackBossBar.removeAll();
        }
        actionBarSubscribers.clear();
        titleSubscribers.clear();
        bossBarSubscribers.clear();
    }
}