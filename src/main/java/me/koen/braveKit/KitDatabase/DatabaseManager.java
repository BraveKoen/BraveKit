package me.koen.braveKit.KitDatabase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import me.koen.braveKit.kit.Kit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(playerTableSQL)) {
            stmt.executeUpdate();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("Database connection pool is not initialized!");
        }
        return dataSource.getConnection();
    }

    public void SaveKit(Kit kit)  {
        String insertSQL = """
    INSERT INTO kits (name, description, icon, items, is_active, cooldown, permission)
    VALUES (?, ?, ?, ?, ?, ?, ?)
    """;

        ItemStack kitIcon = kit.getIcon();
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

    public Map<Integer, Kit> getAllKits() {
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
                        rs.getInt("id"),
                        kitName,
                        icon,
                        Collections.singletonList(rs.getString("description")),
                        items,
                        rs.getInt("cooldown")
                );
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
}
