@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.core.data.SecurityProfile;
@@ .. @@
-public class SecurityManager {
+public class SecurityController {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;
     private final Map<String, SecurityProfile> securityProfiles;

-    public SecurityManager(Main plugin) {
+    public SecurityController(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
@@ .. @@
         // VPN/Proxy check
-        if (plugin.getVPNChecker().isUsingVPN(player)) {
+        if (plugin.getProxyDetector().isUsingVPN(player)) {
             score += 30;
         }