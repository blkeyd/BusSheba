package com.bussheba.ui;

import com.bussheba.model.User;
import com.bussheba.service.AuthService;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class RegisterFrame extends JFrame {

    private JTextField txtName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JComboBox<String> cmbGender;
    private JButton btnRegister;

    private final AuthService authService;

    public RegisterFrame() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("Create Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 25, 8, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblTitle = new JLabel("Create Account", SwingConstants.CENTER);
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 26));
        lblTitle.setForeground(new Color(244, 244, 245));
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 25, 5, 25);
        panel.add(lblTitle, gbc);

        JLabel lblSubtitle = new JLabel("Sign up to start booking trips", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(161, 161, 170));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 25, 20, 25);
        panel.add(lblSubtitle, gbc);

        txtName = new JTextField();
        styleField(txtName, "Full Name");
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 25, 8, 25);
        panel.add(txtName, gbc);

        txtEmail = new JTextField();
        styleField(txtEmail, "Email");
        gbc.gridy = 3;
        panel.add(txtEmail, gbc);

        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other", "Prefer not to say"});
        cmbGender.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        cmbGender.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241)");
        gbc.gridy = 4;
        panel.add(cmbGender, gbc);

        txtPassword = new JPasswordField();
        styleField(txtPassword, "Password");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "margin:8,12,8,12;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241);"
                + "showRevealButton:true");
        gbc.gridy = 5;
        panel.add(txtPassword, gbc);

        txtConfirmPassword = new JPasswordField();
        styleField(txtConfirmPassword, "Confirm Password");
        txtConfirmPassword.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "margin:8,12,8,12;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241);"
                + "showRevealButton:true");
        gbc.gridy = 6;
        panel.add(txtConfirmPassword, gbc);

        btnRegister = new JButton("Create Account");
        btnRegister.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 14));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "background:rgb(99,102,241);"
                + "hoverBackground:rgb(79,70,229);"
                + "borderWidth:0;"
                + "margin:10,0,10,0");
        btnRegister.addActionListener(this::handleRegister);
        gbc.gridy = 7;
        gbc.insets = new Insets(20, 25, 10, 25);
        panel.add(btnRegister, gbc);

        JLabel lblLoginLink = new JLabel("Already have an account? Sign in", SwingConstants.CENTER);
        lblLoginLink.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblLoginLink.setForeground(new Color(129, 140, 248));
        lblLoginLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblLoginLink.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        gbc.gridy = 8;
        gbc.insets = new Insets(0, 25, 10, 25);
        panel.add(lblLoginLink, gbc);

        add(panel);
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "margin:8,12,8,12;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241)");
    }

    private void handleRegister(ActionEvent e) {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String gender = (String) cmbGender.getSelectedItem();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            User newUser = authService.register(name, email, password, gender);
            JOptionPane.showMessageDialog(this, "Account created! Welcome, " + newUser.getName() + ".");
            new LoginFrame().setVisible(true);
            dispose();

        } catch (IllegalArgumentException ex) {
            // Thrown by AuthService when the email is already registered.
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
