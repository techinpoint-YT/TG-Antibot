@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.enums.AttackType;

@@ .. @@
-public class ConnectionListener implements Listener {
+public class PlayerConnectionHandler implements Listener {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;

-    public ConnectionListener(Main plugin) {
+    public PlayerConnectionHandler(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
     }

@@ .. @@
         // Record connection analysis
-        plugin.getAttackAnalyzer().recordConnectionAnalysis();
+        plugin.getThreatAnalytics().recordConnectionAnalysis();

         // Check firewall first
-        if (plugin.getFirewallManager().isBlocked(ip)) {
+        if (plugin.getNetworkFirewall().isBlocked(ip)) {
             event.disallow(Result.KICK_OTHER, plugin.getMessages().get(
@@ .. @@
-            plugin.getAttackAnalyzer().recordAttack(AttackType.JOIN_FLOOD, ip, 1);
+            plugin.getThreatAnalytics().recordAttack(AttackType.JOIN_FLOOD, ip, 1);
             return;
         }

         // Check VPN if enabled
-        if (plugin.getVPNChecker().isUsingVPN(ip)) {
+        if (plugin.getProxyDetector().isUsingVPN(ip)) {
             event.disallow(Result.KICK_OTHER, plugin.getMessages().get(
@@ .. @@
-            plugin.getAttackAnalyzer().recordAttack(AttackType.BEHAVIOR_ANOMALY, ip, 1);
+            plugin.getThreatAnalytics().recordAttack(AttackType.BEHAVIOR_ANOMALY, ip, 1);
             return;
         }

         // Run bot protection checks
-        boolean blocked = plugin.getBotProtectionManager().shouldBlockConnection(
+        boolean blocked = plugin.getThreatDetectionEngine().shouldBlockConnection(
                 uuid,
@@ .. @@
             // Determine attack type based on current protection mode
             AttackType attackType = determineAttackType();
-            plugin.getAttackAnalyzer().recordAttack(attackType, ip, 1);
+            plugin.getThreatAnalytics().recordAttack(attackType, ip, 1);
         }
     }

     private AttackType determineAttackType() {
-        switch (plugin.getBotProtectionManager().getCurrentMode()) {
+        switch (plugin.getThreatDetectionEngine().getCurrentMode()) {
             case LOCKDOWN:
                 return AttackType.JOIN_FLOOD;