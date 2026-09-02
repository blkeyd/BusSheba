package com.bussheba.ui;

import com.bussheba.model.Role;
import com.bussheba.model.User;
import com.bussheba.service.AuthService;
import com.bussheba.ui.admin.AdminDashboard;
import com.bussheba.ui.customer.CustomerDashboard;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    private final AuthService authService;

    public LoginFrame() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Welcome Back");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Panel with background padding
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27)); // Modern Dark Charcoal
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 25, 10, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // Header Title
        JLabel lblTitle = new JLabel("Sign In", SwingConstants.CENTER);
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 28));
        lblTitle.setForeground(new Color(244, 244, 245));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 25, 5, 25);
        panel.add(lblTitle, gbc);

        // Subtitle
        JLabel lblSubtitle = new JLabel("Please enter your details to continue", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(161, 161, 170));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 25, 25, 25);
        panel.add(lblSubtitle, gbc);

        // Username / Email Field
        txtUsername = new JTextField();
        txtUsername.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        txtUsername.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Email");
        txtUsername.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "margin:8,12,8,12;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241)");
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 25, 8, 25);
        panel.add(txtUsername, gbc);

        // Password Field
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        txtPassword.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "margin:8,12,8,12;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241);"
                + "showRevealButton:true"); // Built-in toggle password visibility icon
        gbc.gridy = 3;
        panel.add(txtPassword, gbc);

        // Sign In Button
        btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 14));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "background:rgb(99,102,241);"
                + "hoverBackground:rgb(79,70,229);"
                + "borderWidth:0;"
                + "margin:10,0,10,0");

        btnLogin.addActionListener(this::handleLogin);
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 25, 10, 25);
        panel.add(btnLogin, gbc);

        // "Don't have an account? Register" link-style label
        JLabel lblRegisterLink = new JLabel("Don't have an account? Register", SwingConstants.CENTER);
        lblRegisterLink.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblRegisterLink.setForeground(new Color(129, 140, 248));
        lblRegisterLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegisterLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new RegisterFrame().setVisible(true);
                dispose();
            }
        });
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 25, 10, 25);
        panel.add(lblRegisterLink, gbc);

        add(panel);
    }

    private void handleLogin(ActionEvent e) {
        String email = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Optional<User> userOpt = authService.login(email, password);

            if (userOpt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User user = userOpt.get();

            if (user.getRole() == Role.ADMIN) {
                new AdminDashboard(user).setVisible(true);
            } else {
                new CustomerDashboard(user).setVisible(true);
            }
            dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
