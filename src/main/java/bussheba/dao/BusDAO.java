package com.bussheba.dao;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Bus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the `buses` table. Used by ManageTripsPanel (admin)
 * when creating trips (a trip needs an existing bus_id).
 */
public class BusDAO {

    public Bus insert(Bus bus) throws SQLException {
        String sql = "INSERT INTO buses (bus_number, bus_name, bus_type, total_seats) " +
                     "VALUES (?, ?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bus.getBusNumber());
            ps.setString(2, bus.getBusName());
            ps.setString(3, bus.getBusType());
            ps.setInt(4, bus.getTotalSeats());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bus.setId(rs.getInt("id"));
                }
            }
        }
        return bus;
    }

    public Optional<Bus> findById(int id) throws SQLException {
        String sql = "SELECT * FROM buses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Bus> findAll() throws SQLException {
        String sql = "SELECT * FROM buses ORDER BY id";
        List<Bus> buses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                buses.add(mapRow(rs));
            }
        }
        return buses;
    }

    public boolean update(Bus bus) throws SQLException {
        String sql = "UPDATE buses SET bus_number = ?, bus_name = ?, bus_type = ?, total_seats = ? " +
                     "WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bus.getBusNumber());
            ps.setString(2, bus.getBusName());
            ps.setString(3, bus.getBusType());
            ps.setInt(4, bus.getTotalSeats());
            ps.setInt(5, bus.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deleting a bus is RESTRICTed by the DB if any trips still reference it
     * (fk_trips_bus ON DELETE RESTRICT) — this will throw an SQLException in
     * that case. Callers should catch and show a friendly "remove trips
     * first" message.
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM buses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Bus mapRow(ResultSet rs) throws SQLException {
        return new Bus(
                rs.getInt("id"),
                rs.getString("bus_number"),
                rs.getString("bus_name"),
                rs.getString("bus_type"),
                rs.getInt("total_seats")
        );
    }
}
