@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.enums.AttackType;

@@ .. @@
-public class ServerPingListener implements Listener {
+public class ServerStatusHandler implements Listener {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;

-    public ServerPingListener(Main plugin) {
+    public ServerStatusHandler(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
     }

@@ .. @@
         String ip = event.getAddress().getHostAddress();
         
         // Handle server ping in bot protection manager
-        plugin.getBotProtectionManager().handleServerPing(event.getAddress());
+        plugin.getThreatDetectionEngine().handleServerPing(event.getAddress());
         
         // Check if IP is blocked
-        if (plugin.getFirewallManager().isBlocked(ip)) {
+        if (plugin.getNetworkFirewall().isBlocked(ip)) {
             // Don't reveal server info to blocked IPs
             event.setMotd("§cAccess Denied");
             event.setMaxPlayers(0);
-            plugin.getAttackAnalyzer().recordAttack(AttackType.PING_FLOOD, ip, 1);
+            plugin.getThreatAnalytics().recordAttack(AttackType.PING_FLOOD, ip, 1);
             return;
         }
         
         // Customize MOTD based on protection mode
-        switch (plugin.getBotProtectionManager().getCurrentMode()) {
+        switch (plugin.getThreatDetectionEngine().getCurrentMode()) {
             case LOCKDOWN:
                 event.setMotd("§c§lSERVER UNDER PROTECTION\n§7Please wait before connecting");