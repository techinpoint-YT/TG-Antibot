package org.spigot.core.checks;

import org.spigot.Main;
import org.spigot.core.data.PlayerProfile;

public class AccountLimitCheck {
    private final Main plugin;

    public AccountLimitCheck(Main plugin) {
        this.plugin = plugin;
    }

    public boolean shouldBlock(PlayerProfile profile) {
        if (!plugin.getConfigManager().isAccountLimitCheckEnabled()) {
            return false;
        }

        int maxAccounts = plugin.getConfigManager().getMaxAccountsPerIP();
        int currentAccounts = profile.getNicknames().size();

        if (currentAccounts > maxAccounts) {
            plugin.getLogger().info("§cBlocked IP " + profile.getIp() + " - Too many accounts: " + currentAccounts + "/" + maxAccounts);
            return true;
        }

        return false;
    }
}