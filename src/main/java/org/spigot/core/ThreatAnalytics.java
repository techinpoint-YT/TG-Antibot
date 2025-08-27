@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.core.data.AttackLog;
@@ .. @@
-public class AttackAnalyzer {
+public class ThreatAnalytics {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;
     private final Queue<AttackLog> attackHistory;
@@ .. @@
-    public AttackAnalyzer(Main plugin) {
+    public ThreatAnalytics(TGAntiBotPlugin plugin) {
         this.plugin = plugin;