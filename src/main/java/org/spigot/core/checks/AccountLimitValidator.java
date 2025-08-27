@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;
 import org.spigot.core.data.PlayerProfile;

-public class AccountLimitCheck {
-    private final Main plugin;
+public class AccountLimitValidator {
+    private final TGAntiBotPlugin plugin;

-    public AccountLimitCheck(Main plugin) {
+    public AccountLimitValidator(TGAntiBotPlugin plugin) {
         this.plugin = plugin;
     }