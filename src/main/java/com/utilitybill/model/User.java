package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a user in the Utility Bill Management System.
 * Users can be administrators or standard users with different access levels.
 *
 * <p>This class demonstrates proper encapsulation with private fields
 * and public getter/setter methods, following JavaBean conventions.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public class User implements Serializable {

    /** Unique identifier for serialization */
    private static final long serialVersionUID = 1L;

    /** Unique user identifier (UUID) */
    private String userId;

    /** Username for login (unique) */
    private String username;

    /** Hashed password */
    private String passwordHash;

    /** User's full name */
    private String fullName;

    /** User's email address */
    private String email;

    /** User's role in the system */
    private UserRole role;

    /** Whether the account is active */
    private boolean active;

    /** Account creation timestamp */
    private LocalDateTime createdAt;

    /** Last login timestamp */
    private LocalDateTime lastLoginAt;

    /** Number of consecutive failed login attempts */
    private int failedLoginAttempts;

    /** Whether the account is locked */
    private boolean locked;

    /**
     * Enum representing user roles in the system.
     */
    public enum UserRole {
        /** System administrator with full access */
        ADMIN("Administrator", true, true, true, true),
        /** Standard operator with limited access */
        OPERATOR("Operator", true, true, true, false),
        /** Read-only viewer */
        VIEWER("Viewer", true, false, false, false);

        private final String displayName;
        private final boolean canRead;
        private final boolean canWrite;
        private final boolean canUpdate;
        private final boolean canDelete;

        UserRole(String displayName, boolean canRead, boolean canWrite,
                 boolean canUpdate, boolean canDelete) {
            this.displayName = displayName;
            this.canRead = canRead;
            this.canWrite = canWrite;
            this.canUpdate = canUpdate;
            this.canDelete = canDelete;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean canRead() {
            return canRead;
        }

        public boolean canWrite() {
            return canWrite;
        }

        public boolean canUpdate() {
            return canUpdate;
        }

        public boolean canDelete() {
            return canDelete;
        }
    }

    /**
     * Default constructor required for JSON deserialization.
     */
    public User() {
        this.userId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.locked = false;
        this.failedLoginAttempts = 0;
        this.role = UserRole.OPERATOR;
    }

    /**
     * Constructs a new User with essential details.
     *
     * @param username username for login
     * @param passwordHash hashed password
     * @param fullName user's full name
     * @param email user's email address
     * @param role user's role
     */
    public User(String username, String passwordHash, String fullName, String email, UserRole role) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    // ==================== Getters and Setters ====================

    /**
     * Gets the unique user identifier.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the unique user identifier.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password hash.
     *
     * @return the password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the password hash.
     *
     * @param passwordHash the password hash to set
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Gets the user's full name.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the user's full name.
     *
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the user's email.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email.
     *
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the user's role.
     *
     * @return the user role
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role the role to set
     */
    public void setRole(UserRole role) {
        this.role = role;
    }

    /**
     * Checks if the account is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the account active status.
     *
     * @param active the active status to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the account creation timestamp.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the account creation timestamp.
     *
     * @param createdAt the creation timestamp to set
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last login timestamp.
     *
     * @return the last login timestamp
     */
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    /**
     * Sets the last login timestamp.
     *
     * @param lastLoginAt the last login timestamp to set
     */
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    /**
     * Gets the number of failed login attempts.
     *
     * @return the failed login attempt count
     */
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    /**
     * Sets the number of failed login attempts.
     *
     * @param failedLoginAttempts the count to set
     */
    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    /**
     * Checks if the account is locked.
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the account locked status.
     *
     * @param locked the locked status to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    // ==================== Business Methods ====================

    /**
     * Records a successful login, resetting failed attempts.
     */
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    /**
     * Records a failed login attempt.
     *
     * @return true if account should be locked (3+ attempts)
     */
    public boolean recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.locked = true;
            return true;
        }
        return false;
    }

    /**
     * Unlocks the user account.
     */
    public void unlock() {
        this.locked = false;
        this.failedLoginAttempts = 0;
    }

    /**
     * Checks if the user can perform an action.
     *
     * @return true if user is active and not locked
     */
    public boolean canPerformActions() {
        return active && !locked;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("User{userId='%s', username='%s', fullName='%s', role=%s, active=%s}",
                userId, username, fullName, role, active);
    }
}

