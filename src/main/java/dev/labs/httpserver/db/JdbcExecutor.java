package dev.labs.httpserver.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class JdbcExecutor {

    private static final Logger log = LoggerFactory.getLogger(JdbcExecutor.class);

    private final DatabaseConfig dbConfig;

    public JdbcExecutor(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public <T> T execute(ConnectionCallback<T> callback) {
        Connection conn = null;
        try {
            conn = ConnectionProvider.get(dbConfig);
            return callback.doInConnection(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    ConnectionProvider.release(conn);
                } catch (SQLException e) {
                    log.warn("Failed to release connection", e);
                }
            }
        }
    }

}
