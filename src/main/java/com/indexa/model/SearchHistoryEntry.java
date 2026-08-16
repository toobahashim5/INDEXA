package com.indexa.model;

/**
 * Represents a row in the SEARCH_HISTORY table - one past search a
 * logged-in user performed. Guests never generate these, per the
 * project's "guests don't get persistent history" rule.
 */
public class SearchHistoryEntry {

    private int id;
    private int userId;
    private String query;
    private String searchedAt;

    public SearchHistoryEntry(int id, int userId, String query, String searchedAt) {
        this.id = id;
        this.userId = userId;
        this.query = query;
        this.searchedAt = searchedAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getQuery() {
        return query;
    }

    public String getSearchedAt() {
        return searchedAt;
    }

    @Override
    public String toString() {
        return "SearchHistoryEntry{query='" + query + "', searchedAt='" + searchedAt + "'}";
    }
}
