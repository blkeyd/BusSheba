package com.bussheba.ui.admin;

import com.bussheba.model.User;
import com.bussheba.ui.LoginFrame;
import com.bussheba.ui.components.Theme;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class AdminDashboard extends JFrame {

    private final User currentUser;

    public AdminDashboard(User currentUser) {
        this.currentUser = currentUser;
        initUI();
    }

    private void initUI() {
        setTitle("BusSheba - Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 640);
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

        JLabel lblAppName = new JLabel("BusSheba Admin");
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
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 30, 8, 30);

        gbc.gridy = 0;
        gbc.insets = new Insets(25, 30, 8, 30);
        body.add(actionRow("Manage Buses", "Add, edit, or remove buses", e -> new ManageBusesPanel().setVisible(true)), gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(8, 30, 8, 30);
        body.add(actionRow("Manage Routes", "Add, edit, or remove routes", e -> new ManageRoutesPanel().setVisible(true)), gbc);

        gbc.gridy = 2;
        body.add(actionRow("Manage Trips", "Schedule trips on buses and routes", e -> new ManageTripsPanel().setVisible(true)), gbc);

        gbc.gridy = 3;
        body.add(actionRow("Manage Users", "View users, change roles, remove accounts",
                e -> new ManageUsersPanel(currentUser).setVisible(true)), gbc);

        gbc.gridy = 4;
        body.add(actionRow("Bookings & Revenue", "All bookings across every customer",
                e -> new BookingsRevenuePanel().setVisible(true)), gbc);

        return body;
    }

    private JPanel actionRow(String title, String subtitle, ActionListener onClick) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 14));
        lblTitle.setForeground(Theme.TEXT_DARK);
        textBlock.add(lblTitle);

        JLabel lblSubtitle = new JLabel(subtitle);
        lblSubtitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 11));
        lblSubtitle.setForeground(Theme.TEXT_MUTED);
        lblSubtitle.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
        textBlock.add(lblSubtitle);

        card.add(textBlock, BorderLayout.WEST);

        JLabel lblArrow = new JLabel("\u2192");
        lblArrow.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 18));
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

    private void onLogout() {
        new LoginFrame().setVisible(true);
        dispose();
    }
}
