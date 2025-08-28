package org.spigot.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.spigot.Main;

public class PlayerEventListener implements Listener {

    private final Main plugin;

    public PlayerEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) return;
        
        // Enable auto notifications for staff
        if (event.getPlayer().hasPermission("tga.notifications.auto")) {
            plugin.getNotificationManager().enableAutoNotifications(event.getPlayer());
        }
        
        // Update security profile
        if (event.getPlayer().getAddress() != null) {
            String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
            plugin.getSecurityManager().updateSecurityProfile(ip, "JOIN");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() == null) return;
        
        // Remove from notification systems
        plugin.getNotificationManager().removePlayer(event.getPlayer());
        
        // Update security profile
        if (event.getPlayer().getAddress() != null) {
            String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
            plugin.getSecurityManager().updateSecurityProfile(ip, "QUIT");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (event.getPlayer() == null || event.getPlayer().getAddress() == null) return;
        
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        String command = event.getMessage();
        
        // Log suspicious commands
        if (isSuspiciousCommand(command)) {
            plugin.getLogger().warning("Suspicious command from " + event.getPlayer().getName() + 
                " (" + ip + "): " + command);
        }
        
        plugin.getSecurityManager().updateSecurityProfile(ip, "COMMAND:" + command.split(" ")[0]);
    }

    // Using deprecated PlayerChatEvent for compatibility, but with proper handling
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        if (event.getPlayer() == null || event.getPlayer().getAddress() == null) return;
        
        String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
        String message = event.getMessage();
        
        // Check for spam or suspicious patterns
        if (isSuspiciousMessage(message)) {
            plugin.getLogger().info("Suspicious message from " + event.getPlayer().getName() + 
                " (" + ip + "): " + message);
        }
        
        plugin.getSecurityManager().updateSecurityProfile(ip, "CHAT");
    }
    
    private boolean isSuspiciousCommand(String command) {
        if (command == null) return false;
        
        String lowerCommand = command.toLowerCase();
        return lowerCommand.contains("//") || // WorldEdit commands
               lowerCommand.contains("eval") ||
               lowerCommand.contains("script") ||
               lowerCommand.contains("execute") ||
               lowerCommand.startsWith("/op ") ||
               lowerCommand.startsWith("/deop ");
    }
    
    private boolean isSuspiciousMessage(String message) {
        if (message == null) return false;
        
        String lowerMessage = message.toLowerCase();
        return message.length() > 200 || // Very long messages
               lowerMessage.contains("http://") ||
               lowerMessage.contains("https://") ||
               message.chars().distinct().count() < 5; // Very repetitive
    }
}