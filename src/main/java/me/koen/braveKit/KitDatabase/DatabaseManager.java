package me.koen.braveKit.KitDatabase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.koen.braveKit.kit.Kit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.*;
import java.util.*;

import static org.bukkit.Bukkit.getConsoleSender;
import static org.bukkit.Bukkit.getLogger;

public class DatabaseManager {
    private HikariDataSource dataSource;
    private final FileConfiguration config;

    public DatabaseManager(FileConfiguration config) {
        this.config = config;
        try {
            setupHikariCP();
            createTables();

        } catch (SQLException e) {
            getLogger().severe("Database initialization failed: " + e.getMessage());
        }
    }

    public boolean isConntected() {
        return dataSource != null;
    }

    public void closeConnection() {
        dataSource.close();
    }

    private void setupHikariCP() {
        HikariConfig hikariConfig = new HikariConfig();

        // Configure HikariCP
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                config.getString("mysql.host"),
                config.getInt("mysql.port"),
                config.getString("mysql.database")));
        hikariConfig.setUsername(config.getString("mysql.username"));
        hikariConfig.setPassword(config.getString("mysql.password"));

        // HikariCP settings
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setMaxLifetime(600000);

        // Enable cache prep stmts
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(hikariConfig);
    }

    private void createTables() throws SQLException {
        String playerTableSQL = """
        CREATE TABLE IF NOT EXISTS player_data (
            uuid VARCHAR(36) PRIMARY KEY,
            username VARCHAR(16) NOT NULL,
            last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            play_time INT DEFAULT 0,
            INDEX idx_username (username)
        )
        """;

        String kitsTableSQL = """
        CREATE TABLE IF NOT EXISTS kits (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            icon TEXT,
            items TEXT,
            is_active BOOLEAN DEFAULT true,
            cooldown INT,
            permission VARCHAR(255)
        )
        """;

        String kitUsageTableSQL = """
        CREATE TABLE IF NOT EXISTS kit_usage (
            id INT AUTO_INCREMENT PRIMARY KEY,
            kit_id INT,
            player_uuid VARCHAR(36),
            used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (kit_id) REFERENCES kits(id)
        )
        """;

        // Check if the index exists before creating it
        String checkIndexSQL = """
        SELECT COUNT(1) AS index_exists
        FROM information_schema.statistics
        WHERE table_schema = DATABASE()
        AND table_name = 'kit_usage'
        AND index_name = 'idx_kit_usage'
        """;

        String createIndexSQL = """
        CREATE INDEX idx_kit_usage 
        ON kit_usage (kit_id, player_uuid)
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Create tables
            stmt.execute(playerTableSQL);
            stmt.execute(kitsTableSQL);
            stmt.execute(kitUsageTableSQL);

            // Check if the index exists
            ResultSet rs = stmt.executeQuery(checkIndexSQL);
            if (rs.next() && rs.getInt("index_exists") == 0) {
                // Index does not exist, so create it
                stmt.execute(createIndexSQL);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized!");
        }
        return dataSource.getConnection();
    }

    public void saveKit(Kit kit)  {
        String insertSQL = """
    INSERT INTO kits (name, description, icon, items, is_active, cooldown, permission)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
        ItemStack kitIcon = kit.getIcon();
        if(kit.getKitId() == -1){
            ItemMeta meta = kitIcon.getItemMeta();
            meta.setCustomModelData(getKitId());
            kitIcon.setItemMeta(meta);
        }

        ReadWriteNBT nbtIcon = NBT.itemStackToNBT(kitIcon);
        String jsonIcon = nbtIcon.toString();

        ItemStack[] kitItems = kit.getItems();
        ReadWriteNBT nbtItems = NBT.itemStackArrayToNBT(kitItems);
        String jsonItems = nbtItems.toString();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, kit.getName());
            stmt.setString(2, String.valueOf(kit.getDescription()));
            stmt.setString(3, jsonIcon);
            stmt.setString(4, jsonItems);
            stmt.setBoolean(5, true);
            stmt.setInt(6, 30);
            stmt.setString(7, "ability.use");
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean findKitByName(String name) {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM kits WHERE name = ?")) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next(); // Returns true if a row is found, otherwise false
            }
        } catch (SQLException e) {
            return false; // Return false if an exception occurs
        }
    }

    public Map<Integer, Kit> getAllKits() {
        int startId = 1;
        String selectSQL = """
            SELECT id, name, description, icon, items, is_active, cooldown, permission 
            FROM kits
            """;

        Map<Integer, Kit> kits = new HashMap<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Convert JSON string back to ItemStack for icon
                String jsonIcon = rs.getString("icon");
                ReadWriteNBT nbtIcon = NBT.parseNBT(jsonIcon);
                ItemStack icon = NBT.itemStackFromNBT(nbtIcon);

                // Convert JSON string back to ItemStack array for items
                String jsonItems = rs.getString("items");
                ReadWriteNBT nbtItems = NBT.parseNBT(jsonItems);
                ItemStack[] items = NBT.itemStackArrayFromNBT(nbtItems);

                String kitName = rs.getString("name");

                assert icon != null;
                Kit kit = new Kit(
                        startId,
                        kitName,
                        icon,
                        Collections.singletonList(rs.getString("description")),
                        items,
                        rs.getInt("cooldown")
                );
                startId++;
                kits.put(rs.getInt("id"), kit);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return kits;
    }

    public int getKitId(){
        int kitId = -1;
        String selectSQL = """
        SELECT COALESCE(MAX(id) + 1, 1) AS next_id FROM kits
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                kitId = rs.getInt("next_id");  // Now using the alias name
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        getConsoleSender().sendMessage(String.valueOf(kitId));
        return kitId;
    }

    /**
     * Check if a player can use a specific kit
     * @param player The player to check
     * @param kitId The kit ID to check
     * @param cooldownSeconds The cooldown duration in seconds
     * @return true if the player can use the kit, false if on cooldown
     */
    public boolean canUseKit(Player player, int kitId, int cooldownSeconds) {
        String query = "SELECT used_at FROM kit_usage " +
                "WHERE kit_id = ? AND player_uuid = ? " +
                "ORDER BY used_at DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, kitId);
            stmt.setString(2, player.getUniqueId().toString());

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return true; // No previous usage found
            }

            Timestamp lastUsed = rs.getTimestamp("used_at");
            long secondsSinceLastUse = (System.currentTimeMillis() - lastUsed.getTime()) / 1000;

            return secondsSinceLastUse >= cooldownSeconds;

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // In case of error, prevent usage
        }
    }

    /**
     * Record that a player has used a kit
     * @param player The player who used the kit
     * @param kitId The kit ID that was used
     */
    public void recordKitUsage(Player player, int kitId) {
        String query = "INSERT INTO kit_usage (kit_id, player_uuid, used_at) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE used_at = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            Timestamp now = new Timestamp(System.currentTimeMillis());

            stmt.setInt(1, kitId);
            stmt.setString(2, player.getUniqueId().toString());
            stmt.setTimestamp(3, now);
            stmt.setTimestamp(4, now); // This is for the ON DUPLICATE KEY UPDATE part

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
