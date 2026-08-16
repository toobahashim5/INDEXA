package com.indexa.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates all required tables in indexa.db if they do not already exist.
 *
 * This class is meant to be called once, right when the application
 * starts (see Main.java). Using "CREATE TABLE IF NOT EXISTS" means it
 * is safe to call every single time the app launches - on the first
 * run it builds the schema, on every run after that it does nothing.
 */
public class DatabaseInitializer {

    public static void initialize() {
        // try-with-resources: the Connection and Statement are
        // automatically closed when this block finishes, even if an
        // error happens. No manual close() calls, no leaked connections.
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(CREATE_USERS_TABLE);
            stmt.execute(CREATE_DOCUMENTS_TABLE);
            stmt.execute(CREATE_SEARCH_HISTORY_TABLE);
            stmt.execute(CREATE_BOOKMARKS_TABLE);

            System.out.println("[INDEXA] Database initialized successfully (indexa.db).");

        } catch (SQLException e) {
            // We never show raw stack traces to the end user (per the
            // project's error-handling rule). At startup, before any
            // UI exists, printing to console is acceptable - later,
            // JavaFX Alert dialogs will handle user-facing DB errors.
            System.err.println("[INDEXA] Failed to initialize database: " + e.getMessage());
        }
    }

    // ---------- Table definitions ----------

    private static final String CREATE_USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS USERS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
        """;

    private static final String CREATE_DOCUMENTS_TABLE = """
        CREATE TABLE IF NOT EXISTS DOCUMENTS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            file_path TEXT NOT NULL,
            file_type TEXT NOT NULL,
            content TEXT,
            word_count INTEGER DEFAULT 0,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
        """;

    private static final String CREATE_SEARCH_HISTORY_TABLE = """
        CREATE TABLE IF NOT EXISTS SEARCH_HISTORY (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            query TEXT NOT NULL,
            searched_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES USERS(id)
        );
        """;

    private static final String CREATE_BOOKMARKS_TABLE = """
        CREATE TABLE IF NOT EXISTS BOOKMARKS (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            user_id INTEGER NOT NULL,
            document_id INTEGER NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            FOREIGN KEY (user_id) REFERENCES USERS(id),
            FOREIGN KEY (document_id) REFERENCES DOCUMENTS(id),
            UNIQUE (user_id, document_id)
        );
        """;
}
