package com.bussheba.ui;

import com.bussheba.model.Role;
import com.bussheba.model.User;
import com.bussheba.service.AuthService;
import com.bussheba.ui.admin.AdminDashboard;
import com.bussheba.ui.components.BackgroundPanel;
import com.bussheba.ui.components.GradientPanel;
import com.bussheba.ui.components.RoundedContainer;
import com.bussheba.ui.customer.CustomerDashboard;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Split-card login screen: a full-window background photo (the bus at
 * sunset) with a centered rounded card floating on top. Left half of the
 * card is a purple/indigo gradient with a welcome message; right half is
 * the app's usual dark panel with just the Email/Password fields — no
 * social login buttons.
 */
public class LoginFrame extends JFrame {

    private static final Color GRADIENT_START = new Color(99, 102, 241);   // matches app's existing accent
    private static final Color GRADIENT_END = new Color(67, 56, 202);      // deeper indigo
    private static final Color PANEL_DARK = new Color(24, 24, 27);
    private static final Color FIELD_DARK = new Color(39, 39, 42);
    private static final Color BORDER_DARK = new Color(63, 63, 70);
    private static final Color TEXT_LIGHT = new Color(244, 244, 245);
    private static final Color TEXT_MUTED = new Color(210, 210, 235);

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
        setSize(1960, 1080);
        setLocationRelativeTo(null);
        setResizable(false);

        BackgroundPanel background = new BackgroundPanel("/images/login_background.jpg");
        setContentPane(background);

        RoundedContainer card = new RoundedContainer(24);
        card.setPreferredSize(new Dimension(800, 440));
        card.setLayout(new GridLayout(1, 2, 0, 0));
        card.add(buildLeftPanel());
        card.add(buildRightPanel());

        // BackgroundPanel's own GridBagLayout centers this single child automatically.
        background.add(card);
    }

    private JPanel buildLeftPanel() {
        GradientPanel left = new GradientPanel(GRADIENT_START, GRADIENT_END);
        left.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 40, 0, 30);

        JLabel lblTitle = new JLabel("BusSheba");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 32));
        lblTitle.setForeground(Color.WHITE);
        left.add(lblTitle, gbc);

        return left;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(PANEL_DARK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 35, 8, 35);

        JLabel lblTitle = new JLabel("Sign In");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 22));
        lblTitle.setForeground(TEXT_LIGHT);
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 35, 20, 35);
        right.add(lblTitle, gbc);

        txtUsername = new JTextField();
        styleField(txtUsername, "Email");
        gbc.gridy = 1;
        gbc.insets = new Insets(8, 35, 8, 35);
        right.add(txtUsername, gbc);

        txtPassword = new JPasswordField();
        styleField(txtPassword, "Password");
        txtPassword.putClientProperty(FlatClientProperties.STYLE, fieldStyle() + ";showRevealButton:true");
        gbc.gridy = 2;
        right.add(txtPassword, gbc);

        btnLogin = new JButton("Login");
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
        gbc.gridy = 3;
        gbc.insets = new Insets(20, 35, 10, 35);
        right.add(btnLogin, gbc);

        JLabel lblRegisterLink = new JLabel("New here? Register", SwingConstants.CENTER);
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
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 35, 10, 35);
        right.add(lblRegisterLink, gbc);

        return right;
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 14));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, fieldStyle());
    }

    private String fieldStyle() {
        return "arc:10;"
                + "margin:8,12,8,12;"
                + "background:rgb(" + FIELD_DARK.getRed() + "," + FIELD_DARK.getGreen() + "," + FIELD_DARK.getBlue() + ");"
                + "borderColor:rgb(" + BORDER_DARK.getRed() + "," + BORDER_DARK.getGreen() + "," + BORDER_DARK.getBlue() + ");"
                + "focusedBorderColor:rgb(99,102,241)";
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
