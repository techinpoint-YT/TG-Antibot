package org.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigot.Main;
import org.spigot.core.data.AttackLog;
import org.spigot.core.data.PlayerProfile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public MainCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);

        switch (subCommand) {
            case "help":
                sendHelp(sender);
                break;

            case "reload":
                handleReload(sender);
                break;

            case "stats":
                handleStats(sender);
                break;

            case "status":
                handleStatus(sender);
                break;

            case "whitelist":
                handleWhitelist(sender, args);
                break;

            case "blacklist":
                handleBlacklist(sender, args);
                break;

            case "attacks":
                handleAttacks(sender, args);
                break;

            case "profile":
                handleProfile(sender, args);
                break;

            case "toggle":
                handleToggle(sender, args);
                break;

            // No default case needed since help is already handled
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("tga.reload")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        plugin.reload();
        sender.sendMessage(plugin.getMessages().get("reload", "&aTG-AntiBot config reloaded."));
    }

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("tga.stats")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lTG-ANTIBOT STATISTICS");
        sender.sendMessage("");
        sender.sendMessage("§7Protection Mode: " + plugin.getBotProtectionManager().getCurrentMode().getDisplayName());
        sender.sendMessage("§7Joins/sec: §c" + plugin.getBotProtectionManager().getJoinsPerSecond());
        sender.sendMessage("§7Pings/sec: §b" + plugin.getBotProtectionManager().getPingsPerSecond());
        sender.sendMessage("§7Packets/sec: §6" + plugin.getBotProtectionManager().getPacketsPerSecond());
        sender.sendMessage("");
        sender.sendMessage("§7Total Attacks Blocked: §c" + plugin.getAttackAnalyzer().getTotalAttacksBlocked());
        sender.sendMessage("§7Total Connections Analyzed: §e" + plugin.getAttackAnalyzer().getTotalConnectionsAnalyzed());
        sender.sendMessage("§7Block Rate: §a" + String.format("%.2f%%", plugin.getAttackAnalyzer().getBlockRate()));
        sender.sendMessage("");
        sender.sendMessage("§7Whitelist Size: §a" + plugin.getBotProtectionManager().getWhitelist().size());
        sender.sendMessage("§7Blacklist Size: §c" + plugin.getBotProtectionManager().getBlacklist().size());
        sender.sendMessage("§8§l§n" + separator);
    }

    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("tga.status")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lTG-ANTIBOT STATUS");
        sender.sendMessage("");
        sender.sendMessage("§7Current Mode: " + plugin.getBotProtectionManager().getCurrentMode().getDisplayName());

        if (plugin.getBotProtectionManager().getCurrentAttack().isActive()) {
            sender.sendMessage("§7Attack Status: §c§lACTIVE");
            sender.sendMessage("§7Attack Type: " + plugin.getBotProtectionManager().getCurrentAttack().getCurrentAttackType().getDisplayName());
            sender.sendMessage("§7Duration: §e" + (plugin.getBotProtectionManager().getCurrentAttack().getDuration() / 1000) + "s");
            sender.sendMessage("§7Peak Intensity: §c" + plugin.getBotProtectionManager().getCurrentAttack().getPeakIntensity());
        } else {
            sender.sendMessage("§7Attack Status: §a§lNORMAL");
        }

        sender.sendMessage("");
        sender.sendMessage("§7VPN Protection: " + (plugin.getConfigManager().isVpnProtectionEnabled() ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage("§7Firewall: " + (plugin.getConfigManager().isFirewallEnabled() ? "§aEnabled" : "§cDisabled"));
        sender.sendMessage("§8§l§n" + separator);
    }

    private void handleWhitelist(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tga.whitelist")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /tga whitelist <add|remove> <player|ip>");
            return;
        }

        String action = args[1].toLowerCase();
        String target = args[2];
        String ip = getIPFromTarget(target);

        if (ip == null) {
            sender.sendMessage("§cPlayer not found or invalid IP address.");
            return;
        }

        if (action.equals("add")) {
            plugin.getBotProtectionManager().addToWhitelist(ip);
            sender.sendMessage("§aAdded §e" + target + " §a(§7" + ip + "§a) to whitelist.");
        } else if (action.equals("remove")) {
            plugin.getBotProtectionManager().removeFromWhitelist(ip); // Fixed: was removeFromBlacklist
            sender.sendMessage("§aRemoved §e" + target + " §a(§7" + ip + "§a) from whitelist.");
        } else {
            sender.sendMessage("§cUsage: /tga whitelist <add|remove> <player|ip>");
        }
    }

    private void handleBlacklist(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tga.blacklist")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /tga blacklist <add|remove> <player|ip>");
            return;
        }

        String action = args[1].toLowerCase();
        String target = args[2];
        String ip = getIPFromTarget(target);

        if (ip == null) {
            sender.sendMessage("§cPlayer not found or invalid IP address.");
            return;
        }

        if (action.equals("add")) {
            plugin.getBotProtectionManager().addToBlacklist(ip, "Manual blacklist by " + sender.getName());
            sender.sendMessage("§aAdded §e" + target + " §a(§7" + ip + "§a) to blacklist.");
        } else if (action.equals("remove")) {
            plugin.getBotProtectionManager().removeFromBlacklist(ip);
            sender.sendMessage("§aRemoved §e" + target + " §a(§7" + ip + "§a) from blacklist.");
        } else {
            sender.sendMessage("§cUsage: /tga blacklist <add|remove> <player|ip>");
        }
    }

    private void handleAttacks(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tga.attacks")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        int limit = 10;
        if (args.length > 1) {
            try {
                limit = Integer.parseInt(args[1]);
                limit = Math.min(limit, 50); // Max 50 attacks
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid number format.");
                return;
            }
        }

        List<AttackLog> recentAttacks = plugin.getAttackAnalyzer().getRecentAttacks(limit);

        if (recentAttacks.isEmpty()) {
            sender.sendMessage("§aNo recent attacks recorded.");
            return;
        }

        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lRECENT ATTACKS §7(Last " + limit + ")");
        sender.sendMessage("");

        for (AttackLog attack : recentAttacks) {
            sender.sendMessage(attack.getDetailedInfo());
        }

        sender.sendMessage("§8§l§n" + separator);
    }

    private void handleProfile(CommandSender sender, String[] args) {
        if (!sender.hasPermission("tga.profile")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /tga profile <player|ip>");
            return;
        }

        String target = args[1];
        String ip = getIPFromTarget(target);

        if (ip == null) {
            sender.sendMessage("§cPlayer not found or invalid IP address.");
            return;
        }

        PlayerProfile profile = plugin.getBotProtectionManager().getPlayerProfiles().get(ip);
        if (profile == null) {
            sender.sendMessage("§cNo profile found for " + target);
            return;
        }

        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lPLAYER PROFILE: §e" + target);
        sender.sendMessage("");
        sender.sendMessage("§7IP Address: §f" + profile.getIp());
        sender.sendMessage("§7Nicknames: §f" + String.join(", ", profile.getNicknames()));
        sender.sendMessage("§7Connections: §e" + profile.getConnectionCount());
        sender.sendMessage("§7Play Time: §a" + (profile.getPlayTime() / 1000) + "s");
        sender.sendMessage("§7Suspicion Score: §c" + profile.getSuspicionScore());
        sender.sendMessage("§7First Connection: §7" + new java.util.Date(profile.getFirstConnection()));
        sender.sendMessage("§7Last Connection: §7" + new java.util.Date(profile.getLastConnection()));
        sender.sendMessage("§8§l§n" + separator);
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("tga.toggle")) {
            player.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /tga toggle <actionbar|title|bossbar>");
            return;
        }

        String type = args[1].toLowerCase();
        switch (type) {
            case "actionbar":
                plugin.getNotificationManager().toggleActionBar(player);
                break;
            case "title":
                plugin.getNotificationManager().toggleTitle(player);
                break;
            case "bossbar":
                plugin.getNotificationManager().toggleBossBar(player);
                break;
            default:
                player.sendMessage("§cUsage: /tga toggle <actionbar|title|bossbar>");
                break;
        }
    }

    private String getIPFromTarget(String target) {
        if (target == null || target.trim().isEmpty()) {
            return null;
        }
        
        target = target.trim();
        
        // Check if it's already an IP
        if (target.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
            return target;
        }

        // Try to find online player
        Player player = Bukkit.getPlayerExact(target);
        if (player != null && player.getAddress() != null) {
            return player.getAddress().getAddress().getHostAddress();
        }

        return null;
    }

    private void sendHelp(CommandSender sender) {
        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lTG-ANTIBOT COMMANDS");
        sender.sendMessage("");
        sender.sendMessage("§e/tga help §7- Show this help menu");
        sender.sendMessage("§e/tga reload §7- Reload configuration");
        sender.sendMessage("§e/tga stats §7- Show protection statistics");
        sender.sendMessage("§e/tga status §7- Show current protection status");
        sender.sendMessage("§e/tga whitelist <add|remove> <player|ip> §7- Manage whitelist");
        sender.sendMessage("§e/tga blacklist <add|remove> <player|ip> §7- Manage blacklist");
        sender.sendMessage("§e/tga attacks [limit] §7- Show recent attacks");
        sender.sendMessage("§e/tga profile <player|ip> §7- Show player profile");
        sender.sendMessage("§e/tga toggle <actionbar|title|bossbar> §7- Toggle notifications");
        sender.sendMessage("§e/tga cache §7- Show cache statistics");
        sender.sendMessage("§e/tga debug §7- Show debug information");
        sender.sendMessage("");
        sender.sendMessage("§7Alias: §e/tgantibot §7can be used instead");
        sender.sendMessage("§8§l§n" + separator);
    }
    
    private void handleCache(CommandSender sender) {
        if (!sender.hasPermission("tga.cache")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }
        
        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lTG-ANTIBOT CACHE STATISTICS");
        sender.sendMessage("");
        sender.sendMessage("§7Config Cache: " + plugin.getConfigManager().getCacheSize() + " entries");
        sender.sendMessage("§7Message Cache: " + plugin.getMessages().getCacheStats());
        sender.sendMessage("§7VPN Cache: " + plugin.getVPNChecker().getCacheStats());
        sender.sendMessage("§7Attack Analyzer: " + plugin.getAttackAnalyzer().getMemoryStats());
        sender.sendMessage("§7Bot Protection: " + plugin.getBotProtectionManager().getStats());
        sender.sendMessage("§7Firewall: " + plugin.getFirewallManager().getStats());
        sender.sendMessage("§8§l§n" + separator);
    }
    
    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("tga.debug")) {
            sender.sendMessage(plugin.getMessages().get("no-permission"));
            return;
        }
        
        String separator = generateSeparator(50);
        sender.sendMessage("§8§l§n" + separator);
        sender.sendMessage("");
        sender.sendMessage("§c§lTG-ANTIBOT DEBUG INFORMATION");
        sender.sendMessage("");
        sender.sendMessage("§7Plugin Version: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§7Server Version: §f" + plugin.getServer().getVersion());
        sender.sendMessage("§7Java Version: §f" + System.getProperty("java.version"));
        sender.sendMessage("§7Debug Mode: §f" + plugin.getConfigManager().isDebugMode());
        sender.sendMessage("§7Performance Mode: §f" + plugin.getConfigManager().isPerformanceMode());
        sender.sendMessage("§7System Active: §f" + plugin.getConfigManager().isSystemActive());
        sender.sendMessage("");
        sender.sendMessage("§7Memory Usage:");
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1024 / 1024;
        long totalMemory = runtime.totalMemory() / 1024 / 1024;
        long freeMemory = runtime.freeMemory() / 1024 / 1024;
        long usedMemory = totalMemory - freeMemory;
        sender.sendMessage("§7  Used: §f" + usedMemory + "MB / " + maxMemory + "MB");
        sender.sendMessage("§7  Free: §f" + freeMemory + "MB");
        sender.sendMessage("§8§l§n" + separator);
    }

    // Helper method to generate separator string (Java 8 compatible)
    private String generateSeparator(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append('=');
        }
        return sb.toString();
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("help", "reload", "stats", "status",
                    "whitelist", "blacklist", "attacks", "profile", "toggle", "cache", "debug");

            for (String subCmd : subCommands) {
                if (sender.hasPermission("tga." + subCmd) || subCmd.equals("help")) {
                    completions.add(subCmd);
                }
            }

            return filterStartsWith(completions, args[0]);
        }

        if (args.length == 2) {
            String subCmd = args[0].toLowerCase();

            if ((subCmd.equals("whitelist") || subCmd.equals("blacklist")) &&
                    sender.hasPermission("tga." + subCmd)) {
                completions.addAll(Arrays.asList("add", "remove"));
            } else if (subCmd.equals("toggle") && sender.hasPermission("tga.toggle")) {
                completions.addAll(Arrays.asList("actionbar", "title", "bossbar"));
            } else if ((subCmd.equals("profile")) && sender.hasPermission("tga.profile")) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
            }

            return filterStartsWith(completions, args[1]);
        }

        if (args.length == 3) {
            String subCmd = args[0].toLowerCase();
            if ((subCmd.equals("whitelist") || subCmd.equals("blacklist")) &&
                    sender.hasPermission("tga." + subCmd)) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                return filterStartsWith(completions, args[2]);
            }
        }

        return completions;
    }

    private List<String> filterStartsWith(List<String> options, String input) {
        String lowerInput = input.toLowerCase(Locale.ROOT);
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lowerInput)) {
                filtered.add(option);
            }
        }
        return filtered;
    }
}