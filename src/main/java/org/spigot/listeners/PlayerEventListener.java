package org.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.spigot.Main;

public class PlayerEventListener implements Listener {

    private final Main plugin;

    public PlayerEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Enable auto notifications for staff
        if (event.getPlayer().hasPermission("tga.notifications.auto")) {
            plugin.getNotificationManager().enableAutoNotifications(event.getPlayer());
        }
        
        // Update security profile
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        plugin.getSecurityManager().updateSecurityProfile(ip, "JOIN");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove from notification systems
        plugin.getNotificationManager().removePlayer(event.getPlayer());
        
        // Update security profile
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        plugin.getSecurityManager().updateSecurityProfile(ip, "QUIT");
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        plugin.getSecurityManager().updateSecurityProfile(ip, "COMMAND");
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        plugin.getSecurityManager().updateSecurityProfile(ip, "CHAT");
    }
}