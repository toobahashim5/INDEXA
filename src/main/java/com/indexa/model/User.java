package com.indexa.model;

/**
 * Represents a registered user (mirrors the USERS table).
 *
 * Note: this class holds the password HASH, never the plain-text
 * password. Hashing itself happens in util/PasswordUtil.java (built
 * in a later step) before a User object is ever created or saved.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String createdAt;

    // Used when creating a brand-new user (before it has an id/createdAt,
    // which the database assigns automatically).
    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Used when loading an existing user back out of the database.
    public User(int id, String name, String email, String passwordHash, String createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
    }
}
