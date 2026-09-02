package com.bussheba.model;

/**
 * Maps to the `buses` table.
 *
 * id            SERIAL PRIMARY KEY
 * bus_number    VARCHAR(50) NOT NULL UNIQUE
 * bus_name      VARCHAR(100) NOT NULL
 * bus_type      VARCHAR(50) NOT NULL
 * total_seats   INTEGER NOT NULL CHECK (total_seats > 0)
 */
public class Bus {

    private Integer id;
    private String busNumber;
    private String busName;
    private String busType;
    private int totalSeats;

    public Bus() {
    }

    public Bus(Integer id, String busNumber, String busName, String busType, int totalSeats) {
        this.id = id;
        this.busNumber = busNumber;
        this.busName = busName;
        this.busType = busType;
        this.totalSeats = totalSeats;
    }

    public Bus(String busNumber, String busName, String busType, int totalSeats) {
        this.busNumber = busNumber;
        this.busName = busName;
        this.busType = busType;
        this.totalSeats = totalSeats;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("totalSeats must be > 0");
        }
        this.totalSeats = totalSeats;
    }

    @Override
    public String toString() {
        return "Bus{" +
                "id=" + id +
                ", busNumber='" + busNumber + '\'' +
                ", busName='" + busName + '\'' +
                ", busType='" + busType + '\'' +
                ", totalSeats=" + totalSeats +
                '}';
    }
}
