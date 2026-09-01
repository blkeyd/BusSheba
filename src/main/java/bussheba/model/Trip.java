package com.bussheba.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to the `trips` table.
 *
 * id                SERIAL PRIMARY KEY
 * bus_id            INTEGER NOT NULL REFERENCES buses(id)
 * route_id          INTEGER NOT NULL REFERENCES routes(id)
 * departure_time    TIMESTAMP NOT NULL
 * arrival_time      TIMESTAMP NOT NULL CHECK (arrival_time > departure_time)
 * base_price        NUMERIC(10,2) NOT NULL CHECK (base_price >= 0)
 *
 * Foreign keys are kept as plain ids here (not embedded Bus/Route objects)
 * so the model stays a direct row mapping; DAOs can join and hydrate
 * richer view objects separately if the UI ever needs them.
 */
public class Trip {

    private Integer id;
    private int busId;
    private int routeId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal basePrice;

    public Trip() {
    }

    public Trip(Integer id, int busId, int routeId, LocalDateTime departureTime,
                LocalDateTime arrivalTime, BigDecimal basePrice) {
        this.id = id;
        this.busId = busId;
        this.routeId = routeId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.basePrice = basePrice;
    }

    public Trip(int busId, int routeId, LocalDateTime departureTime,
                LocalDateTime arrivalTime, BigDecimal basePrice) {
        this.busId = busId;
        this.routeId = routeId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.basePrice = basePrice;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getBusId() {
        return busId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    /** Mirrors the chk_trip_times CHECK constraint, for validation before an INSERT/UPDATE. */
    public boolean hasValidTimes() {
        return departureTime != null && arrivalTime != null && arrivalTime.isAfter(departureTime);
    }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + id +
                ", busId=" + busId +
                ", routeId=" + routeId +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", basePrice=" + basePrice +
                '}';
    }
}
