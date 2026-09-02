package com.bussheba.model;

/**
 * Mirrors the values stored in users.role (VARCHAR(30), default 'USER').
 * If you rename these, update the DEFAULT constraint in schema.sql too.
 */
public enum Role {
    USER,
    ADMIN;

    /**
     * Safe conversion from the raw string stored in the DB column.
     */
    public static Role fromDb(String value) {
        if (value == null) {
            return Role.USER;
        }
        return Role.valueOf(value.trim().toUpperCase());
    }
}
