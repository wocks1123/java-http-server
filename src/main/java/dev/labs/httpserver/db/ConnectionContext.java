package dev.labs.httpserver.db;

import java.sql.Connection;

public final class ConnectionContext {

    private ConnectionContext() {
    }

    private static final ThreadLocal<Connection> current = new ThreadLocal<>();

    public static void bind(Connection connection) {
        current.set(connection);
    }

    public static Connection get() {
        return current.get();
    }

    public static void clear() {
        current.remove();
    }

    public static boolean hasConnection() {
        return current.get() != null;
    }
}
