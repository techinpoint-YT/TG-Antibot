@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.core.data.PlayerProfile;

-public class BehaviorAnalysisCheck {
-    private final Main plugin;
+public class BehaviorPatternAnalyzer {
+    private final TGAntiBotPlugin plugin;

-    public BehaviorAnalysisCheck(Main plugin) {
+    public BehaviorPatternAnalyzer(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
     }