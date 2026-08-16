package com.indexa.dao;

import com.indexa.database.DatabaseConnection;
import com.indexa.model.User;
import com.indexa.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Data Access Object for the USERS table. Every method uses
 * try-with-resources for its Connection/Statement, and every query
 * uses PreparedStatement - never string-concatenated SQL.
 */
public class UserDAO {

    /**
     * Registers a new user. Hashes the password before it ever
     * touches the database (PasswordUtil, Step 12). Throws SQLException
     * with a message the UI can show if the email is already taken -
     * the USERS.email UNIQUE constraint (DatabaseInitializer) enforces
     * this at the database level as a second line of defense.
     */
    public User registerUser(String name, String email, String plainPassword) throws SQLException {
        String passwordHash = PasswordUtil.hashPassword(plainPassword);
        String sql = "INSERT INTO USERS (name, email, password_hash) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, passwordHash);
            stmt.executeUpdate();

            User user = new User(name, email, passwordHash);
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
            return user;
        }
    }

    /**
     * Looks up a user by email. Used both for login (to fetch the
     * stored hash for verification) and for checking whether an email
     * is already registered.
     */
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM USERS WHERE email = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean emailExists(String email) throws SQLException {
        return findByEmail(email).isPresent();
    }

    /**
     * Validates a login attempt. Returns the User if the email exists
     * AND the password matches; returns Optional.empty() otherwise -
     * deliberately not distinguishing "wrong email" from "wrong
     * password" in the return value, so the UI shows one generic
     * "Invalid email or password" message (avoids leaking which
     * emails are registered).
     */
    public Optional<User> validateLogin(String email, String plainPassword) throws SQLException {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (PasswordUtil.verifyPassword(plainPassword, user.getPasswordHash())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    // ---------- Internal helper ----------

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getString("created_at")
        );
    }
}
