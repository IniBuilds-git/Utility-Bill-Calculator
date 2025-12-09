package com.utilitybill.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String passwordHash;
    private String fullName;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private int failedLoginAttempts;
    private boolean locked;

    public enum UserRole {
        ADMIN("Administrator", true, true, true, true),
        OPERATOR("Operator", true, true, true, false),
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

    public User() {
        this.userId = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.active = true;
        this.locked = false;
        this.failedLoginAttempts = 0;
        this.role = UserRole.OPERATOR;
    }

    public User(String username, String passwordHash, String fullName, String email, UserRole role) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public boolean recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 3) {
            this.locked = true;
            return true;
        }
        return false;
    }

    public void unlock() {
        this.locked = false;
        this.failedLoginAttempts = 0;
    }

    public boolean canPerformActions() {
        return active && !locked;
    }

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

