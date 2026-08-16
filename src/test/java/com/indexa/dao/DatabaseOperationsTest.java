package com.indexa.dao;

import com.indexa.database.DatabaseConnection;
import com.indexa.database.DatabaseInitializer;
import com.indexa.model.Document;
import com.indexa.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for DAO database operations. Runs against the real
 * indexa.db (SQLite is just a local file, so this is safe), and each
 * test cleans up its own inserted rows afterward so repeated test
 * runs never leave stale data behind.
 */
class DatabaseOperationsTest {

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final UserDAO userDAO = new UserDAO();

    private Integer insertedDocumentId;
    private String insertedUserEmail;

    @BeforeAll
    static void setUpDatabase() {
        DatabaseInitializer.initialize(); // ensures tables exist before any test runs
    }

    @AfterEach
    void cleanUp() throws SQLException {
        if (insertedDocumentId != null) {
            documentDAO.deleteDocument(insertedDocumentId);
            insertedDocumentId = null;
        }
        if (insertedUserEmail != null) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM USERS WHERE email = ?")) {
                stmt.setString(1, insertedUserEmail);
                stmt.executeUpdate();
            }
            insertedUserEmail = null;
        }
    }

    @Test
    void insertedDocumentCanBeRetrievedById() throws SQLException {
        Document doc = new Document("Test Document", "test/path.txt", "TXT", "Some content", 2);
        doc = documentDAO.insertDocument(doc);
        insertedDocumentId = doc.getId();

        assertTrue(doc.getId() > 0); // database should have assigned a real id

        Optional<Document> fetched = documentDAO.getDocumentById(doc.getId());
        assertTrue(fetched.isPresent());
        assertEquals("Test Document", fetched.get().getTitle());
    }

    @Test
    void deletedDocumentNoLongerExists() throws SQLException {
        Document doc = new Document("Temp Document", "test/path2.txt", "TXT", "Content", 1);
        doc = documentDAO.insertDocument(doc);
        int id = doc.getId();

        documentDAO.deleteDocument(id);
        insertedDocumentId = null; // already deleted, nothing left to clean up

        assertTrue(documentDAO.getDocumentById(id).isEmpty());
    }

    @Test
    void userCanRegisterAndThenLogIn() throws SQLException {
        String uniqueEmail = "test_" + System.nanoTime() + "@indexa.test";
        insertedUserEmail = uniqueEmail;

        User registered = userDAO.registerUser("Test User", uniqueEmail, "password123");
        assertTrue(registered.getId() > 0);

        // Correct password should validate successfully.
        Optional<User> loginResult = userDAO.validateLogin(uniqueEmail, "password123");
        assertTrue(loginResult.isPresent());
        assertEquals("Test User", loginResult.get().getName());

        // Wrong password must fail.
        assertTrue(userDAO.validateLogin(uniqueEmail, "wrongpassword").isEmpty());
    }

    @Test
    void passwordIsNeverStoredAsPlainText() throws SQLException {
        String uniqueEmail = "plaincheck_" + System.nanoTime() + "@indexa.test";
        insertedUserEmail = uniqueEmail;

        User registered = userDAO.registerUser("Plain Check", uniqueEmail, "mypassword");

        // The stored hash must never equal the raw password.
        assertNotEquals("mypassword", registered.getPasswordHash());
        assertTrue(registered.getPasswordHash().contains(":")); // "salt:hash" format
    }

    @Test
    void duplicateEmailIsDetected() throws SQLException {
        String uniqueEmail = "dup_" + System.nanoTime() + "@indexa.test";
        insertedUserEmail = uniqueEmail;

        userDAO.registerUser("First User", uniqueEmail, "password1");
        assertTrue(userDAO.emailExists(uniqueEmail));
    }
}
