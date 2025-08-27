package org.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigot.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReloadCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public ReloadCommand(Main plugin) {
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
                if (!sender.hasPermission("tga.reload")) {
                    sender.sendMessage(plugin.getMessages().get("no-permission",
                            "&cYou don't have permission!"));
                    return true;
                }
                plugin.reloadConfig();
                plugin.getConfigManager().reload();
                plugin.getMessages().reload();
                sender.sendMessage(plugin.getMessages().get("reload", "&aTG-AntiBot config reloaded."));
                break;

            case "ipblock":
                if (!sender.hasPermission("tga.ipblock")) {
                    sender.sendMessage(plugin.getMessages().get("no-permission",
                            "&cYou don't have permission!"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessages().get("ipblock-usage",
                            "&cUsage: /tga ipblock <player>"));
                    return true;
                }
                plugin.getFirewallChecker().blockPlayer(args[1]);
                sender.sendMessage(plugin.getMessages().get("ipblock-success",
                        "&aBlocked IP of player: &e" + args[1]));
                break;

            case "ipwhitelist":
                if (!sender.hasPermission("tga.ipwhitelist")) {
                    sender.sendMessage(plugin.getMessages().get("no-permission",
                            "&cYou don't have permission!"));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessages().get("ipwhitelist-usage",
                            "&cUsage: /tga ipwhitelist <player>"));
                    return true;
                }
                plugin.getFirewallChecker().whitelistPlayer(args[1]);
                sender.sendMessage(plugin.getMessages().get("ipwhitelist-success",
                        "&aWhitelisted IP of player: &e" + args[1]));
                break;

            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7================= &cTG-AntiBot Help &7================="));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a/tgantibot reload &7- Reloads the config and messages"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a/tgantibot ipblock <player> &7- Block a player's IP"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a/tgantibot ipwhitelist <player> &7- Whitelist a player's IP"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&a/tgantibot help &7- Shows this help menu"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7Alias: &a/tga &7can be used instead of &a/tgantibot"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                "&7==============================================="));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("tga.reload")) completions.add("reload");
            if (sender.hasPermission("tga.ipblock")) completions.add("ipblock");
            if (sender.hasPermission("tga.ipwhitelist")) completions.add("ipwhitelist");
            completions.add("help");

            return filterStartsWith(completions, args[0]);
        }

        if (args.length == 2) {
            if ((args[0].equalsIgnoreCase("ipblock") && sender.hasPermission("tga.ipblock")) ||
                    (args[0].equalsIgnoreCase("ipwhitelist") && sender.hasPermission("tga.ipwhitelist"))) {
                Bukkit.getOnlinePlayers().forEach(p -> completions.add(p.getName()));
                return filterStartsWith(completions, args[1]);
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
