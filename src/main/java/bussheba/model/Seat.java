package com.bussheba.model;

/**
 * Maps to the `seats` table.
 *
 * id             SERIAL PRIMARY KEY
 * trip_id        INTEGER NOT NULL REFERENCES trips(id) ON DELETE CASCADE
 * seat_number    VARCHAR(20) NOT NULL
 * seat_class     VARCHAR(30) NOT NULL
 * status         VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
 *
 * UNIQUE (trip_id, seat_number)
 */
public class Seat {

    /** Mirrors the free-text values expected in seats.status. */
    public enum Status {
        AVAILABLE,
        LOCKED,     // optional: reserved during checkout, before payment confirms
        BOOKED
    }

    private Integer id;
    private int tripId;
    private String seatNumber;
    private String seatClass;
    private Status status;

    public Seat() {
    }

    public Seat(Integer id, int tripId, String seatNumber, String seatClass, Status status) {
        this.id = id;
        this.tripId = tripId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.status = status;
    }

    public Seat(int tripId, String seatNumber, String seatClass) {
        this.tripId = tripId;
        this.seatNumber = seatNumber;
        this.seatClass = seatClass;
        this.status = Status.AVAILABLE;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatClass() {
        return seatClass;
    }

    public void setSeatClass(String seatClass) {
        this.seatClass = seatClass;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "id=" + id +
                ", tripId=" + tripId +
                ", seatNumber='" + seatNumber + '\'' +
                ", seatClass='" + seatClass + '\'' +
                ", status=" + status +
                '}';
    }
}
