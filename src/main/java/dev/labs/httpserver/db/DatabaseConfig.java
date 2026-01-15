package dev.labs.httpserver.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig forProduction() {
        return new DatabaseConfig(
                "jdbc:h2:mem:production;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
    }

    public static DatabaseConfig forTest() {
        return new DatabaseConfig(
                "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void initializeSchema() {
        try (Connection conn = getConnection()) {
            // todos 테이블
            conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS todos (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id VARCHAR(255) NOT NULL,
                        title VARCHAR(255) NOT NULL,
                        completed BOOLEAN NOT NULL DEFAULT FALSE
                    )
                    """);

            // todo_stats 테이블
            conn.createStatement().execute("""
                    CREATE TABLE IF NOT EXISTS todo_stats (
                        user_id VARCHAR(255) PRIMARY KEY,
                        total_count INT NOT NULL DEFAULT 0,
                        completed_count INT NOT NULL DEFAULT 0
                    )
                    """);

            log.info("Database schema initialized");
        } catch (SQLException e) {
            log.error("Failed to initialize database schema", e);
            throw new RuntimeException(e);
        }
    }

    public void cleanUp() {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("TRUNCATE TABLE todos");
            conn.createStatement().execute("TRUNCATE TABLE todo_stats");
            log.debug("All data cleared and sequences reset");
        } catch (SQLException e) {
            log.error("Failed to clear database", e);
            throw new RuntimeException(e);
        }
    }

}
