package com.bussheba.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place that opens a JDBC connection to the BusSheba PostgreSQL
 * database you created in pgAdmin 4.
 *
 * FILL IN YOUR ACTUAL VALUES BELOW:
 *   - DB_NAME: the database name you gave it in pgAdmin (e.g. "bussheba")
 *   - USER / PASSWORD: your PostgreSQL login (the one you use to open
 *     pgAdmin's Query Tool on this database)
 *   - HOST / PORT: only change these if your PostgreSQL server isn't
 *     running locally on the default port 5432
 *
 * Every DAO calls DBConnection.getConnection() to get a fresh connection,
 * and is responsible for closing it (DAOs below use try-with-resources so
 * this happens automatically).
 */
public class DBConnection {

    private static final String HOST = "localhost";
    private static final String PORT = "7000";
    private static final String DB_NAME = "busSheba";      // TODO: replace with your actual DB name
    private static final String USER = "postgres";         // TODO: replace with your actual username
    private static final String PASSWORD = "5659"; // TODO: replace with your actual password

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DB_NAME;

    static {
        try {
            // Registers the PostgreSQL driver (from the pom.xml dependency).
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "PostgreSQL JDBC driver not found. Check the postgresql dependency in pom.xml.", e);
        }
    }

    private DBConnection() {
        // Utility class; not meant to be instantiated.
    }

    /**
     * Opens a new connection to the database. Callers should use
     * try-with-resources so it's closed automatically, e.g.:
     *
     *   try (Connection conn = DBConnection.getConnection()) {
     *       ...
     *   }
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Quick manual test: run this class directly to confirm your
     * credentials and pgAdmin database are reachable before wiring up
     * any DAOs.
     */
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connected successfully to: " + conn.getCatalog());
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
