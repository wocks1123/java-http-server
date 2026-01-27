package dev.labs.httpserver.db;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionProvider {

    public static Connection get(DatabaseConfig dbConfig) throws SQLException {
        if (ConnectionContext.hasConnection()) {
            return ConnectionContext.get();
        }
        return dbConfig.getConnection();
    }

    public static void release(Connection conn) throws SQLException {
        if (ConnectionContext.hasConnection()) {
            return;
        }
        conn.close();
    }

}
