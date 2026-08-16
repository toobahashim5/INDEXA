package com.indexa.dao;

import com.indexa.database.DatabaseConnection;
import com.indexa.model.Document;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for the DOCUMENTS table.
 *
 * Every method opens its own short-lived connection using
 * try-with-resources, so connections are always closed automatically -
 * even if an exception occurs. This avoids the connection-leak bug
 * from the earlier Rock Paper Scissors project.
 *
 * All queries use PreparedStatement (never string-concatenated SQL),
 * per the project's security rules.
 *
 * Important: this class only stores document METADATA and raw content
 * in SQLite. It has nothing to do with searching - the actual search
 * mechanism is the in-memory InvertedIndex/Trie built by
 * IndexingService.
 */
public class DocumentDAO {

    /**
     * Inserts a new document row and returns the same Document object
     * with its database-generated id filled in (needed so the caller
     * can use that id when adding entries to the InvertedIndex/Trie).
     */
    public Document insertDocument(Document doc) throws SQLException {
        String sql = "INSERT INTO DOCUMENTS (title, file_path, file_type, content, word_count) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, doc.getTitle());
            stmt.setString(2, doc.getFilePath());
            stmt.setString(3, doc.getFileType());
            stmt.setString(4, doc.getContent());
            stmt.setInt(5, doc.getWordCount());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    doc.setId(keys.getInt(1));
                }
            }
        }
        return doc;
    }

    /**
     * Returns every indexed document, ordered by newest first.
     * Used by the Documents screen (TableView) and Re-index All.
     */
    public List<Document> getAllDocuments() throws SQLException {
        String sql = "SELECT * FROM DOCUMENTS ORDER BY id DESC";
        List<Document> documents = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                documents.add(mapRow(rs));
            }
        }
        return documents;
    }

    /**
     * Looks up a single document by id. Returns Optional.empty() if no
     * document has that id, instead of returning null.
     */
    public Optional<Document> getDocumentById(int id) throws SQLException {
        String sql = "SELECT * FROM DOCUMENTS WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks whether a document from this exact file path has already
     * been indexed, so re-running indexing doesn't create duplicate rows.
     */
    public boolean existsByFilePath(String filePath) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DOCUMENTS WHERE file_path = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, filePath);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /**
     * Deletes a single document by id.
     */
    public void deleteDocument(int id) throws SQLException {
        String sql = "DELETE FROM DOCUMENTS WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes every document row. Used before a full "Re-index All",
     * so old rows don't pile up every time indexing runs.
     */
    public void deleteAllDocuments() throws SQLException {
        String sql = "DELETE FROM DOCUMENTS";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
        }
    }

    /**
     * Total number of indexed documents - used on the Index Statistics
     * screen.
     */
    public int getDocumentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM DOCUMENTS";

        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // ---------- Internal helper ----------

    private Document mapRow(ResultSet rs) throws SQLException {
        return new Document(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("file_path"),
                rs.getString("file_type"),
                rs.getString("content"),
                rs.getInt("word_count"),
                rs.getString("created_at")
        );
    }
}
