package com.bussheba.ui.admin;

import com.bussheba.dao.BusDAO;
import com.bussheba.model.Bus;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin CRUD screen for the `buses` table. Selecting a row loads it into
 * the form fields for editing; clearing the form (or nothing selected)
 * means "Add" will insert a new bus instead of updating one.
 */
public class ManageBusesPanel extends JFrame {

    private final BusDAO busDAO = new BusDAO();

    private JTable busTable;
    private DefaultTableModel tableModel;
    private List<Bus> currentBuses;
    private Integer selectedBusId;

    private JTextField txtBusNumber;
    private JTextField txtBusName;
    private JTextField txtBusType;
    private JTextField txtTotalSeats;

    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnClear;

    public ManageBusesPanel() {
        initUI();
        loadBuses();
    }

    private void initUI() {
        setTitle("BusSheba Admin - Manage Buses");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 550);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Manage Buses");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(new Color(244, 244, 245));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(lblTitle, BorderLayout.NORTH);

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"ID", "Bus Number", "Bus Name", "Type", "Total Seats"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        busTable = new JTable(tableModel);
        busTable.setRowHeight(26);
        busTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        busTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(busTable);
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

        txtBusNumber = new JTextField();
        styleField(txtBusNumber, "Bus Number (e.g. BUS-002)");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panel.add(txtBusNumber, gbc);

        txtBusName = new JTextField();
        styleField(txtBusName, "Bus Name");
        gbc.gridx = 1;
        panel.add(txtBusName, gbc);

        txtBusType = new JTextField();
        styleField(txtBusType, "Type (e.g. AC Sleeper)");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(txtBusType, gbc);

        txtTotalSeats = new JTextField();
        styleField(txtTotalSeats, "Total Seats");
        gbc.gridx = 1;
        panel.add(txtTotalSeats, gbc);

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

        btnSave = new JButton("Add Bus");
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

    private void loadBuses() {
        try {
            currentBuses = busDAO.findAll();
            tableModel.setRowCount(0);

            for (Bus bus : currentBuses) {
                tableModel.addRow(new Object[]{
                        bus.getId(), bus.getBusNumber(), bus.getBusName(), bus.getBusType(), bus.getTotalSeats()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load buses: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected() {
        int row = busTable.getSelectedRow();
        if (row < 0 || currentBuses == null || row >= currentBuses.size()) {
            return;
        }

        Bus bus = currentBuses.get(row);
        selectedBusId = bus.getId();
        txtBusNumber.setText(bus.getBusNumber());
        txtBusName.setText(bus.getBusName());
        txtBusType.setText(bus.getBusType());
        txtTotalSeats.setText(String.valueOf(bus.getTotalSeats()));

        btnSave.setText("Update Bus");
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        selectedBusId = null;
        txtBusNumber.setText("");
        txtBusName.setText("");
        txtBusType.setText("");
        txtTotalSeats.setText("");
        btnSave.setText("Add Bus");
        btnDelete.setEnabled(false);
        busTable.clearSelection();
    }

    private void onSave() {
        String busNumber = txtBusNumber.getText().trim();
        String busName = txtBusName.getText().trim();
        String busType = txtBusType.getText().trim();
        String totalSeatsText = txtTotalSeats.getText().trim();

        if (busNumber.isEmpty() || busName.isEmpty() || busType.isEmpty() || totalSeatsText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int totalSeats;
        try {
            totalSeats = Integer.parseInt(totalSeatsText);
            if (totalSeats <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Total Seats must be a positive whole number.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (selectedBusId == null) {
                Bus newBus = new Bus(busNumber, busName, busType, totalSeats);
                busDAO.insert(newBus);
                JOptionPane.showMessageDialog(this, "Bus added.");
            } else {
                Bus updated = new Bus(selectedBusId, busNumber, busName, busType, totalSeats);
                busDAO.update(updated);
                JOptionPane.showMessageDialog(this, "Bus updated.");
            }

            clearForm();
            loadBuses();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedBusId == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this bus? This will fail if any trips still reference it.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            busDAO.delete(selectedBusId);
            JOptionPane.showMessageDialog(this, "Bus deleted.");
            clearForm();
            loadBuses();

        } catch (SQLException ex) {
            // fk_trips_bus is ON DELETE RESTRICT — this fires if trips still reference the bus.
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this bus — it's used by one or more trips. Remove those trips first.",
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
