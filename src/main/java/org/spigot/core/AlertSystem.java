@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.enums.AttackType;
@@ .. @@
-public class NotificationManager {
+public class AlertSystem {

-    private final Main plugin;
+    private final TGAntiBotPlugin plugin;
     private final Set<Player> actionBarSubscribers;
@@ .. @@
-    public NotificationManager(Main plugin) {
+    public AlertSystem(TGAntiBotPlugin plugin) {
         this.plugin = plugin;