package com.bussheba.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to the `bookings` table.
 *
 * id               SERIAL PRIMARY KEY
 * user_id          INTEGER NOT NULL REFERENCES users(id)
 * trip_id          INTEGER NOT NULL REFERENCES trips(id)
 * seat_id          INTEGER NOT NULL REFERENCES seats(id)   UNIQUE
 * booking_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 * status           VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED'
 * total_amount     NUMERIC(10,2) NOT NULL CHECK (total_amount >= 0)
 *
 * uq_booking_seat enforces one active booking per seat at the DB level;
 * BookingService is still responsible for the cancel/re-book lifecycle.
 */
public class Booking {

    /** Mirrors the free-text values expected in bookings.status. */
    public enum Status {
        CONFIRMED,
        CANCELLED
    }

    private Integer id;
    private int userId;
    private int tripId;
    private int seatId;
    private LocalDateTime bookingDate;
    private Status status;
    private BigDecimal totalAmount;

    public Booking() {
    }

    public Booking(Integer id, int userId, int tripId, int seatId, LocalDateTime bookingDate,
                   Status status, BigDecimal totalAmount) {
        this.id = id;
        this.userId = userId;
        this.tripId = tripId;
        this.seatId = seatId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    /** Convenience constructor for creating a new booking (id/bookingDate filled by DB defaults). */
    public Booking(int userId, int tripId, int seatId, BigDecimal totalAmount) {
        this.userId = userId;
        this.tripId = tripId;
        this.seatId = seatId;
        this.totalAmount = totalAmount;
        this.status = Status.CONFIRMED;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isCancelled() {
        return status == Status.CANCELLED;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", userId=" + userId +
                ", tripId=" + tripId +
                ", seatId=" + seatId +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
