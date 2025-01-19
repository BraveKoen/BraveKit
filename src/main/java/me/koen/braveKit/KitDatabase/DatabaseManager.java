package me.koen.braveKit.KitDatabase;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class DatabaseManager {
    private HikariDataSource dataSource;
    private FileConfiguration config;

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
}
