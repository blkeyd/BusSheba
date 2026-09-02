package com.bussheba.service;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Booking;
import com.bussheba.model.Trip;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Business rules around booking and cancelling a seat.
 *
 * These two operations are kept out of the plain DAOs because each one
 * touches two tables (bookings + seats) and must be all-or-nothing:
 *   - bookSeat: insert a booking row AND flip the seat to BOOKED
 *   - cancelBooking: mark the booking CANCELLED AND flip the seat back to AVAILABLE
 *
 * If either half failed silently, you'd end up with a seat marked booked
 * with no real booking behind it (or a "confirmed" booking on a seat
 * someone else can still book). So both methods run inside a single JDBC
 * transaction (autoCommit=false, explicit commit/rollback).
 *
 * `SELECT ... FOR UPDATE` on the seat row also guards against the classic
 * race condition: two customers clicking "book" on the same seat at
 * nearly the same instant. Whichever transaction gets there first locks
 * the row; the second one sees the already-updated status and is
 * rejected cleanly instead of both succeeding.
 */
public class BookingService {

    /**
     * Books a seat for a user on a trip.
     *
     * @throws IllegalStateException if the seat is no longer available
     * @throws SQLException on a database error
     */
    public Booking bookSeat(int userId, int tripId, int seatId) throws SQLException {
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Lock the seat row so no other transaction can book it while we're deciding.
            String lockSeatSql = "SELECT status FROM seats WHERE id = ? AND trip_id = ? FOR UPDATE";
            String seatStatus;
            try (PreparedStatement ps = conn.prepareStatement(lockSeatSql)) {
                ps.setInt(1, seatId);
                ps.setInt(2, tripId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Seat does not exist for this trip.");
                    }
                    seatStatus = rs.getString("status");
                }
            }

            if (!"AVAILABLE".equals(seatStatus)) {
                conn.rollback();
                throw new IllegalStateException("This seat is no longer available.");
            }

            // Look up the trip's base price to compute total_amount.
            BigDecimal totalAmount;
            String tripSql = "SELECT base_price FROM trips WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(tripSql)) {
                ps.setInt(1, tripId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Trip not found.");
                    }
                    totalAmount = rs.getBigDecimal("base_price");
                }
            }

            // Insert the booking.
            int bookingId;
            java.time.LocalDateTime bookingDate;
            String insertBookingSql = "INSERT INTO bookings (user_id, trip_id, seat_id, status, total_amount) " +
                                       "VALUES (?, ?, ?, 'CONFIRMED', ?) RETURNING id, booking_date";
            try (PreparedStatement ps = conn.prepareStatement(insertBookingSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, tripId);
                ps.setInt(3, seatId);
                ps.setBigDecimal(4, totalAmount);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    bookingId = rs.getInt("id");
                    bookingDate = rs.getTimestamp("booking_date").toLocalDateTime();
                }
            }

            // Flip the seat to BOOKED.
            String updateSeatSql = "UPDATE seats SET status = 'BOOKED' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSeatSql)) {
                ps.setInt(1, seatId);
                ps.executeUpdate();
            }

            conn.commit();

            return new Booking(bookingId, userId, tripId, seatId, bookingDate, Booking.Status.CONFIRMED, totalAmount);

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    throw rollbackEx;
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Cancels a confirmed booking and frees its seat back up.
     *
     * @throws IllegalStateException if the booking doesn't exist or is already cancelled
     * @throws SQLException on a database error
     */
    public void cancelBooking(int bookingId) throws SQLException {
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int seatId;
            String status;
            String lockBookingSql = "SELECT seat_id, status FROM bookings WHERE id = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(lockBookingSql)) {
                ps.setInt(1, bookingId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalStateException("Booking not found.");
                    }
                    seatId = rs.getInt("seat_id");
                    status = rs.getString("status");
                }
            }

            if ("CANCELLED".equals(status)) {
                conn.rollback();
                throw new IllegalStateException("This booking is already cancelled.");
            }

            String cancelBookingSql = "UPDATE bookings SET status = 'CANCELLED' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(cancelBookingSql)) {
                ps.setInt(1, bookingId);
                ps.executeUpdate();
            }

            String freeSeatSql = "UPDATE seats SET status = 'AVAILABLE' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(freeSeatSql)) {
                ps.setInt(1, seatId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException | IllegalStateException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    throw rollbackEx;
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
}
