package com.indexa.model;

/**
 * Represents a saved bookmark (mirrors the BOOKMARKS table).
 *
 * A bookmark just links a user_id to a document_id. The database's
 * UNIQUE(user_id, document_id) constraint (see DatabaseInitializer)
 * is what actually prevents duplicate bookmarks - this class just
 * carries the data.
 */
public class Bookmark {

    private int id;
    private int userId;
    private int documentId;
    private String createdAt;

    // Used when creating a brand-new bookmark.
    public Bookmark(int userId, int documentId) {
        this.userId = userId;
        this.documentId = documentId;
    }

    // Used when loading an existing bookmark back out of the database.
    public Bookmark(int id, int userId, int documentId, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.documentId = documentId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDocumentId() {
        return documentId;
    }

    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Bookmark{userId=" + userId + ", documentId=" + documentId + "}";
    }
}
