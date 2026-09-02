package com.bussheba.ui.admin;

import com.bussheba.dao.UserDAO;
import com.bussheba.model.Role;
import com.bussheba.model.User;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin view of the `users` table. Deliberately no password editing here —
 * resetting a password should go through a proper "forgot password" flow
 * later, not a plain-text field an admin can see. This screen only lets
 * an admin promote/demote a role or remove an account.
 */
public class ManageUsersPanel extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final User currentAdmin;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private List<User> currentUsers;

    private JButton btnToggleRole;
    private JButton btnDelete;
    private JButton btnRefresh;

    public ManageUsersPanel(User currentAdmin) {
        this.currentAdmin = currentAdmin;
        initUI();
        loadUsers();
    }

    private void initUI() {
        setTitle("BusSheba Admin - Manage Users");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Manage Users");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(new Color(244, 244, 245));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Email", "Role", "Gender", "Joined"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(26);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomRow.setBackground(new Color(24, 24, 27));
        bottomRow.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        btnRefresh = new JButton("Refresh");
        styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadUsers());
        bottomRow.add(btnRefresh);

        btnToggleRole = new JButton("Toggle Admin / User");
        stylePrimaryButton(btnToggleRole);
        btnToggleRole.setEnabled(false);
        btnToggleRole.addActionListener(e -> onToggleRole());
        bottomRow.add(btnToggleRole);

        btnDelete = new JButton("Delete User");
        styleDangerButton(btnDelete);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> onDelete());
        bottomRow.add(btnDelete);

        add(bottomRow, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        try {
            currentUsers = userDAO.findAll();
            tableModel.setRowCount(0);

            for (User user : currentUsers) {
                tableModel.addRow(new Object[]{
                        user.getId(), user.getName(), user.getEmail(), user.getRole(),
                        user.getGender() != null ? user.getGender() : "-",
                        user.getCreatedAt() != null ? user.getCreatedAt().toLocalDate() : "-"
                });
            }

            updateButtonStates();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtonStates() {
        int row = userTable.getSelectedRow();
        if (row < 0 || currentUsers == null || row >= currentUsers.size()) {
            btnToggleRole.setEnabled(false);
            btnDelete.setEnabled(false);
            return;
        }

        User selected = currentUsers.get(row);
        boolean isSelf = selected.getId().equals(currentAdmin.getId());

        // An admin shouldn't demote or delete their own account from this screen —
        // that could lock them out with no other admin able to log in and undo it.
        btnToggleRole.setEnabled(!isSelf);
        btnDelete.setEnabled(!isSelf);
    }

    private void onToggleRole() {
        int row = userTable.getSelectedRow();
        if (row < 0 || currentUsers == null || row >= currentUsers.size()) {
            return;
        }

        User selected = currentUsers.get(row);
        Role newRole = selected.getRole() == Role.ADMIN ? Role.USER : Role.ADMIN;

        int choice = JOptionPane.showConfirmDialog(this,
                "Change " + selected.getName() + "'s role to " + newRole + "?",
                "Confirm Role Change", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            selected.setRole(newRole);
            userDAO.update(selected);
            JOptionPane.showMessageDialog(this, "Role updated.");
            loadUsers();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = userTable.getSelectedRow();
        if (row < 0 || currentUsers == null || row >= currentUsers.size()) {
            return;
        }

        User selected = currentUsers.get(row);

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete account for " + selected.getName() + " (" + selected.getEmail() + ")? "
                        + "This will fail if they have existing bookings.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            userDAO.delete(selected.getId());
            JOptionPane.showMessageDialog(this, "User deleted.");
            loadUsers();

        } catch (SQLException ex) {
            // fk_bookings_user is ON DELETE RESTRICT — fires if this user has bookings on record.
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this user — they have bookings on record.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(99,102,241);hoverBackground:rgb(79,70,229);borderWidth:0;margin:8,16,8,16");
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        button.setForeground(new Color(244, 244, 245));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(39,39,42);hoverBackground:rgb(63,63,70);borderWidth:0;margin:8,16,8,16");
    }

    private void styleDangerButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(220,38,38);hoverBackground:rgb(185,28,28);borderWidth:0;margin:8,16,8,16");
    }
}
