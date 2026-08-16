package com.indexa.dao;

import com.indexa.database.DatabaseConnection;
import com.indexa.model.Bookmark;
import com.indexa.model.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the BOOKMARKS table.
 *
 * Duplicate bookmarks are prevented at TWO levels, per good practice:
 *   1. Here in code, via isBookmarked() checked before inserting.
 *   2. At the database level, via the UNIQUE(user_id, document_id)
 *      constraint set up in DatabaseInitializer (Step 2) - this is
 *      the real guarantee, since it holds even under concurrent access.
 */
public class BookmarkDAO {

    public void addBookmark(int userId, int documentId) throws SQLException {
        String sql = "INSERT INTO BOOKMARKS (user_id, document_id) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, documentId);
            stmt.executeUpdate();
        }
    }

    public void removeBookmark(int userId, int documentId) throws SQLException {
        String sql = "DELETE FROM BOOKMARKS WHERE user_id = ? AND document_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, documentId);
            stmt.executeUpdate();
        }
    }

    public boolean isBookmarked(int userId, int documentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM BOOKMARKS WHERE user_id = ? AND document_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, documentId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Returns the full Document for every bookmark a user has saved,
     * newest first - joins BOOKMARKS with DOCUMENTS so the caller gets
     * ready-to-display documents, not just raw document IDs.
     */
    public List<Document> getBookmarkedDocuments(int userId) throws SQLException {
        String sql = "SELECT d.* FROM DOCUMENTS d " +
                     "JOIN BOOKMARKS b ON d.id = b.document_id " +
                     "WHERE b.user_id = ? ORDER BY b.created_at DESC, b.id DESC";
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documents.add(new Document(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("file_path"),
                            rs.getString("file_type"),
                            rs.getString("content"),
                            rs.getInt("word_count"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return documents;
    }
}
