package dev.labs.httpserver.db;

import java.sql.Connection;
import java.sql.SQLException;

public class SimpleTransactionManager {

    private final DatabaseConfig dbConfig;

    public SimpleTransactionManager(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void begin() throws SQLException {
        Connection conn = dbConfig.getConnection();
        conn.setAutoCommit(false);
        ConnectionContext.bind(conn);
    }

    public void commit() throws SQLException {
        ConnectionContext.get().commit();
    }

    public void rollback() throws SQLException {
        Connection conn = ConnectionContext.get();
        if (conn != null) conn.rollback();
    }

    public void end() throws SQLException {
        Connection conn = ConnectionContext.get();
        ConnectionContext.clear();
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
