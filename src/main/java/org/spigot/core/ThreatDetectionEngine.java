@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.core.checks.*;
 import org.spigot.core.data.PlayerProfile;
 import org.spigot.core.data.AttackData;
@@ .. @@
-public class BotProtectionManager {
+public class ThreatDetectionEngine {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;
     private final Map<String, PlayerProfile> playerProfiles;
@@ .. @@
-    public BotProtectionManager(Main plugin) {
+    public ThreatDetectionEngine(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
@@ .. @@
         // Initialize checks
-        this.speedCheck = new ConnectionSpeedCheck(plugin);
-        this.nicknameCheck = new NicknameCheck(plugin);
-        this.accountCheck = new AccountLimitCheck(plugin);
-        this.reconnectCheck = new ReconnectCheck(plugin);
-        this.geoCheck = new GeoLocationCheck(plugin);
-        this.behaviorCheck = new BehaviorAnalysisCheck(plugin);
+        this.speedCheck = new ConnectionVelocityAnalyzer(plugin);
+        this.nicknameCheck = new UsernameValidator(plugin);
+        this.accountCheck = new AccountLimitValidator(plugin);
+        this.reconnectCheck = new ReconnectionAnalyzer(plugin);
+        this.geoCheck = new GeographicFilter(plugin);
+        this.behaviorCheck = new BehaviorPatternAnalyzer(plugin);