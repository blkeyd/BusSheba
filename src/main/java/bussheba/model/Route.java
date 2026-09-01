package com.bussheba.model;

import java.math.BigDecimal;

/**
 * Maps to the `routes` table.
 *
 * id             SERIAL PRIMARY KEY
 * origin         VARCHAR(100) NOT NULL
 * destination    VARCHAR(100) NOT NULL
 * distance_km    NUMERIC(10,2) NOT NULL CHECK (distance_km >= 0)
 */
public class Route {

    private Integer id;
    private String origin;
    private String destination;
    private BigDecimal distanceKm;

    public Route() {
    }

    public Route(Integer id, String origin, String destination, BigDecimal distanceKm) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
    }

    public Route(String origin, String destination, BigDecimal distanceKm) {
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        if (distanceKm != null && distanceKm.signum() < 0) {
            throw new IllegalArgumentException("distanceKm must be >= 0");
        }
        this.distanceKm = distanceKm;
    }

    @Override
    public String toString() {
        return origin + " -> " + destination + " (" + distanceKm + " km)";
    }
}
