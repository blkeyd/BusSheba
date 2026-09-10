package com.bussheba.ui.admin;

import com.bussheba.ui.components.Theme;

import com.bussheba.dao.BusDAO;
import com.bussheba.dao.RouteDAO;
import com.bussheba.dao.SeatDAO;
import com.bussheba.dao.TripDAO;
import com.bussheba.model.Bus;
import com.bussheba.model.Route;
import com.bussheba.model.Trip;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * Admin CRUD screen for the `trips` table. A trip needs an existing bus_id
 * and route_id, so those are dropdowns loaded from BusDAO/RouteDAO rather
 * than free-text fields.
 *
 * When a NEW trip is created, this screen also calls
 * SeatDAO.insertSeatsForTrip() right after, generating seats A1..A{N}
 * where N is the selected bus's total_seats — otherwise BookTripFrame
 * would have nothing to show customers for this trip.
 */
public class ManageTripsPanel extends JFrame {

    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final BusDAO busDAO = new BusDAO();
    private final RouteDAO routeDAO = new RouteDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    private JTable tripTable;
    private DefaultTableModel tableModel;
    private List<Trip> currentTrips;
    private Integer selectedTripId;

    private List<Bus> allBuses;
    private List<Route> allRoutes;

    private JComboBox<ComboItem> cmbBus;
    private JComboBox<ComboItem> cmbRoute;
    private JTextField txtDeparture;
    private JTextField txtArrival;
    private JTextField txtBasePrice;

    private JButton btnSave;
    private JButton btnDelete;
    private JButton btnClear;

    public ManageTripsPanel() {
        initUI();
        loadDropdownData();
        loadTrips();
    }

    private void initUI() {
        setTitle("BusSheba Admin - Manage Trips");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 620);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.BG_LIGHT);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("Manage Trips");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(Theme.TEXT_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(lblTitle, BorderLayout.NORTH);

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(), BorderLayout.SOUTH);
    }

    private JScrollPane buildTablePanel() {
        String[] columns = {"ID", "Bus", "Route", "Departure", "Arrival", "Price"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tripTable = new JTable(tableModel);
        tripTable.setRowHeight(26);
        tripTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tripTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tripTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        return scrollPane;
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BG_LIGHT);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbBus = new JComboBox<>();
        styleCombo(cmbBus);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panel.add(labeledRow("Bus", cmbBus), gbc);

        cmbRoute = new JComboBox<>();
        styleCombo(cmbRoute);
        gbc.gridx = 1;
        panel.add(labeledRow("Route", cmbRoute), gbc);

        txtDeparture = new JTextField();
        styleField(txtDeparture, "yyyy-MM-dd HH:mm");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(labeledRow("Departure", txtDeparture), gbc);

        txtArrival = new JTextField();
        styleField(txtArrival, "yyyy-MM-dd HH:mm");
        gbc.gridx = 1;
        panel.add(labeledRow("Arrival", txtArrival), gbc);

        txtBasePrice = new JTextField();
        styleField(txtBasePrice, "Base Price (e.g. 850.00)");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(labeledRow("Base Price", txtBasePrice), gbc);
        gbc.gridwidth = 1;

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setBackground(Theme.BG_LIGHT);

        btnClear = new JButton("Clear / New");
        styleSecondaryButton(btnClear);
        btnClear.addActionListener(e -> clearForm());
        buttonRow.add(btnClear);

        btnDelete = new JButton("Delete");
        styleDangerButton(btnDelete);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> onDelete());
        buttonRow.add(btnDelete);

        btnSave = new JButton("Add Trip");
        stylePrimaryButton(btnSave);
        btnSave.addActionListener(e -> onSave());
        buttonRow.add(btnSave);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 5, 0, 5);
        panel.add(buttonRow, gbc);

        return panel;
    }

    private JPanel labeledRow(String labelText, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 3));
        wrapper.setBackground(Theme.BG_LIGHT);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 11));
        label.setForeground(Theme.TEXT_MUTED);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadDropdownData() {
        try {
            allBuses = busDAO.findAll();
            cmbBus.removeAllItems();
            for (Bus bus : allBuses) {
                cmbBus.addItem(new ComboItem(bus.getId(), bus.getBusNumber() + " - " + bus.getBusName()
                        + " (" + bus.getTotalSeats() + " seats)"));
            }

            allRoutes = routeDAO.findAll();
            cmbRoute.removeAllItems();
            for (Route route : allRoutes) {
                cmbRoute.addItem(new ComboItem(route.getId(), route.getOrigin() + " -> " + route.getDestination()));
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load buses/routes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTrips() {
        try {
            currentTrips = tripDAO.findAll();
            tableModel.setRowCount(0);

            for (Trip trip : currentTrips) {
                String busLabel = allBuses.stream()
                        .filter(b -> b.getId().equals(trip.getBusId()))
                        .findFirst()
                        .map(Bus::getBusNumber)
                        .orElse("#" + trip.getBusId());

                String routeLabel = allRoutes.stream()
                        .filter(r -> r.getId().equals(trip.getRouteId()))
                        .findFirst()
                        .map(r -> r.getOrigin() + " -> " + r.getDestination())
                        .orElse("#" + trip.getRouteId());

                tableModel.addRow(new Object[]{
                        trip.getId(), busLabel, routeLabel,
                        trip.getDepartureTime().format(DISPLAY_FMT),
                        trip.getArrivalTime().format(DISPLAY_FMT),
                        trip.getBasePrice()
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load trips: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRowSelected() {
        int row = tripTable.getSelectedRow();
        if (row < 0 || currentTrips == null || row >= currentTrips.size()) {
            return;
        }

        Trip trip = currentTrips.get(row);
        selectedTripId = trip.getId();

        selectComboItemById(cmbBus, trip.getBusId());
        selectComboItemById(cmbRoute, trip.getRouteId());
        txtDeparture.setText(trip.getDepartureTime().format(INPUT_FMT));
        txtArrival.setText(trip.getArrivalTime().format(INPUT_FMT));
        txtBasePrice.setText(trip.getBasePrice().toPlainString());

        btnSave.setText("Update Trip");
        btnDelete.setEnabled(true);
    }

    private void selectComboItemById(JComboBox<ComboItem> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).id == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void clearForm() {
        selectedTripId = null;
        if (cmbBus.getItemCount() > 0) cmbBus.setSelectedIndex(0);
        if (cmbRoute.getItemCount() > 0) cmbRoute.setSelectedIndex(0);
        txtDeparture.setText("");
        txtArrival.setText("");
        txtBasePrice.setText("");
        btnSave.setText("Add Trip");
        btnDelete.setEnabled(false);
        tripTable.clearSelection();
    }

    private void onSave() {
        ComboItem busItem = (ComboItem) cmbBus.getSelectedItem();
        ComboItem routeItem = (ComboItem) cmbRoute.getSelectedItem();
        String departureText = txtDeparture.getText().trim();
        String arrivalText = txtArrival.getText().trim();
        String priceText = txtBasePrice.getText().trim();

        if (busItem == null || routeItem == null || departureText.isEmpty()
                || arrivalText.isEmpty() || priceText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Missing info", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime departure;
        LocalDateTime arrival;
        try {
            departure = LocalDateTime.parse(departureText, INPUT_FMT);
            arrival = LocalDateTime.parse(arrivalText, INPUT_FMT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Dates must be in the format yyyy-MM-dd HH:mm (e.g. 2026-09-15 14:30).",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!arrival.isAfter(departure)) {
            JOptionPane.showMessageDialog(this, "Arrival time must be after departure time.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal basePrice;
        try {
            basePrice = new BigDecimal(priceText);
            if (basePrice.signum() < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Base Price must be a non-negative number.",
                    "Invalid input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (selectedTripId == null) {
                Trip newTrip = new Trip(busItem.id, routeItem.id, departure, arrival, basePrice);
                tripDAO.insert(newTrip);

                // Auto-generate seats to match the selected bus's total_seats.
                Optional<Bus> busOpt = busDAO.findById(busItem.id);
                if (busOpt.isPresent()) {
                    seatDAO.insertSeatsForTrip(newTrip.getId(), busOpt.get().getTotalSeats(), busOpt.get().getBusType());
                }

                JOptionPane.showMessageDialog(this, "Trip added, with seats generated automatically.");
            } else {
                Trip updated = new Trip(selectedTripId, busItem.id, routeItem.id, departure, arrival, basePrice);
                tripDAO.update(updated);
                JOptionPane.showMessageDialog(this, "Trip updated. (Note: seats are not regenerated on edit.)");
            }

            clearForm();
            loadTrips();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        if (selectedTripId == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete this trip? This will fail if any bookings exist for it.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            tripDAO.delete(selectedTripId);
            JOptionPane.showMessageDialog(this, "Trip deleted.");
            clearForm();
            loadTrips();

        } catch (SQLException ex) {
            // fk_bookings_trip is ON DELETE RESTRICT — fires if bookings still reference this trip.
            JOptionPane.showMessageDialog(this,
                    "Cannot delete this trip — it has bookings on record.",
                    "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        field.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, placeholder);
        field.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;margin:6,10,6,10;background:rgb(255,255,255);"
                + "borderColor:rgb(224,224,224);focusedBorderColor:rgb(0,121,107)");
    }

    private void styleCombo(JComboBox<ComboItem> combo) {
        combo.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        combo.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(255,255,255);borderColor:rgb(224,224,224);focusedBorderColor:rgb(0,121,107)");
    }

    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(230,126,34);hoverBackground:rgb(202,106,18);borderWidth:0;margin:8,16,8,16");
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        button.setForeground(Theme.TEXT_DARK);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(224,224,224);hoverBackground:rgb(200,200,200);borderWidth:0;margin:8,16,8,16");
    }

    private void styleDangerButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;background:rgb(211,47,47);hoverBackground:rgb(183,28,28);borderWidth:0;margin:8,16,8,16");
    }

    /** Small wrapper so JComboBox can show a readable label while we keep the underlying id. */
    private static class ComboItem {
        final int id;
        final String label;

        ComboItem(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
