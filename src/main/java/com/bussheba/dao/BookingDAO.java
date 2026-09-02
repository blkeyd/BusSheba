package com.bussheba.dao;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the `bookings` table.
 *
 * NOTE: creating a booking and flipping the matching seat to BOOKED are two
 * separate statements against two tables — BookingService should wrap both
 * in a single JDBC transaction (conn.setAutoCommit(false), commit/rollback)
 * so a booking can never exist with a seat that wasn't actually reserved,
 * or vice versa. This DAO only handles the `bookings` table itself.
 */
public class BookingDAO {

    public Booking insert(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (user_id, trip_id, seat_id, status, total_amount) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id, booking_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, booking.getUserId());
            ps.setInt(2, booking.getTripId());
            ps.setInt(3, booking.getSeatId());
            ps.setString(4, booking.getStatus().name());
            ps.setBigDecimal(5, booking.getTotalAmount());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    booking.setId(rs.getInt("id"));
                    booking.setBookingDate(rs.getTimestamp("booking_date").toLocalDateTime());
                }
            }
        }
        return booking;
    }

    public Optional<Booking> findById(int id) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Used by MyBookingsFrame — a customer's own booking history. */
    public List<Booking> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY booking_date DESC";
        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        }
        return bookings;
    }

    /** Used by AdminDashboard — every booking across all customers. */
    public List<Booking> findAll() throws SQLException {
        String sql = "SELECT * FROM bookings ORDER BY booking_date DESC";
        List<Booking> bookings = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
        }
        return bookings;
    }

    /** Flips status to CANCELLED. Pair this with SeatDAO.updateStatus(seatId, AVAILABLE) in the service layer. */
    public boolean updateStatus(int bookingId, Booking.Status status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("trip_id"),
                rs.getInt("seat_id"),
                rs.getTimestamp("booking_date").toLocalDateTime(),
                Booking.Status.valueOf(rs.getString("status")),
                rs.getBigDecimal("total_amount")
        );
    }
}
