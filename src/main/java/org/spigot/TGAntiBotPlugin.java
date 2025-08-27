@@ .. @@
-public class Main extends JavaPlugin {
+public class TGAntiBotPlugin extends JavaPlugin {

-    private static Main instance;
+    private static TGAntiBotPlugin instance;
     private ConfigManager configManager;
-    private BotProtectionManager botProtectionManager;
-    private SecurityManager securityManager;
-    private NotificationManager notificationManager;
-    private AttackAnalyzer attackAnalyzer;
-    private VPNChecker vpnChecker;
-    private FirewallManager firewallManager;
+    private ThreatDetectionEngine threatDetectionEngine;
+    private SecurityController securityController;
+    private AlertSystem alertSystem;
+    private ThreatAnalytics threatAnalytics;
+    private ProxyDetector proxyDetector;
+    private NetworkFirewall networkFirewall;
     private Messages messages;

@@ .. @@
         // Initialize core managers
         configManager = new ConfigManager(this);
         messages = new Messages(this);
         
         // Initialize security components
-        vpnChecker = new VPNChecker(this);
-        firewallManager = new FirewallManager(this);
-        securityManager = new SecurityManager(this);
+        proxyDetector = new ProxyDetector(this);
+        networkFirewall = new NetworkFirewall(this);
+        securityController = new SecurityController(this);
         
         // Initialize protection systems
-        botProtectionManager = new BotProtectionManager(this);
-        attackAnalyzer = new AttackAnalyzer(this);
-        notificationManager = new NotificationManager(this);
+        threatDetectionEngine = new ThreatDetectionEngine(this);
+        threatAnalytics = new ThreatAnalytics(this);
+        alertSystem = new AlertSystem(this);

@@ .. @@
         // Register event listeners
-        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
-        getServer().getPluginManager().registerEvents(new ServerPingListener(this), this);
-        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
+        getServer().getPluginManager().registerEvents(new PlayerConnectionHandler(this), this);
+        getServer().getPluginManager().registerEvents(new ServerStatusHandler(this), this);
+        getServer().getPluginManager().registerEvents(new PlayerActivityMonitor(this), this);

@@ .. @@
     private void registerCommands() {
-        MainCommand mainCommand = new MainCommand(this);
+        TGAntiBotCommand mainCommand = new TGAntiBotCommand(this);
         PluginCommand command = getCommand("tgantibot");
@@ .. @@
         getLogger().info("§cShutting down TG-AntiBot...");
         
-        if (botProtectionManager != null) {
-            botProtectionManager.shutdown();
+        if (threatDetectionEngine != null) {
+            threatDetectionEngine.shutdown();
         }
-        if (attackAnalyzer != null) {
-            attackAnalyzer.shutdown();
+        if (threatAnalytics != null) {
+            threatAnalytics.shutdown();
         }
-        if (notificationManager != null) {
-            notificationManager.shutdown();
+        if (alertSystem != null) {
+            alertSystem.shutdown();
         }
@@ .. @@
         reloadConfig();
         configManager.reload();
         messages.reload();
-        if (botProtectionManager != null) {
-            botProtectionManager.reload();
+        if (threatDetectionEngine != null) {
+            threatDetectionEngine.reload();
         }
     }

     // Getters
-    public static Main getInstance() {
+    public static TGAntiBotPlugin getInstance() {
         return instance;
     }

@@ .. @@
         return configManager;
     }

-    public BotProtectionManager getBotProtectionManager() {
-        return botProtectionManager;
+    public ThreatDetectionEngine getThreatDetectionEngine() {
+        return threatDetectionEngine;
     }

-    public SecurityManager getSecurityManager() {
-        return securityManager;
+    public SecurityController getSecurityController() {
+        return securityController;
     }

-    public NotificationManager getNotificationManager() {
-        return notificationManager;
+    public AlertSystem getAlertSystem() {
+        return alertSystem;
     }

-    public AttackAnalyzer getAttackAnalyzer() {
-        return attackAnalyzer;
+    public ThreatAnalytics getThreatAnalytics() {
+        return threatAnalytics;
     }

-    public VPNChecker getVPNChecker() {
-        return vpnChecker;
+    public ProxyDetector getProxyDetector() {
+        return proxyDetector;
     }

-    public FirewallManager getFirewallManager() {
-        return firewallManager;
+    public NetworkFirewall getNetworkFirewall() {
+        return networkFirewall;
     }

@@ .. @@
 }