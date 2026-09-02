package com.bussheba.ui.admin;

import com.bussheba.model.User;
import com.bussheba.ui.LoginFrame;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private final User currentUser;

    private JButton btnManageBuses;
    private JButton btnManageRoutes;
    private JButton btnManageTrips;
    private JButton btnManageUsers;
    private JButton btnViewBookings;
    private JButton btnLogout;

    public AdminDashboard(User currentUser) {
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setTitle("BusSheba - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblWelcome = new JLabel("Admin: " + currentUser.getName(), SwingConstants.CENTER);
        lblWelcome.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 22));
        lblWelcome.setForeground(new Color(244, 244, 245));
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 30, 5, 30);
        panel.add(lblWelcome, gbc);

        JLabel lblSubtitle = new JLabel("Manage buses, routes, trips, and users", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(161, 161, 170));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 30, 25, 30);
        panel.add(lblSubtitle, gbc);

        btnManageBuses = new JButton("Manage Buses");
        stylePrimaryButton(btnManageBuses);
        btnManageBuses.addActionListener(e -> onManageBuses());
        gbc.gridy = 2;
        gbc.insets = new Insets(8, 30, 8, 30);
        panel.add(btnManageBuses, gbc);

        btnManageRoutes = new JButton("Manage Routes");
        stylePrimaryButton(btnManageRoutes);
        btnManageRoutes.addActionListener(e -> onManageRoutes());
        gbc.gridy = 3;
        panel.add(btnManageRoutes, gbc);

        btnManageTrips = new JButton("Manage Trips");
        stylePrimaryButton(btnManageTrips);
        btnManageTrips.addActionListener(e -> onManageTrips());
        gbc.gridy = 4;
        panel.add(btnManageTrips, gbc);

        btnManageUsers = new JButton("Manage Users");
        stylePrimaryButton(btnManageUsers);
        btnManageUsers.addActionListener(e -> onManageUsers());
        gbc.gridy = 5;
        panel.add(btnManageUsers, gbc);

        btnViewBookings = new JButton("View All Bookings & Revenue");
        stylePrimaryButton(btnViewBookings);
        btnViewBookings.addActionListener(e -> onViewBookings());
        gbc.gridy = 6;
        panel.add(btnViewBookings, gbc);

        btnLogout = new JButton("Logout");
        styleSecondaryButton(btnLogout);
        btnLogout.addActionListener(e -> onLogout());
        gbc.gridy = 7;
        gbc.insets = new Insets(30, 30, 10, 30);
        panel.add(btnLogout, gbc);

        add(panel);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "background:rgb(99,102,241);"
                + "hoverBackground:rgb(79,70,229);"
                + "borderWidth:0;"
                + "margin:10,0,10,0");
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        button.setForeground(new Color(244, 244, 245));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "background:rgb(39,39,42);"
                + "hoverBackground:rgb(63,63,70);"
                + "borderWidth:0;"
                + "margin:10,0,10,0");
    }

    private void onManageBuses() {
        new ManageBusesPanel().setVisible(true);
    }

    private void onManageRoutes() {
        new ManageRoutesPanel().setVisible(true);
    }

    private void onManageTrips() {
        new ManageTripsPanel().setVisible(true);
    }

    private void onManageUsers() {
        new ManageUsersPanel(currentUser).setVisible(true);
    }

    private void onViewBookings() {
        new BookingsRevenuePanel().setVisible(true);
    }

    private void onLogout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
