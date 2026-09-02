package com.bussheba.ui.customer;

import com.bussheba.model.User;
import com.bussheba.ui.LoginFrame;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;

public class CustomerDashboard extends JFrame {

    private final User currentUser;

    private JButton btnBookTrip;
    private JButton btnMyBookings;
    private JButton btnLogout;

    public CustomerDashboard(User currentUser) {
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setTitle("BusSheba - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 30, 10, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel lblWelcome = new JLabel("Welcome, " + currentUser.getName() + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 24));
        lblWelcome.setForeground(new Color(244, 244, 245));
        gbc.gridy = 0;
        gbc.insets = new Insets(30, 30, 5, 30);
        panel.add(lblWelcome, gbc);

        JLabel lblSubtitle = new JLabel("What would you like to do?", SwingConstants.CENTER);
        lblSubtitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        lblSubtitle.setForeground(new Color(161, 161, 170));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 30, 30, 30);
        panel.add(lblSubtitle, gbc);

        btnBookTrip = new JButton("Book a Trip");
        stylePrimaryButton(btnBookTrip);
        btnBookTrip.addActionListener(e -> onBookTrip());
        gbc.gridy = 2;
        gbc.insets = new Insets(10, 30, 10, 30);
        panel.add(btnBookTrip, gbc);

        btnMyBookings = new JButton("My Bookings");
        stylePrimaryButton(btnMyBookings);
        btnMyBookings.addActionListener(e -> onMyBookings());
        gbc.gridy = 3;
        panel.add(btnMyBookings, gbc);

        btnLogout = new JButton("Logout");
        styleSecondaryButton(btnLogout);
        btnLogout.addActionListener(e -> onLogout());
        gbc.gridy = 4;
        gbc.insets = new Insets(40, 30, 10, 30);
        panel.add(btnLogout, gbc);

        add(panel);
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:12;"
                + "background:rgb(99,102,241);"
                + "hoverBackground:rgb(79,70,229);"
                + "borderWidth:0;"
                + "margin:12,0,12,0");
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

    private void onBookTrip() {
        new BookTripFrame(currentUser).setVisible(true);
    }

    private void onMyBookings() {
        new MyBookingsFrame(currentUser).setVisible(true);
    }

    private void onLogout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
