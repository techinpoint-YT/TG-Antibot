@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.core.data.AttackLog;
 import org.spigot.core.data.PlayerProfile;
 import org.spigot.enums.AttackType;

@@ .. @@
-public class MainCommand implements CommandExecutor, TabCompleter {
+public class TGAntiBotCommand implements CommandExecutor, TabCompleter {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;

-    public MainCommand(Main plugin) {
+    public TGAntiBotCommand(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
     }

@@ .. @@
         sender.sendMessage("§7Protection Mode: " + plugin.getBotProtectionManager().getCurrentMode().getDisplayName());
-        sender.sendMessage("§7Joins/sec: §c" + plugin.getBotProtectionManager().getJoinsPerSecond());
-        sender.sendMessage("§7Pings/sec: §b" + plugin.getBotProtectionManager().getPingsPerSecond());
-        sender.sendMessage("§7Packets/sec: §6" + plugin.getBotProtectionManager().getPacketsPerSecond());
+        sender.sendMessage("§7Joins/sec: §c" + plugin.getThreatDetectionEngine().getJoinsPerSecond());
+        sender.sendMessage("§7Pings/sec: §b" + plugin.getThreatDetectionEngine().getPingsPerSecond());
+        sender.sendMessage("§7Packets/sec: §6" + plugin.getThreatDetectionEngine().getPacketsPerSecond());
         sender.sendMessage("");
-        sender.sendMessage("§7Total Attacks Blocked: §c" + plugin.getAttackAnalyzer().getTotalAttacksBlocked());
-        sender.sendMessage("§7Total Connections Analyzed: §e" + plugin.getAttackAnalyzer().getTotalConnectionsAnalyzed());
-        sender.sendMessage("§7Block Rate: §a" + String.format("%.2f%%", plugin.getAttackAnalyzer().getBlockRate()));
+        sender.sendMessage("§7Total Attacks Blocked: §c" + plugin.getThreatAnalytics().getTotalAttacksBlocked());
+        sender.sendMessage("§7Total Connections Analyzed: §e" + plugin.getThreatAnalytics().getTotalConnectionsAnalyzed());
+        sender.sendMessage("§7Block Rate: §a" + String.format("%.2f%%", plugin.getThreatAnalytics().getBlockRate()));
         sender.sendMessage("");
-        sender.sendMessage("§7Whitelist Size: §a" + plugin.getBotProtectionManager().getWhitelist().size());
-        sender.sendMessage("§7Blacklist Size: §c" + plugin.getBotProtectionManager().getBlacklist().size());
+        sender.sendMessage("§7Whitelist Size: §a" + plugin.getThreatDetectionEngine().getWhitelist().size());
+        sender.sendMessage("§7Blacklist Size: §c" + plugin.getThreatDetectionEngine().getBlacklist().size());
         sender.sendMessage("§8§l§n" + "=".repeat(50));
     }

@@ .. @@
         sender.sendMessage("§c§lTG-ANTIBOT STATUS");
         sender.sendMessage("");
-        sender.sendMessage("§7Current Mode: " + plugin.getBotProtectionManager().getCurrentMode().getDisplayName());
+        sender.sendMessage("§7Current Mode: " + plugin.getThreatDetectionEngine().getCurrentMode().getDisplayName());
         
-        if (plugin.getBotProtectionManager().getCurrentAttack().isActive()) {
+        if (plugin.getThreatDetectionEngine().getCurrentAttack().isActive()) {
             sender.sendMessage("§7Attack Status: §c§lACTIVE");
-            sender.sendMessage("§7Attack Type: " + plugin.getBotProtectionManager().getCurrentAttack().getCurrentAttackType().getDisplayName());
-            sender.sendMessage("§7Duration: §e" + (plugin.getBotProtectionManager().getCurrentAttack().getDuration() / 1000) + "s");
-            sender.sendMessage("§7Peak Intensity: §c" + plugin.getBotProtectionManager().getCurrentAttack().getPeakIntensity());
+            sender.sendMessage("§7Attack Type: " + plugin.getThreatDetectionEngine().getCurrentAttack().getCurrentAttackType().getDisplayName());
+            sender.sendMessage("§7Duration: §e" + (plugin.getThreatDetectionEngine().getCurrentAttack().getDuration() / 1000) + "s");
+            sender.sendMessage("§7Peak Intensity: §c" + plugin.getThreatDetectionEngine().getCurrentAttack().getPeakIntensity());
         } else {
             sender.sendMessage("§7Attack Status: §a§lNORMAL");
         }
@@ .. @@
         if (action.equals("add")) {
-            plugin.getBotProtectionManager().addToWhitelist(ip);
+            plugin.getThreatDetectionEngine().addToWhitelist(ip);
             sender.sendMessage("§aAdded §e" + target + " §a(§7" + ip + "§a) to whitelist.");
         } else if (action.equals("remove")) {
-            plugin.getBotProtectionManager().removeFromBlacklist(ip);
+            plugin.getThreatDetectionEngine().removeFromBlacklist(ip);
             sender.sendMessage("§aRemoved §e" + target + " §a(§7" + ip + "§a) from whitelist.");
         } else {
             sender.sendMessage("§cUsage: /tga whitelist <add|remove> <player|ip>");
@@ .. @@
         if (action.equals("add")) {
-            plugin.getBotProtectionManager().addToBlacklist(ip, "Manual blacklist by " + sender.getName());
+            plugin.getThreatDetectionEngine().addToBlacklist(ip, "Manual blacklist by " + sender.getName());
             sender.sendMessage("§aAdded §e" + target + " §a(§7" + ip + "§a) to blacklist.");
         } else if (action.equals("remove")) {
-            plugin.getBotProtectionManager().removeFromBlacklist(ip);
+            plugin.getThreatDetectionEngine().removeFromBlacklist(ip);
             sender.sendMessage("§aRemoved §e" + target + " §a(§7" + ip + "§a) from blacklist.");
         } else {
             sender.sendMessage("§cUsage: /tga blacklist <add|remove> <player|ip>");
@@ .. @@
-        List<AttackLog> recentAttacks = plugin.getAttackAnalyzer().getRecentAttacks(limit);
+        List<AttackLog> recentAttacks = plugin.getThreatAnalytics().getRecentAttacks(limit);
         
         if (recentAttacks.isEmpty()) {
@@ .. @@
-        PlayerProfile profile = plugin.getBotProtectionManager().getPlayerProfiles().get(ip);
+        PlayerProfile profile = plugin.getThreatDetectionEngine().getPlayerProfiles().get(ip);
         if (profile == null) {
@@ .. @@
         switch (type) {
             case "actionbar":
-                plugin.getNotificationManager().toggleActionBar(player);
+                plugin.getAlertSystem().toggleActionBar(player);
                 break;
             case "title":
-                plugin.getNotificationManager().toggleTitle(player);
+                plugin.getAlertSystem().toggleTitle(player);
                 break;
             case "bossbar":
-                plugin.getNotificationManager().toggleBossBar(player);
+                plugin.getAlertSystem().toggleBossBar(player);
                 break;
             default:
                 player.sendMessage("§cUsage: /tga toggle <actionbar|title|bossbar>");