package com.indexa.dao;

import com.indexa.database.DatabaseConnection;
import com.indexa.model.SearchHistoryEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the SEARCH_HISTORY table. Only ever called
 * for logged-in users - HomeController checks currentUser != null
 * before recording a search, since guests don't get persistent history.
 */
public class HistoryDAO {

    public void recordSearch(int userId, String query) throws SQLException {
        String sql = "INSERT INTO SEARCH_HISTORY (user_id, query) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, query);
            stmt.executeUpdate();
        }
    }

    /**
     * Returns a user's search history, most recent first.
     */
    public List<SearchHistoryEntry> getHistoryForUser(int userId) throws SQLException {
        String sql = "SELECT * FROM SEARCH_HISTORY WHERE user_id = ? ORDER BY searched_at DESC, id DESC";
        List<SearchHistoryEntry> history = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    history.add(mapRow(rs));
                }
            }
        }
        return history;
    }

    public void deleteEntry(int historyId) throws SQLException {
        String sql = "DELETE FROM SEARCH_HISTORY WHERE id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, historyId);
            stmt.executeUpdate();
        }
    }

    public void clearHistoryForUser(int userId) throws SQLException {
        String sql = "DELETE FROM SEARCH_HISTORY WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    private SearchHistoryEntry mapRow(ResultSet rs) throws SQLException {
        return new SearchHistoryEntry(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("query"),
                rs.getString("searched_at")
        );
    }
}
