package com.bussheba.model;

import java.time.LocalDateTime;

/**
 * Maps to the `users` table.
 *
 * id                SERIAL PRIMARY KEY
 * name              VARCHAR(100) NOT NULL
 * email             VARCHAR(150) NOT NULL UNIQUE
 * password_hash     VARCHAR(255) NOT NULL
 * role              VARCHAR(30) NOT NULL DEFAULT 'USER'
 * gender            VARCHAR(20)
 * created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 */
public class User {

    private Integer id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private String gender;
    private LocalDateTime createdAt;

    public User() {
    }

    public User(Integer id, String name, String email, String passwordHash,
                Role role, String gender, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.gender = gender;
        this.createdAt = createdAt;
    }

    /** Convenience constructor for registering a brand-new user (no id/createdAt yet). */
    public User(String name, String email, String passwordHash, Role role, String gender) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.gender = gender;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
