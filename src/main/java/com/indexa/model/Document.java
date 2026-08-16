package com.indexa.model;

/**
 * Represents an indexed document (mirrors the DOCUMENTS table).
 *
 * Important: this class only stores metadata + raw content in SQLite.
 * The actual searchable index (which keywords point to this document)
 * lives separately in the in-memory InvertedIndex and Trie (Step 5/6),
 * NOT in the database, per the project rules.
 */
public class Document {

    private int id;
    private String title;
    private String filePath;
    private String fileType;
    private String content;
    private int wordCount;
    private String createdAt;

    // Used when adding a brand-new document (before it has a database id).
    public Document(String title, String filePath, String fileType, String content, int wordCount) {
        this.title = title;
        this.filePath = filePath;
        this.fileType = fileType;
        this.content = content;
        this.wordCount = wordCount;
    }

    // Used when loading an existing document back out of the database.
    public Document(int id, String title, String filePath, String fileType,
                     String content, int wordCount, String createdAt) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.fileType = fileType;
        this.content = content;
        this.wordCount = wordCount;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Document{id=" + id + ", title='" + title + "', type='" + fileType + "'}";
    }
}
