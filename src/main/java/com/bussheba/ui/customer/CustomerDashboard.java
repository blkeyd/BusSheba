package com.bussheba.ui.customer;

import com.bussheba.model.User;
import com.bussheba.ui.LoginFrame;
import com.bussheba.ui.components.Theme;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;

/**
 * Customer landing screen — light/teal "ticketing platform" theme.
 * Teal header bar with the app name and user info, two big action cards
 * below for the two things a customer actually does here.
 */
public class CustomerDashboard extends JFrame {

    private final User currentUser;

    public CustomerDashboard(User currentUser) {
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setTitle("BusSheba - Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(null);
        setResizable(false);

        getContentPane().setBackground(Theme.BG_LIGHT);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.TEAL_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));

        JLabel lblAppName = new JLabel("BusSheba");
        lblAppName.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblAppName.setForeground(Theme.TEXT_ON_TEAL);
        header.add(lblAppName, BorderLayout.WEST);

        JPanel rightBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightBlock.setOpaque(false);

        JLabel lblUser = new JLabel(currentUser.getName());
        lblUser.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        lblUser.setForeground(Theme.TEXT_ON_TEAL);
        rightBlock.add(lblUser);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        btnLogout.setForeground(Theme.TEAL_PRIMARY);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:8;background:white;borderWidth:0;margin:5,12,5,12");
        btnLogout.addActionListener(e -> onLogout());
        rightBlock.add(btnLogout);

        header.add(rightBlock, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.BG_LIGHT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(20, 40, 15, 40);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblGreeting = new JLabel("Welcome back, " + currentUser.getName() + "!");
        lblGreeting.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 18));
        lblGreeting.setForeground(Theme.TEXT_DARK);
        gbc.gridy = 0;
        gbc.insets = new Insets(35, 40, 20, 40);
        body.add(lblGreeting, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(10, 40, 10, 40);
        body.add(buildActionCard("Book a Trip", "Search routes and reserve your seat", e -> onBookTrip()), gbc);

        gbc.gridy = 2;
        body.add(buildActionCard("My Bookings", "View or cancel your upcoming trips", e -> onMyBookings()), gbc);

        return body;
    }

    private JPanel buildActionCard(String title, String subtitle, java.awt.event.ActionListener onClick) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 16));
        lblTitle.setForeground(Theme.TEXT_DARK);
        textBlock.add(lblTitle);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblSubtitle.setForeground(Theme.TEXT_MUTED);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        textBlock.add(lblSubtitle);

        card.add(textBlock, BorderLayout.WEST);

        JLabel lblArrow = new JLabel("\u2192");
        lblArrow.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblArrow.setForeground(Theme.TEAL_PRIMARY);
        card.add(lblArrow, BorderLayout.EAST);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                onClick.actionPerformed(null);
            }
        });

        return card;
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
