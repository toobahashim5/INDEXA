package com.indexa.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsible for opening a connection to the local SQLite database file.
 *
 * SQLite is "embedded" - there is no separate database server running
 * in the background. The entire database lives in a single file called
 * indexa.db that sits next to the project. JDBC just gives Java a
 * standard way to talk to that file using SQL.
 */
public class DatabaseConnection {

    // The database file will be created in the project's root folder
    // the first time the app connects to it.
    private static final String DB_URL = "jdbc:sqlite:indexa.db";

    /**
     * Opens and returns a new connection to indexa.db.
     * Every DAO class (UserDAO, DocumentDAO, etc. - built in later steps)
     * will call this method whenever it needs to run a query.
     *
     * We deliberately do NOT keep one single shared connection open for
     * the whole app's lifetime. Opening a short-lived connection per
     * operation (and closing it with try-with-resources) is simpler and
     * avoids connection leaks - a bug you already ran into in the
     * Rock Paper Scissors project.
     */
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}
