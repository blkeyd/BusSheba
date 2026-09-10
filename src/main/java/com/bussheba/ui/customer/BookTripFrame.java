package com.bussheba.ui.customer;

import com.bussheba.dao.RouteDAO;
import com.bussheba.dao.SeatDAO;
import com.bussheba.dao.TripDAO;
import com.bussheba.model.Booking;
import com.bussheba.model.Route;
import com.bussheba.model.Seat;
import com.bussheba.model.Trip;
import com.bussheba.model.User;
import com.bussheba.service.BookingService;
import com.bussheba.ui.components.Theme;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TreeSet;

/**
 * Customer flow, "railway ticketing" style: pick From/To from dropdowns
 * (populated from actual route data, not free text) -> results table of
 * buses with time and price -> pick a trip -> pick a seat -> confirm.
 */
public class BookTripFrame extends JFrame {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM, HH:mm");

    private final User currentUser;
    private final RouteDAO routeDAO = new RouteDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingService bookingService = new BookingService();

    private JComboBox<String> cmbFrom;
    private JComboBox<String> cmbTo;
    private JButton btnSearch;

    private JTable tripTable;
    private DefaultTableModel tripTableModel;
    private List<Trip> currentTrips;

    private JPanel seatPanel;
    private JLabel lblSelectedSeat;
    private JButton btnBook;

    private Integer selectedTripId;
    private Integer selectedSeatId;
    private String selectedSeatNumber;

    public BookTripFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadCityDropdowns();
    }

    private void initUI() {
        setTitle("BusSheba - Book a Trip");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 680);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.BG_LIGHT);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(), BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new BorderLayout(10, 10));
        centerWrap.setBackground(Theme.BG_LIGHT);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        centerWrap.add(buildResultsPanel(), BorderLayout.CENTER);
        centerWrap.add(buildSeatPanel(), BorderLayout.SOUTH);
        add(centerWrap, BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.TEAL_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Search Buses");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT_ON_TEAL);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(lblTitle);
        header.add(Box.createVerticalStrut(12));

        JPanel searchRow = new JPanel(new GridBagLayout());
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbFrom = new JComboBox<>();
        styleCombo(cmbFrom);
        gbc.gridx = 0;
        gbc.weightx = 1;
        searchRow.add(labeled("From", cmbFrom), gbc);

        cmbTo = new JComboBox<>();
        styleCombo(cmbTo);
        gbc.gridx = 1;
        searchRow.add(labeled("To", cmbTo), gbc);

        btnSearch = new JButton("Search Buses");
        btnSearch.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        btnSearch.setForeground(Color.WHITE);
        btnSearch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSearch.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:8;background:rgb(230,126,34);hoverBackground:rgb(202,106,18);borderWidth:0;margin:10,20,10,20");
        btnSearch.addActionListener(e -> onSearch());
        gbc.gridx = 2;
        gbc.weightx = 0;
        gbc.insets = new Insets(16, 0, 0, 0);
        searchRow.add(btnSearch, gbc);

        header.add(searchRow);
        return header;
    }

    private JPanel labeled(String labelText, JComponent field) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 4));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 11));
        label.setForeground(new Color(224, 242, 241));
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildResultsPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Theme.CARD_WHITE);
        wrap.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));

        String[] columns = {"Bus", "Departure", "Arrival", "Price"};
        tripTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tripTable = new JTable(tripTableModel);
        tripTable.setRowHeight(30);
        tripTable.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        tripTable.getTableHeader().setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 12));
        tripTable.getTableHeader().setBackground(Theme.TEAL_LIGHT);
        tripTable.setSelectionBackground(Theme.TEAL_LIGHT);
        tripTable.setSelectionForeground(Theme.TEXT_DARK);
        tripTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tripTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTripSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tripTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        wrap.add(scrollPane, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildSeatPanel() {
        JPanel container = new JPanel(new BorderLayout(5, 8));
        container.setBackground(Theme.CARD_WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)));

        JLabel lblSeatsTitle = new JLabel("Select a trip above to see available seats");
        lblSeatsTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblSeatsTitle.setForeground(Theme.TEXT_MUTED);
        container.add(lblSeatsTitle, BorderLayout.NORTH);

        seatPanel = new JPanel(new GridLayout(0, 6, 8, 8));
        seatPanel.setBackground(Theme.CARD_WHITE);
        JScrollPane seatScroll = new JScrollPane(seatPanel);
        seatScroll.setPreferredSize(new Dimension(650, 180));
        seatScroll.setBorder(BorderFactory.createEmptyBorder());
        container.add(seatScroll, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);

        lblSelectedSeat = new JLabel("No seat selected");
        lblSelectedSeat.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        lblSelectedSeat.setForeground(Theme.TEXT_DARK);
        bottomRow.add(lblSelectedSeat, BorderLayout.WEST);

        btnBook = new JButton("Confirm Booking");
        btnBook.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        btnBook.setForeground(Color.WHITE);
        btnBook.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBook.setEnabled(false);
        btnBook.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:8;background:rgb(230,126,34);hoverBackground:rgb(202,106,18);"
                + "disabledBackground:rgb(224,224,224);borderWidth:0;margin:8,16,8,16");
        btnBook.addActionListener(e -> onConfirmBooking());
        bottomRow.add(btnBook, BorderLayout.EAST);

        container.add(bottomRow, BorderLayout.SOUTH);
        return container;
    }

    private void loadCityDropdowns() {
        try {
            List<Route> routes = routeDAO.findAll();
            TreeSet<String> cities = new TreeSet<>();
            for (Route route : routes) {
                cities.add(route.getOrigin());
                cities.add(route.getDestination());
            }

            cmbFrom.removeAllItems();
            cmbTo.removeAllItems();
            for (String city : cities) {
                cmbFrom.addItem(city);
                cmbTo.addItem(city);
            }

            if (cmbTo.getItemCount() > 1) {
                cmbTo.setSelectedIndex(1); // avoid From == To by default when possible
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load cities: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSearch() {
        String origin = (String) cmbFrom.getSelectedItem();
        String destination = (String) cmbTo.getSelectedItem();

        if (origin == null || destination == null) {
            JOptionPane.showMessageDialog(this, "No routes available yet.", "No routes", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (origin.equals(destination)) {
            JOptionPane.showMessageDialog(this, "From and To can't be the same city.",
                    "Invalid selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Route> routes = routeDAO.findByOriginAndDestination(origin, destination);

            tripTableModel.setRowCount(0);
            currentTrips = new java.util.ArrayList<>();
            resetSeatSelection();

            for (Route route : routes) {
                List<Trip> trips = tripDAO.findByRouteId(route.getId());
                for (Trip trip : trips) {
                    currentTrips.add(trip);
                    tripTableModel.addRow(new Object[]{
                            "Bus #" + trip.getBusId(),
                            trip.getDepartureTime().format(DATE_FMT),
                            trip.getArrivalTime().format(DATE_FMT),
                            trip.getBasePrice()
                    });
                }
            }

            if (currentTrips.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No trips found for that route.",
                        "No results", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onTripSelected() {
        int row = tripTable.getSelectedRow();
        if (row < 0 || currentTrips == null || row >= currentTrips.size()) {
            return;
        }

        Trip trip = currentTrips.get(row);
        selectedTripId = trip.getId();
        resetSeatSelection();
        loadSeatsForTrip(trip.getId());
    }

    private void loadSeatsForTrip(int tripId) {
        seatPanel.removeAll();

        try {
            List<Seat> seats = seatDAO.findByTripId(tripId);

            if (seats.isEmpty()) {
                JLabel lblNone = new JLabel("No seats configured for this trip yet.");
                lblNone.setForeground(Theme.TEXT_MUTED);
                seatPanel.add(lblNone);
            }

            for (Seat seat : seats) {
                JButton seatButton = new JButton(seat.getSeatNumber());
                seatButton.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 12));
                boolean available = seat.getStatus() == Seat.Status.AVAILABLE;

                seatButton.setEnabled(available);
                seatButton.setCursor(new Cursor(available ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

                String bg = available
                        ? "rgb(" + Theme.SEAT_AVAILABLE.getRed() + "," + Theme.SEAT_AVAILABLE.getGreen() + "," + Theme.SEAT_AVAILABLE.getBlue() + ")"
                        : "rgb(" + Theme.SEAT_BOOKED.getRed() + "," + Theme.SEAT_BOOKED.getGreen() + "," + Theme.SEAT_BOOKED.getBlue() + ")";
                String fg = available ? "white" : "rgb(150,150,150)";

                seatButton.putClientProperty(FlatClientProperties.STYLE, ""
                        + "arc:8;borderWidth:0;foreground:" + fg + ";background:" + bg + ";"
                        + "hoverBackground:rgb(230,126,34)");

                if (available) {
                    seatButton.addActionListener(e -> onSeatClicked(seat, seatButton));
                }

                seatPanel.add(seatButton);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load seats: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private void onSeatClicked(Seat seat, JButton clickedButton) {
        for (Component comp : seatPanel.getComponents()) {
            if (comp instanceof JButton button) {
                button.putClientProperty(FlatClientProperties.OUTLINE, null);
            }
        }
        clickedButton.putClientProperty(FlatClientProperties.OUTLINE, "rgb(230,126,34)");

        selectedSeatId = seat.getId();
        selectedSeatNumber = seat.getSeatNumber();
        lblSelectedSeat.setText("Selected seat: " + selectedSeatNumber);
        btnBook.setEnabled(true);
    }

    private void resetSeatSelection() {
        selectedSeatId = null;
        selectedSeatNumber = null;
        lblSelectedSeat.setText("No seat selected");
        btnBook.setEnabled(false);
        seatPanel.removeAll();
        seatPanel.revalidate();
        seatPanel.repaint();
    }

    private void onConfirmBooking() {
        if (selectedTripId == null || selectedSeatId == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Confirm booking seat " + selectedSeatNumber + "?",
                "Confirm Booking", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Booking booking = bookingService.bookSeat(currentUser.getId(), selectedTripId, selectedSeatId);
            JOptionPane.showMessageDialog(this,
                    "Booking confirmed! Booking #" + booking.getId() + " - Seat " + selectedSeatNumber,
                    "Success", JOptionPane.INFORMATION_MESSAGE);

            loadSeatsForTrip(selectedTripId);
            resetSeatSelection();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
            loadSeatsForTrip(selectedTripId);
            resetSeatSelection();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        combo.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:8;background:white;borderWidth:0");
    }
}
