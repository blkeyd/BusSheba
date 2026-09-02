package com.bussheba.service;

import com.bussheba.dao.UserDAO;
import com.bussheba.model.Role;
import com.bussheba.model.User;
import com.bussheba.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Business rules around registration and login. UI classes (LoginFrame,
 * RegisterFrame) should call this instead of touching UserDAO or
 * PasswordUtil directly.
 */
public class AuthService {

    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    /** Lets tests (or a future DI setup) inject a mock/alternate DAO. */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new customer account.
     *
     * @throws IllegalArgumentException if the email is already taken
     * @throws SQLException on a database error
     */
    public User register(String name, String email, String plainPassword, String gender) throws SQLException {
        if (userDAO.emailExists(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = PasswordUtil.hash(plainPassword);
        User newUser = new User(name, email, hashedPassword, Role.USER, gender);

        return userDAO.insert(newUser);
    }

    /**
     * Attempts a login. Returns the matching User if the email exists and
     * the password is correct; empty otherwise (deliberately not
     * distinguishing "no such email" from "wrong password" in the return
     * value, so the UI can show one generic "invalid credentials" message
     * rather than leaking which emails are registered).
     */
    public Optional<User> login(String email, String plainPassword) throws SQLException {
        Optional<User> userOpt = userDAO.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        boolean passwordMatches = PasswordUtil.verify(plainPassword, user.getPasswordHash());

        return passwordMatches ? Optional.of(user) : Optional.empty();
    }
}
