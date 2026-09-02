package com.bussheba.ui.admin;

import com.bussheba.dao.RouteDAO;
import com.bussheba.model.Route;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin CRUD screen for the `routes` table. Same select-to-edit pattern as
 * ManageBusesPanel.
 */
public class ManageRoutesPanel extends JFrame {

    private final RouteDAO routeDAO = new RouteDAO();

    private JTable routeTable;
    private DefaultTableModel tableModel;
    private List<Route> currentRoutes;
    private Integer selectedRouteId;

    private JTextField txtOrigin;
    private JTextField txtDestination;
    private JTextField txtDistance;

    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnClear;

    public ManageRoutesPanel() {
        initUI();
        loadRoutes();
    }

    private void initUI() {
        setTitle("BusSheba Admin - Manage Routes");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Manage Routes");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(new Color(244, 244, 245));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(lblTitle, BorderLayout.NORTH);

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"ID", "Origin", "Destination", "Distance (km)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        routeTable = new JTable(tableModel);
        routeTable.setRowHeight(26);
        routeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        routeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(routeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        return scrollPane;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtOrigin = new JTextField();
        styleField(txtOrigin, "Origin (e.g. Dhaka)");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panel.add(txtOrigin, gbc);

        txtDestination = new JTextField();
        styleField(txtDestination, "Destination (e.g. Sylhet)");
        gbc.gridx = 1;
        panel.add(txtDestination, gbc);

        txtDistance = new JTextField();
        styleField(txtDistance, "Distance in km (e.g. 247.5)");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(txtDistance, gbc);
        gbc.gridwidth = 1;

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setBackground(new Color(24, 24, 27));

        btnClear = new JButton("Clear / New");
        styleSecondaryButton(btnClear);
        btnClear.addActionListener(e -> clearForm());
        buttonRow.add(btnClear);

        btnDelete = new JButton("Delete");
        styleDangerButton(btnDelete);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> onDelete());
        buttonRow.add(btnDelete);

        btnSave = new JButton("Add Route");
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> onSave());
        buttonRow.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 0, 5);
        panel.add(buttonRow, gbc);

        return panel;
    }

    private void loadRoutes() {
        try {
            currentRoutes = routeDAO.findAll();
            tableModel.setRowCount(0);

            for (Route route : currentRoutes) {
                tableModel.addRow(new Object[]{
                        route.getId(), route.getOrigin(), route.getDestination(), route.getDistanceKm()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load routes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected() {
        int row = routeTable.getSelectedRow();
        if (row < 0 || currentRoutes == null || row >= currentRoutes.size()) {
            return;
        }

        Route route = currentRoutes.get(row);
        selectedRouteId = route.getId();
        txtOrigin.setText(route.getOrigin());
        txtDestination.setText(route.getDestination());
        txtDistance.setText(route.getDistanceKm().toPlainString());

        btnSave.setText("Update Route");
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        selectedRouteId = null;
        txtOrigin.setText("");
        txtDestination.setText("");
        txtDistance.setText("");
        btnSave.setText("Add Route");
        btnDelete.setEnabled(false);
        routeTable.clearSelection();
    }

    private void onSave() {
        String origin = txtOrigin.getText().trim();
        String destination = txtDestination.getText().trim();
        String distanceText = txtDistance.getText().trim();

        if (origin.isEmpty() || destination.isEmpty() || distanceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal distanceKm;
        try {
            distanceKm = new BigDecimal(distanceText);
            if (distanceKm.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Distance must be a non-negative number.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (selectedRouteId == null) {
                Route newRoute = new Route(origin, destination, distanceKm);
                routeDAO.insert(newRoute);
                JOptionPane.showMessageDialog(this, "Route added.");
            } else {
                Route updated = new Route(selectedRouteId, origin, destination, distanceKm);
                routeDAO.update(updated);
                JOptionPane.showMessageDialog(this, "Route updated.");
            }

            clearForm();
            loadRoutes();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedRouteId == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this route? This will fail if any trips still reference it.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            routeDAO.delete(selectedRouteId);
            JOptionPane.showMessageDialog(this, "Route deleted.");
            clearForm();
            loadRoutes();

        } catch (SQLException ex) {
            // fk_trips_route is ON DELETE RESTRICT — this fires if trips still reference the route.
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this route — it's used by one or more trips. Remove those trips first.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;"
                + "margin:6,10,6,10;"
                + "background:rgb(39,39,42);"
                + "borderColor:rgb(63,63,70);"
                + "focusedBorderColor:rgb(99,102,241)");
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
