package com.bussheba.ui.admin;

import com.bussheba.ui.components.Theme;

import com.bussheba.dao.BookingDAO;
import com.bussheba.dao.RouteDAO;
import com.bussheba.dao.SeatDAO;
import com.bussheba.dao.TripDAO;
import com.bussheba.dao.UserDAO;
import com.bussheba.model.Booking;
import com.bussheba.model.Route;
import com.bussheba.model.Seat;
import com.bussheba.model.Trip;
import com.bussheba.model.User;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Admin read-only view: every booking across all customers, plus total
 * revenue. "Revenue" here means the sum of total_amount for CONFIRMED
 * bookings only — cancelled bookings don't count toward it, matching
 * what the business actually collected (or would collect once payment
 * is wired in; PaymentDAO isn't yet called from BookingService, so this
 * is booking-confirmed revenue, not confirmed-payment revenue).
 */
public class BookingsRevenuePanel extends JFrame {

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TripDAO tripDAO = new TripDAO();
    private final RouteDAO routeDAO = new RouteDAO();
    private final SeatDAO seatDAO = new SeatDAO();

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private JLabel lblRevenue;
    private JLabel lblCounts;

    public BookingsRevenuePanel() {
        initUI();
        loadBookings();
    }

    private void initUI() {
        setTitle("BusSheba Admin - Bookings & Revenue");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(850, 550);
        setLocationRelativeTo(null);

        getContentPane().setBackground(Theme.BG_LIGHT);
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Theme.BG_LIGHT);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));

        JLabel lblTitle = new JLabel("All Bookings & Revenue");
        lblTitle.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 20));
        lblTitle.setForeground(Theme.TEXT_DARK);
        headerPanel.add(lblTitle);

        lblRevenue = new JLabel("Total Revenue (confirmed bookings): -");
        lblRevenue.setFont(new Font(FlatRobotoFont.FAMILY, Font.BOLD, 15));
        lblRevenue.setForeground(new Color(129, 140, 248));
        lblRevenue.setBorder(BorderFactory.createEmptyBorder(8, 0, 2, 0));
        headerPanel.add(lblRevenue);

        lblCounts = new JLabel("-");
        lblCounts.setFont(new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 12));
        lblCounts.setForeground(Theme.TEXT_MUTED);
        headerPanel.add(lblCounts);

        add(headerPanel, BorderLayout.NORTH);

        String[] columns = {"Booking #", "Customer", "Route", "Departure", "Seat", "Amount", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingTable = new JTable(tableModel);
        bookingTable.setRowHeight(26);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadBookings() {
        try {
            List<Booking> bookings = bookingDAO.findAll();
            tableModel.setRowCount(0);

            BigDecimal totalRevenue = BigDecimal.ZERO;
            int confirmedCount = 0;
            int cancelledCount = 0;

            for (Booking booking : bookings) {
                String customerLabel = userDAO.findById(booking.getUserId())
                        .map(User::getName)
                        .orElse("Unknown user");

                String routeLabel = "-";
                String departureLabel = "-";
                Optional<Trip> tripOpt = tripDAO.findById(booking.getTripId());
                if (tripOpt.isPresent()) {
                    Trip trip = tripOpt.get();
                    departureLabel = trip.getDepartureTime().format(DISPLAY_FMT);
                    Optional<Route> routeOpt = routeDAO.findById(trip.getRouteId());
                    if (routeOpt.isPresent()) {
                        routeLabel = routeOpt.get().getOrigin() + " -> " + routeOpt.get().getDestination();
                    }
                }

                String seatLabel = seatDAO.findById(booking.getSeatId())
                        .map(Seat::getSeatNumber)
                        .orElse("-");

                tableModel.addRow(new Object[]{
                        "#" + booking.getId(), customerLabel, routeLabel, departureLabel,
                        seatLabel, booking.getTotalAmount(), booking.getStatus()
                });

                if (booking.getStatus() == Booking.Status.CONFIRMED) {
                    totalRevenue = totalRevenue.add(booking.getTotalAmount());
                    confirmedCount++;
                } else {
                    cancelledCount++;
                }
            }

            lblRevenue.setText("Total Revenue (confirmed bookings): " + totalRevenue);
            lblCounts.setText(confirmedCount + " confirmed, " + cancelledCount + " cancelled, "
                    + bookings.size() + " total bookings");

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load bookings: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
