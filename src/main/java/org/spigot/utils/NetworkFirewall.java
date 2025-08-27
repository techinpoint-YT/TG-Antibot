@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;

@@ .. @@
-public class FirewallManager {
+public class NetworkFirewall {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;
     private final Set<String> dynamicBlockedIPs;
     private final Set<String> tempBlockedIPs;

-    public FirewallManager(Main plugin) {
+    public NetworkFirewall(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
@@ .. @@
         if (plugin.getConfigManager().isDebugMode()) {
-            plugin.getLogger().info("[FirewallManager] Checking IP: " + ip);
+            plugin.getLogger().info("[NetworkFirewall] Checking IP: " + ip);
         }
@@ .. @@
         if (configBlockedIps.contains(ip) || dynamicBlockedIPs.contains(ip) || tempBlockedIPs.contains(ip)) {
             if (plugin.getConfigManager().isDebugMode()) {
-                plugin.getLogger().info("[FirewallManager] Blocked connection from IP: " + ip);
+                plugin.getLogger().info("[NetworkFirewall] Blocked connection from IP: " + ip);
             }
             return true;
         }