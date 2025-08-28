package org.spigot;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigot.commands.MainCommand;
import org.spigot.listeners.ConnectionListener;
import org.spigot.listeners.ServerPingListener;
import org.spigot.listeners.PlayerEventListener;
import org.spigot.config.ConfigManager;
import org.spigot.core.BotProtectionManager;
import org.spigot.core.SecurityManager;
import org.spigot.core.NotificationManager;
import org.spigot.core.AttackAnalyzer;
import org.spigot.utils.VPNChecker;
import org.spigot.utils.FirewallManager;
import org.spigot.utils.Messages;
import org.spigot.utils.Metrics;

public class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private BotProtectionManager botProtectionManager;
    private SecurityManager securityManager;
    private NotificationManager notificationManager;
    private AttackAnalyzer attackAnalyzer;
    private VPNChecker vpnChecker;
    private FirewallManager firewallManager;
    private Messages messages;

    @Override
    public void onEnable() {
        instance = this;
        long startTime = System.currentTimeMillis();

        // Initialize configuration
        saveDefaultConfig();
        try {
            saveResource("messages.yml", false);
        } catch (IllegalArgumentException e) {
            getLogger().warning("messages.yml not found in JAR, creating default.");
        }

        // Initialize core managers
        configManager = new ConfigManager(this);
        messages = new Messages(this);
        
        // Initialize security components
        vpnChecker = new VPNChecker(this);
        firewallManager = new FirewallManager(this);
        securityManager = new SecurityManager(this);
        
        // Initialize protection systems
        botProtectionManager = new BotProtectionManager(this);
        attackAnalyzer = new AttackAnalyzer(this);
        notificationManager = new NotificationManager(this);

        // Register event listeners
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ServerPingListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        // Register commands
        registerCommands();

        // Initialize metrics
        new Metrics(this, 19847); // Official TG-AntiBot plugin ID

        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("§aTG-AntiBot has been successfully enabled! §7(Took " + loadTime + "ms)");
        getLogger().info("§7Running advanced bot protection with §c" + getDescription().getVersion() + " §7version");
    }

    private void registerCommands() {
        MainCommand mainCommand = new MainCommand(this);
        PluginCommand command = getCommand("tgantibot");
        if (command != null) {
            command.setExecutor(mainCommand);
            command.setTabCompleter(mainCommand);
        } else {
            getLogger().severe("Command 'tgantibot' is not defined in plugin.yml!");
        }
    }

    @Override
    public void onDisable() {
        long startTime = System.currentTimeMillis();
        getLogger().info("§cShutting down TG-AntiBot...");
        
        if (botProtectionManager != null) {
            botProtectionManager.shutdown();
        }
        if (attackAnalyzer != null) {
            attackAnalyzer.shutdown();
        }
        if (notificationManager != null) {
            notificationManager.shutdown();
        }
        
        long shutdownTime = System.currentTimeMillis() - startTime;
        getLogger().info("§cTG-AntiBot has been disabled! §7(Took " + shutdownTime + "ms)");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        messages.reload();
        if (botProtectionManager != null) {
            botProtectionManager.reload();
        }
    }

    // Getters
    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BotProtectionManager getBotProtectionManager() {
        return botProtectionManager;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public AttackAnalyzer getAttackAnalyzer() {
        return attackAnalyzer;
    }

    public VPNChecker getVPNChecker() {
        return vpnChecker;
    }

    public FirewallManager getFirewallManager() {
        return firewallManager;
    }

    public Messages getMessages() {
        return messages;
    }
}