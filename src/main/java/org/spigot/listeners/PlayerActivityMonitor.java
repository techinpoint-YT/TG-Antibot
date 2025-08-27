@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;

@@ .. @@
-public class PlayerEventListener implements Listener {
+public class PlayerActivityMonitor implements Listener {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;

-    public PlayerEventListener(Main plugin) {
+    public PlayerActivityMonitor(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
     }

@@ .. @@
         // Enable auto notifications for staff
         if (event.getPlayer().hasPermission("tga.notifications.auto")) {
-            plugin.getNotificationManager().enableAutoNotifications(event.getPlayer());
+            plugin.getAlertSystem().enableAutoNotifications(event.getPlayer());
         }
         
         // Update security profile
         String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
-        plugin.getSecurityManager().updateSecurityProfile(ip, "JOIN");
+        plugin.getSecurityController().updateSecurityProfile(ip, "JOIN");
     }

@@ .. @@
         // Remove from notification systems
-        plugin.getNotificationManager().removePlayer(event.getPlayer());
+        plugin.getAlertSystem().removePlayer(event.getPlayer());
         
         // Update security profile
         String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
-        plugin.getSecurityManager().updateSecurityProfile(ip, "QUIT");
+        plugin.getSecurityController().updateSecurityProfile(ip, "QUIT");
     }

@@ .. @@
         String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
-        plugin.getSecurityManager().updateSecurityProfile(ip, "COMMAND");
+        plugin.getSecurityController().updateSecurityProfile(ip, "COMMAND");
     }

@@ .. @@
         String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
-        plugin.getSecurityManager().updateSecurityProfile(ip, "CHAT");
+        plugin.getSecurityController().updateSecurityProfile(ip, "CHAT");
     }
 }