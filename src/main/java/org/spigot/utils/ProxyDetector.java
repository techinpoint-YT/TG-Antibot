@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;

@@ .. @@
-public class VPNChecker {
+public class ProxyDetector {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;
     private final OkHttpClient httpClient;
     private final ObjectMapper jsonMapper;

-    public VPNChecker(Main plugin) {
+    public ProxyDetector(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
@@ .. @@
-        } catch (Exception e) {
-            plugin.getLogger().warning("[VPNChecker] Unexpected error for IP " + ip + ": " + e.getMessage());
+        } catch (Exception e) {
+            plugin.getLogger().warning("[ProxyDetector] Unexpected error for IP " + ip + ": " + e.getMessage());
         }