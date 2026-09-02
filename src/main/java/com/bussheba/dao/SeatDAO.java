package com.bussheba.dao;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the `seats` table. When an admin creates a Trip, the
 * service layer should also call insert() once per physical seat (e.g.
 * A1..A40) so BookTripFrame has something to show the customer.
 */
public class SeatDAO {

    public Seat insert(Seat seat) throws SQLException {
        String sql = "INSERT INTO seats (trip_id, seat_number, seat_class, status) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, seat.getTripId());
            ps.setString(2, seat.getSeatNumber());
            ps.setString(3, seat.getSeatClass());
            ps.setString(4, seat.getStatus().name());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    seat.setId(rs.getInt("id"));
                }
            }
        }
        return seat;
    }

    /** Convenience batch insert — generates seat_number A1..A{count} for a new trip. */
    public void insertSeatsForTrip(int tripId, int count, String seatClass) throws SQLException {
        String sql = "INSERT INTO seats (trip_id, seat_number, seat_class, status) VALUES (?, ?, ?, 'AVAILABLE')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 1; i <= count; i++) {
                ps.setInt(1, tripId);
                ps.setString(2, "A" + i);
                ps.setString(3, seatClass);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<Seat> findById(int id) throws SQLException {
        String sql = "SELECT * FROM seats WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    /** All seats for a trip, used to render the seat map in BookTripFrame. */
    public List<Seat> findByTripId(int tripId) throws SQLException {
        String sql = "SELECT * FROM seats WHERE trip_id = ? ORDER BY seat_number";
        List<Seat> seats = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRow(rs));
                }
            }
        }
        return seats;
    }

    /** Only the seats still open to book — filters out BOOKED/LOCKED. */
    public List<Seat> findAvailableByTripId(int tripId) throws SQLException {
        String sql = "SELECT * FROM seats WHERE trip_id = ? AND status = 'AVAILABLE' ORDER BY seat_number";
        List<Seat> seats = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    seats.add(mapRow(rs));
                }
            }
        }
        return seats;
    }

    /**
     * Flips a seat's status — BookingService calls this to mark a seat
     * BOOKED when a booking is confirmed, or back to AVAILABLE on cancel.
     */
    public boolean updateStatus(int seatId, Seat.Status status) throws SQLException {
        String sql = "UPDATE seats SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, seatId);
            return ps.executeUpdate() > 0;
        }
    }

    private Seat mapRow(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getInt("id"),
                rs.getInt("trip_id"),
                rs.getString("seat_number"),
                rs.getString("seat_class"),
                Seat.Status.valueOf(rs.getString("status"))
        );
    }
}
