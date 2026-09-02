package com.bussheba.dao;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Data access for the `payments` table. One-to-one with bookings
 * (booking_id is UNIQUE), so findByBookingId() is the main lookup —
 * there's no findAll()-by-user here since payments are always reached
 * through their booking.
 */
public class PaymentDAO {

    public Payment insert(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, method, amount, status) " +
                     "VALUES (?, ?, ?, ?) RETURNING id, transaction_date";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, payment.getBookingId());
            ps.setString(2, payment.getMethod());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getStatus().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payment.setId(rs.getInt("id"));
                    payment.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                }
            }
        }
        return payment;
    }

    public Optional<Payment> findById(int id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Used to show payment status alongside a booking in MyBookingsFrame / AdminDashboard. */
    public Optional<Payment> findByBookingId(int bookingId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE booking_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** Called once the (simulated) payment gateway responds, to flip PENDING -> SUCCESS/FAILED. */
    public boolean updateStatus(int paymentId, Payment.Status status) throws SQLException {
        String sql = "UPDATE payments SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("booking_id"),
                rs.getString("method"),
                rs.getBigDecimal("amount"),
                Payment.Status.valueOf(rs.getString("status")),
                rs.getTimestamp("transaction_date").toLocalDateTime()
        );
    }
}
