package org.spigot.core.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.spigot.TGAntiBotPlugin;
import org.spigot.core.data.PlayerProfile;
import org.spigot.core.data.AttackLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Advanced Database Manager with connection pooling
 * Supports SQLite and MySQL databases
 */
public class DatabaseManager {

    private final TGAntiBotPlugin plugin;
    private HikariDataSource dataSource;
    private DatabaseType databaseType;

    public DatabaseManager(TGAntiBotPlugin plugin) {
        this.plugin = plugin;
        this.databaseType = DatabaseType.valueOf(
            plugin.getConfig().getString("database.type", "SQLITE").toUpperCase()
        );
        
        initializeDatabase();
        createTables();
    }

    private void initializeDatabase() {
        HikariConfig config = new HikariConfig();
        
        switch (databaseType) {
            case MYSQL:
                String host = plugin.getConfig().getString("database.mysql.host", "localhost");
                int port = plugin.getConfig().getInt("database.mysql.port", 3306);
                String database = plugin.getConfig().getString("database.mysql.database", "tgantibot");
                String username = plugin.getConfig().getString("database.mysql.username", "root");
                String password = plugin.getConfig().getString("database.mysql.password", "");
                
                config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC");
                config.setUsername(username);
                config.setPassword(password);
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                break;
                
            case SQLITE:
            default:
                config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/database.db");
                config.setDriverClassName("org.sqlite.JDBC");
                break;
        }
        
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
        
        plugin.getLogger().info("§a[Database] Connected to " + databaseType + " database");
    }

    private void createTables() {
        String playerProfilesTable = """
            CREATE TABLE IF NOT EXISTS player_profiles (
                ip VARCHAR(45) PRIMARY KEY,
                nicknames TEXT,
                connection_count INT DEFAULT 0,
                total_playtime BIGINT DEFAULT 0,
                first_connection BIGINT,
                last_connection BIGINT,
                suspicion_score INT DEFAULT 0,
                is_whitelisted BOOLEAN DEFAULT FALSE,
                is_blacklisted BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
            
        String attackLogsTable = """
            CREATE TABLE IF NOT EXISTS attack_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                attack_type VARCHAR(50),
                source_ip VARCHAR(45),
                intensity BIGINT,
                timestamp BIGINT,
                blocked BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
            
        String securityEventsTable = """
            CREATE TABLE IF NOT EXISTS security_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                event_type VARCHAR(50),
                ip_address VARCHAR(45),
                player_name VARCHAR(16),
                description TEXT,
                severity VARCHAR(20),
                timestamp BIGINT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = getConnection()) {
            conn.createStatement().execute(playerProfilesTable);
            conn.createStatement().execute(attackLogsTable);
            conn.createStatement().execute(securityEventsTable);
            
            plugin.getLogger().info("§a[Database] Tables created successfully");
        } catch (SQLException e) {
            plugin.getLogger().severe("§c[Database] Failed to create tables: " + e.getMessage());
        }
    }

    public CompletableFuture<Void> savePlayerProfile(PlayerProfile profile) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT OR REPLACE INTO player_profiles 
                (ip, nicknames, connection_count, total_playtime, first_connection, last_connection, suspicion_score, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
                
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, profile.getIp());
                stmt.setString(2, String.join(",", profile.getNicknames()));
                stmt.setInt(3, profile.getConnectionCount());
                stmt.setLong(4, profile.getPlayTime());
                stmt.setLong(5, profile.getFirstConnection());
                stmt.setLong(6, profile.getLastConnection());
                stmt.setInt(7, profile.getSuspicionScore());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("§e[Database] Failed to save player profile: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<PlayerProfile> loadPlayerProfile(String ip) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM player_profiles WHERE ip = ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, ip);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    PlayerProfile profile = new PlayerProfile(ip);
                    
                    String[] nicknames = rs.getString("nicknames").split(",");
                    for (String nickname : nicknames) {
                        if (!nickname.trim().isEmpty()) {
                            profile.getNicknames().add(nickname.trim());
                        }
                    }
                    
                    // Set other profile data
                    profile.addPlayTime(rs.getLong("total_playtime"));
                    
                    return profile;
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("§e[Database] Failed to load player profile: " + e.getMessage());
            }
            
            return null;
        });
    }

    public CompletableFuture<Void> logAttack(AttackLog attackLog) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO attack_logs (attack_type, source_ip, intensity, timestamp, blocked)
                VALUES (?, ?, ?, ?, ?)
                """;
                
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, attackLog.getType().getName());
                stmt.setString(2, attackLog.getSourceIP());
                stmt.setLong(3, attackLog.getIntensity());
                stmt.setLong(4, attackLog.getTimestamp());
                stmt.setBoolean(5, true);
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("§e[Database] Failed to log attack: " + e.getMessage());
            }
        });
    }

    public CompletableFuture<List<AttackLog>> getRecentAttacks(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<AttackLog> attacks = new ArrayList<>();
            String sql = "SELECT * FROM attack_logs ORDER BY timestamp DESC LIMIT ?";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    // Create AttackLog from database data
                    // This would need to be implemented based on your AttackLog constructor
                }
            } catch (SQLException e) {
                plugin.getLogger().warning("§e[Database] Failed to get recent attacks: " + e.getMessage());
            }
            
            return attacks;
        });
    }

    public CompletableFuture<Void> logSecurityEvent(String eventType, String ip, String playerName, 
                                                   String description, String severity) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO security_events (event_type, ip_address, player_name, description, severity, timestamp)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
                
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, eventType);
                stmt.setString(2, ip);
                stmt.setString(3, playerName);
                stmt.setString(4, description);
                stmt.setString(5, severity);
                stmt.setLong(6, System.currentTimeMillis());
                
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().warning("§e[Database] Failed to log security event: " + e.getMessage());
            }
        });
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("§a[Database] Connection pool closed");
        }
    }

    public enum DatabaseType {
        SQLITE, MYSQL
    }
}