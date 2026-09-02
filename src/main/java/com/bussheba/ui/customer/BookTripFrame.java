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
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Customer flow: search a route (origin/destination) -> pick a trip from
 * the results -> pick an available seat -> confirm booking.
 *
 * Deliberately three DAOs + one service, no single "TripSearchService":
 * this screen is simple enough that going straight to RouteDAO/TripDAO/
 * SeatDAO for reads is fine; only the actual booking write needs the
 * transactional BookingService.
 */
public class BookTripFrame extends JFrame {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM, HH:mm");

    private final User currentUser;
    private final RouteDAO routeDAO = new RouteDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingService bookingService = new BookingService();

    private JTextField txtOrigin;
    private JTextField txtDestination;
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
    }

    private void initUI() {
        setTitle("BusSheba - Book a Trip");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 650);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(10, 10));

        add(buildSearchPanel(), BorderLayout.NORTH);
        add(buildResultsPanel(), BorderLayout.CENTER);
        add(buildSeatPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(24, 24, 27));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtOrigin = new JTextField();
        styleField(txtOrigin, "From");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        panel.add(txtOrigin, gbc);

        txtDestination = new JTextField();
        styleField(txtDestination, "To");
        gbc.gridx = 1;
        panel.add(txtDestination, gbc);

        btnSearch = new JButton("Search");
        stylePrimaryButton(btnSearch);
        btnSearch.addActionListener(e -> onSearch());
        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(btnSearch, gbc);

        return panel;
    }

    private JScrollPane buildResultsPanel() {
        String[] columns = {"Bus", "Departure", "Arrival", "Price"};
        tripTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tripTable = new JTable(tripTableModel);
        tripTable.setRowHeight(28);
        tripTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tripTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onTripSelected();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tripTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        return scrollPane;
    }

    private JPanel buildSeatPanel() {
        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.setBackground(new Color(24, 24, 27));
        container.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

        JLabel lblSeatsTitle = new JLabel("Select a trip above to see available seats");
        lblSeatsTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblSeatsTitle.setForeground(new Color(161, 161, 170));
        container.add(lblSeatsTitle, BorderLayout.NORTH);

        seatPanel = new JPanel(new GridLayout(0, 6, 6, 6));
        seatPanel.setBackground(new Color(24, 24, 27));
        JScrollPane seatScroll = new JScrollPane(seatPanel);
        seatScroll.setPreferredSize(new Dimension(650, 180));
        seatScroll.setBorder(BorderFactory.createEmptyBorder());
        container.add(seatScroll, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(new Color(24, 24, 27));

        lblSelectedSeat = new JLabel("No seat selected");
        lblSelectedSeat.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        lblSelectedSeat.setForeground(new Color(244, 244, 245));
        bottomRow.add(lblSelectedSeat, BorderLayout.WEST);

        btnBook = new JButton("Confirm Booking");
        stylePrimaryButton(btnBook);
        btnBook.setEnabled(false);
        btnBook.addActionListener(e -> onConfirmBooking());
        bottomRow.add(btnBook, BorderLayout.EAST);

        container.add(bottomRow, BorderLayout.SOUTH);
        return container;
    }

    private void onSearch() {
        String origin = txtOrigin.getText().trim();
        String destination = txtDestination.getText().trim();

        if (origin.isEmpty() || destination.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both origin and destination.",
                    "Missing info", JOptionPane.WARNING_MESSAGE);
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
                lblNone.setForeground(new Color(161, 161, 170));
                seatPanel.add(lblNone);
            }

            for (Seat seat : seats) {
                JButton seatButton = new JButton(seat.getSeatNumber());
                seatButton.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 12));
                boolean available = seat.getStatus() == Seat.Status.AVAILABLE;

                seatButton.setEnabled(available);
                seatButton.setCursor(new Cursor(available ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
                seatButton.putClientProperty(FlatClientProperties.STYLE, ""
                        + "arc:8;"
                        + "borderWidth:0;"
                        + "foreground:" + (available ? "rgb(244,244,245)" : "rgb(113,113,122)") + ";"
                        + "background:" + (available ? "rgb(39,39,42)" : "rgb(24,24,27)") + ";"
                        + "hoverBackground:rgb(99,102,241)");

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
        // Reset all seat button borders, then highlight the clicked one.
        for (Component comp : seatPanel.getComponents()) {
            if (comp instanceof JButton button) {
                button.putClientProperty(FlatClientProperties.OUTLINE, null);
            }
        }
        clickedButton.putClientProperty(FlatClientProperties.OUTLINE, "rgb(99,102,241)");

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

            // Refresh the seat map so the just-booked seat shows as unavailable.
            loadSeatsForTrip(selectedTripId);
            resetSeatSelection();

        } catch (IllegalStateException ex) {
            // Seat got taken by someone else between selection and click, or trip/seat mismatch.
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking Failed", JOptionPane.ERROR_MESSAGE);
            loadSeatsForTrip(selectedTripId);
            resetSeatSelection();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
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
                + "arc:10;"
                + "background:rgb(99,102,241);"
                + "hoverBackground:rgb(79,70,229);"
                + "borderWidth:0;"
                + "margin:8,16,8,16");
    }
}
