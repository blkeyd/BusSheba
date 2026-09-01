package com.bussheba.dao;

import com.bussheba.db.DBConnection;
import com.bussheba.model.Route;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access for the `routes` table. Used by ManageTripsPanel (admin)
 * when creating trips (a trip needs an existing route_id), and by
 * BookTripFrame (customer) when searching trips by origin/destination.
 */
public class RouteDAO {

    public Route insert(Route route) throws SQLException {
        String sql = "INSERT INTO routes (origin, destination, distance_km) " +
                     "VALUES (?, ?, ?) RETURNING id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, route.getOrigin());
            ps.setString(2, route.getDestination());
            ps.setBigDecimal(3, route.getDistanceKm());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    route.setId(rs.getInt("id"));
                }
            }
        }
        return route;
    }

    public Optional<Route> findById(int id) throws SQLException {
        String sql = "SELECT * FROM routes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public List<Route> findAll() throws SQLException {
        String sql = "SELECT * FROM routes ORDER BY id";
        List<Route> routes = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                routes.add(mapRow(rs));
            }
        }
        return routes;
    }

    /** Used by the customer's search screen — e.g. "Dhaka" -> "Sylhet". */
    public List<Route> findByOriginAndDestination(String origin, String destination) throws SQLException {
        String sql = "SELECT * FROM routes WHERE origin ILIKE ? AND destination ILIKE ?";
        List<Route> routes = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, origin);
            ps.setString(2, destination);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    routes.add(mapRow(rs));
                }
            }
        }
        return routes;
    }

    public boolean update(Route route) throws SQLException {
        String sql = "UPDATE routes SET origin = ?, destination = ?, distance_km = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, route.getOrigin());
            ps.setString(2, route.getDestination());
            ps.setBigDecimal(3, route.getDistanceKm());
            ps.setInt(4, route.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /** RESTRICTed by fk_trips_route if trips still reference this route. */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM routes WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Route mapRow(ResultSet rs) throws SQLException {
        return new Route(
                rs.getInt("id"),
                rs.getString("origin"),
                rs.getString("destination"),
                rs.getBigDecimal("distance_km")
        );
    }
}
