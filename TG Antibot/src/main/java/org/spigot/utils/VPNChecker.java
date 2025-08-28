package org.spigot.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.entity.Player;
import org.spigot.Main;

import java.io.IOException;
import java.net.InetAddress;

public class VPNChecker {

    private final Main plugin;
    private final OkHttpClient httpClient;
    private final ObjectMapper jsonMapper;

    public VPNChecker(Main plugin) {
        this.plugin = plugin;
        this.httpClient = new OkHttpClient();
        this.jsonMapper = new ObjectMapper();
    }

    /**
     * Check if a Player is using a VPN.
     */
    public boolean isUsingVPN(Player player) {
        if (player == null || player.getAddress() == null) return false;
        return isUsingVPN(player.getAddress().getAddress().getHostAddress());
    }

    /**
     * Check if an IP address is using a VPN (for AsyncPlayerPreLoginEvent or raw IP checks).
     */
    public boolean isUsingVPN(String ip) {
        if (!plugin.getConfigManager().isVpnProtectionEnabled()) return false;

        if (plugin.getConfigManager().isDebugMode()) {
            plugin.getLogger().info("[VPNChecker] Checking IP: " + ip);
        }

        try {
            String apiKey = plugin.getConfigManager().getVpnApiKey();
            String url = "https://proxycheck.io/v2/" + ip + "?vpn=1&asn=1" +
                    (apiKey.isEmpty() ? "" : "&key=" + apiKey);

            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    plugin.getLogger().warning("[VPNChecker] API request failed: " + response.code());
                    return false;
                }

                String body = response.body().string();
                JsonNode json = jsonMapper.readTree(body);

                if (json.has(ip)) {
                    String proxy = json.get(ip).path("proxy").asText();
                    return "yes".equalsIgnoreCase(proxy);
                }
            }
        } catch (IOException e) {
            plugin.getLogger().warning("[VPNChecker] Error checking VPN for IP " + ip + ": " + e.getMessage());
        } catch (Exception e) {
            plugin.getLogger().warning("[VPNChecker] Unexpected error for IP " + ip + ": " + e.getMessage());
        }

        return false;
    }
}
