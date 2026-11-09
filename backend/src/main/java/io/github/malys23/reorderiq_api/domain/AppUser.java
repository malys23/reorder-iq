package io.github.malys23.reorderiq_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name="users")
public class AppUser {
    @Id
    private Integer userId;

    private String username;

    private String passwordHash;

    private OffsetDateTime createdAt;

    private OffsetDateTime lastLogin;

    private String userRole;

    protected AppUser() {}

    public AppUser(Integer userId, String username, String passwordHash, OffsetDateTime lastLogin, String userRole){
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = OffsetDateTime.now();
        this.lastLogin = lastLogin;
        this.userRole = userRole;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastLogin() {
        return lastLogin;
    }

    public String getUserRole() {
        return userRole;
    }
}
