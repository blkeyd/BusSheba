package com.bussheba.dao;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Trip;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the `trips` table. This is the table customers search
 * against (BookTripFrame) and admins manage (ManageTripsPanel).
 */
public class TripDAO {

    public Trip insert(Trip trip) throws SQLException {
        String sql = "INSERT INTO trips (bus_id, route_id, departure_time, arrival_time, base_price) " +
                     "VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, trip.getBusId());
            ps.setInt(2, trip.getRouteId());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(trip.getDepartureTime()));
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(trip.getArrivalTime()));
            ps.setBigDecimal(5, trip.getBasePrice());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    trip.setId(rs.getInt("id"));
                }
            }
        }
        return trip;
    }

    public Optional<Trip> findById(int id) throws SQLException {
        String sql = "SELECT * FROM trips WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Trip> findAll() throws SQLException {
        String sql = "SELECT * FROM trips ORDER BY departure_time";
        List<Trip> trips = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                trips.add(mapRow(rs));
            }
        }
        return trips;
    }

    /** Used by BookTripFrame — all trips running on a given route, soonest first. */
    public List<Trip> findByRouteId(int routeId) throws SQLException {
        String sql = "SELECT * FROM trips WHERE route_id = ? ORDER BY departure_time";
        List<Trip> trips = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, routeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trips.add(mapRow(rs));
                }
            }
        }
        return trips;
    }

    public boolean update(Trip trip) throws SQLException {
        String sql = "UPDATE trips SET bus_id = ?, route_id = ?, departure_time = ?, " +
                     "arrival_time = ?, base_price = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, trip.getBusId());
            ps.setInt(2, trip.getRouteId());
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(trip.getDepartureTime()));
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(trip.getArrivalTime()));
            ps.setBigDecimal(5, trip.getBasePrice());
            ps.setInt(6, trip.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /** RESTRICTed if bookings still reference this trip's seats (fk_bookings_trip). */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM trips WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Trip mapRow(ResultSet rs) throws SQLException {
        return new Trip(
                rs.getInt("id"),
                rs.getInt("bus_id"),
                rs.getInt("route_id"),
                rs.getTimestamp("departure_time").toLocalDateTime(),
                rs.getTimestamp("arrival_time").toLocalDateTime(),
                rs.getBigDecimal("base_price")
        );
    }
}
