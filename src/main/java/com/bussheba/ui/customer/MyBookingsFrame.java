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
import com.bussheba.ui.components.Theme;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Customer's booking history — light/teal theme. Deliberately shows ONLY
 * CONFIRMED bookings: once cancelled, a booking drops out of this list
 * entirely rather than sticking around with a "CANCELLED" label. The row
 * still exists in the database (for admin's revenue history) — this
 * screen just chooses not to surface it to the customer anymore.
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

    public MyBookingsFrame(User currentUser) {
        this.currentUser = currentUser;
        initUI();
        loadBookings();
    }

    private void initUI() {
        setTitle("BusSheba - My Bookings");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 500);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.BG_LIGHT);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.TEAL_PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel lblTitle = new JLabel("My Bookings");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 18));
        lblTitle.setForeground(Theme.TEXT_ON_TEAL);
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setBackground(Theme.BG_LIGHT);
        centerWrap.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] columns = {"Booking #", "Route", "Departure", "Seat", "Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(30);
        bookingTable.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
        bookingTable.getTableHeader().setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 12));
        bookingTable.getTableHeader().setBackground(Theme.TEAL_LIGHT);
        bookingTable.setSelectionBackground(Theme.TEAL_LIGHT);
        bookingTable.setSelectionForeground(Theme.TEXT_DARK);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnCancel.setEnabled(bookingTable.getSelectedRow() >= 0);
            }
        });

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setBackground(Theme.CARD_WHITE);
        tableWrap.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableWrap.add(scrollPane, BorderLayout.CENTER);
        centerWrap.add(tableWrap, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        bottomRow.setBackground(Theme.BG_LIGHT);

        btnCancel = new JButton("Cancel Selected Booking");
        btnCancel.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 13));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancel.setEnabled(false);
        btnCancel.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc:8;background:rgb(211,47,47);hoverBackground:rgb(183,28,28);"
                + "disabledBackground:rgb(224,224,224);borderWidth:0;margin:8,16,8,16");
        btnCancel.addActionListener(e -> onCancelBooking());
        bottomRow.add(btnCancel);

        centerWrap.add(bottomRow, BorderLayout.SOUTH);
        add(centerWrap, BorderLayout.CENTER);
    }

    private void loadBookings() {
        try {
            List<Booking> allBookings = bookingDAO.findByUserId(currentUser.getId());

            // Only show CONFIRMED bookings — cancelled ones disappear from
            // this view entirely, even though the row still exists in the DB.
            currentBookings = new ArrayList<>();
            for (Booking booking : allBookings) {
                if (booking.getStatus() == Booking.Status.CONFIRMED) {
                    currentBookings.add(booking);
                }
            }

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
                        "#" + booking.getId(), routeLabel, departureLabel, seatLabel, booking.getTotalAmount()
                });
            }

            btnCancel.setEnabled(false);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
}
