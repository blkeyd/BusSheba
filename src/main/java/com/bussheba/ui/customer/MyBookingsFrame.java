package com.bussheba.ui.customer;

import com.bussheba.dao.BookingDAO;
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
import java.util.Optional;

/**
 * Customer flow: view your own booking history and cancel a still-confirmed
 * booking. Cancelling goes through BookingService.cancelBooking(), the same
 * transactional method that frees the seat back up atomically.
 */
public class MyBookingsFrame extends JFrame {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final User currentUser;
    private final BookingDAO bookingDAO = new BookingDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final RouteDAO routeDAO = new RouteDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final BookingService bookingService = new BookingService();

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private List<Booking> currentBookings;

    private JButton btnCancel;
    private JButton btnRefresh;

    public MyBookingsFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadBookings();
    }

    private void initUI() {
        setTitle("BusSheba - My Bookings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(750, 500);
        setLocationRelativeTo(null);

        getContentPane().setBackground(new Color(24, 24, 27));
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitle = new JLabel("My Bookings");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(new Color(244, 244, 245));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        add(lblTitle, BorderLayout.NORTH);

        String[] columns = {"Booking #", "Route", "Departure", "Seat", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(28);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateCancelButtonState();
            }
        });

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomRow.setBackground(new Color(24, 24, 27));
        bottomRow.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        btnRefresh = new JButton("Refresh");
        styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadBookings());
        bottomRow.add(btnRefresh);

        btnCancel = new JButton("Cancel Selected Booking");
        styleDangerButton(btnCancel);
        btnCancel.setEnabled(false);
        btnCancel.addActionListener(e -> onCancelBooking());
        bottomRow.add(btnCancel);

        add(bottomRow, BorderLayout.SOUTH);
    }

    private void loadBookings() {
        try {
            currentBookings = bookingDAO.findByUserId(currentUser.getId());
            tableModel.setRowCount(0);

            for (Booking booking : currentBookings) {
                String routeLabel = "Unknown route";
                String departureLabel = "-";
                String seatLabel = "-";

                Optional<Trip> tripOpt = tripDAO.findById(booking.getTripId());
                if (tripOpt.isPresent()) {
                    Trip trip = tripOpt.get();
                    departureLabel = trip.getDepartureTime().format(DATE_FMT);

                    Optional<Route> routeOpt = routeDAO.findById(trip.getRouteId());
                    if (routeOpt.isPresent()) {
                        Route route = routeOpt.get();
                        routeLabel = route.getOrigin() + " -> " + route.getDestination();
                    }
                }

                Optional<Seat> seatOpt = seatDAO.findById(booking.getSeatId());
                if (seatOpt.isPresent()) {
                    seatLabel = seatOpt.get().getSeatNumber();
                }

                tableModel.addRow(new Object[]{
                        "#" + booking.getId(),
                        routeLabel,
                        departureLabel,
                        seatLabel,
                        booking.getTotalAmount(),
                        booking.getStatus()
                });
            }

            updateCancelButtonState();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCancelButtonState() {
        int row = bookingTable.getSelectedRow();
        if (row < 0 || currentBookings == null || row >= currentBookings.size()) {
            btnCancel.setEnabled(false);
            return;
        }
        Booking selected = currentBookings.get(row);
        btnCancel.setEnabled(selected.getStatus() == Booking.Status.CONFIRMED);
    }

    private void onCancelBooking() {
        int row = bookingTable.getSelectedRow();
        if (row < 0 || currentBookings == null || row >= currentBookings.size()) {
            return;
        }

        Booking selected = currentBookings.get(row);

        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel booking #" + selected.getId() + "? This will free up the seat for other customers.",
                "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            bookingService.cancelBooking(selected.getId());
            JOptionPane.showMessageDialog(this, "Booking #" + selected.getId() + " cancelled.",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            loadBookings();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Failed", JOptionPane.ERROR_MESSAGE);
            loadBookings();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        button.setForeground(new Color(244, 244, 245));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;"
                + "background:rgb(39,39,42);"
                + "hoverBackground:rgb(63,63,70);"
                + "borderWidth:0;"
                + "margin:8,16,8,16");
    }

    private void styleDangerButton(JButton button) {
        button.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:10;"
                + "background:rgb(220,38,38);"
                + "hoverBackground:rgb(185,28,28);"
                + "borderWidth:0;"
                + "margin:8,16,8,16");
    }
}
