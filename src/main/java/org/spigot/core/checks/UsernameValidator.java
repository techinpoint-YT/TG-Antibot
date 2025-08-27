@@ .. @@
-import org.spigot.Main;
+import org.spigot.TGAntiBotPlugin;

@@ .. @@
-public class NicknameCheck {
-    private final Main plugin;
+public class UsernameValidator {
+    private final TGAntiBotPlugin plugin;
     private final Pattern suspiciousPattern;

-    public NicknameCheck(Main plugin) {
+    public UsernameValidator(TGAntiBotPlugin plugin) {
         this.plugin = plugin;