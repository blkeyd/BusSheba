package com.bussheba.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Maps to the `payments` table.
 *
 * id                  SERIAL PRIMARY KEY
 * booking_id          INTEGER NOT NULL UNIQUE REFERENCES bookings(id) ON DELETE CASCADE
 * method              VARCHAR(50) NOT NULL
 * amount              NUMERIC(10,2) NOT NULL CHECK (amount >= 0)
 * status              VARCHAR(30) NOT NULL DEFAULT 'PENDING'
 * transaction_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
 *
 * booking_id is UNIQUE, so this is a strict one-to-one with Booking:
 * each booking has at most one payment row.
 */
public class Payment {

    /** Mirrors the free-text values expected in payments.status. */
    public enum Status {
        PENDING,
        SUCCESS,
        FAILED
    }

    private Integer id;
    private int bookingId;
    private String method;
    private BigDecimal amount;
    private Status status;
    private LocalDateTime transactionDate;

    public Payment() {
    }

    public Payment(Integer id, int bookingId, String method, BigDecimal amount,
                   Status status, LocalDateTime transactionDate) {
        this.id = id;
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.status = status;
        this.transactionDate = transactionDate;
    }

    /** Convenience constructor for recording a new payment attempt. */
    public Payment(int bookingId, String method, BigDecimal amount) {
        this.bookingId = bookingId;
        this.method = method;
        this.amount = amount;
        this.status = Status.PENDING;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESS;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", method='" + method + '\'' +
                ", amount=" + amount +
                ", status=" + status +
                '}';
    }
}
