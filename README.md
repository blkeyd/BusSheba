# 🚌 BusSheba

### A full-stack bus ticket booking system — from seat selection to payment, built with Java Swing and PostgreSQL.

BusSheba replaces the chaos of phone-call and counter-based bus ticket booking with a proper layered desktop application: customers search routes, pick a seat on a live seat map, and book it — with the backend guaranteeing two people can never be sold the same seat, even if they click "Book" at the same instant.

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Swing](https://img.shields.io/badge/UI-Java%20Swing%20%2B%20FlatLaf-4F46E5)](https://www.formdev.com/flatlaf/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](#license)

---

## ✨ Features

**Customer**
- 🔐 Secure registration & login — passwords hashed with salted PBKDF2 (never stored in plain text)
- 🔍 Search trips by origin and destination
- 🪑 Interactive seat map — pick a specific seat, not just a generic "1 ticket"
- ✅ Race-condition-safe booking — two customers can't be sold the same seat, even booking at the exact same moment
- 📋 View booking history and cancel a confirmed booking (instantly frees the seat back up)

**Admin**
- 🚍 Manage buses (add/edit/delete, with seat capacity)
- 🗺️ Manage routes (origin, destination, distance)
- 🕒 Manage trips (assign a bus + route, set schedule and price — seats are **auto-generated** to match the bus's capacity)
- 👤 Manage users — promote/demote roles, remove accounts (with a safety lock against self-demotion/self-deletion)
- 💰 Bookings & Revenue dashboard — every booking across all customers, with live total revenue

**Under the hood**
- Clean layered architecture: `model → dao → service → ui`
- All booking/cancellation logic wrapped in real database transactions with row-level locking (`SELECT ... FOR UPDATE`), so concurrent bookings resolve safely instead of corrupting data
- Foreign keys and check constraints enforced at the database level, not just in the app
- A partial unique index ensures a seat can be rebooked after a cancellation, while still preventing two *active* bookings on the same seat

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Java 17 |
| **UI** | Java Swing + [FlatLaf](https://www.formdev.com/flatlaf/) (modern dark theme, custom-styled components) |
| **Database** | PostgreSQL |
| **DB Access** | Plain JDBC (`org.postgresql:postgresql` driver) — no ORM, hand-written SQL in the DAO layer |
| **Build Tool** | Maven |
| **Password Hashing** | PBKDF2WithHmacSHA256 (JDK built-in, salted, constant-time comparison) |
| **IDE** | Apache NetBeans (recommended — Swing GUI Builder support) |

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or later ([Eclipse Temurin](https://adoptium.net/) recommended)
- **PostgreSQL** (with [pgAdmin 4](https://www.pgadmin.org/) for managing the database)
- **Apache NetBeans** ([download](https://netbeans.apache.org/)) — or any IDE with Maven support
- **Maven** (bundled with NetBeans; install separately if using another IDE)

Verify Java is installed:
```bash
java -version
```

### 1. Clone the repository

```bash
git clone https://github.com/your-username/BusSheba.git
cd BusSheba
```

### 2. Set up the database

Open pgAdmin 4, create a new database (or use an existing one), then run the schema script against it via the Query Tool:

```bash
# schema.sql creates all 7 tables: users, buses, routes, trips, seats, bookings, payments
```

> 📄 See `schema.sql` in the project root for the full DDL, including foreign keys, check constraints, and indexes.

Optionally, load some test data to explore the app immediately:

```bash
# test-data.sql inserts one bus, one route (Dhaka → Sylhet), one trip, and 12 seats
```

### 3. Configure your database connection

Open `src/main/java/com/bussheba/db/DBConnection.java` and fill in your credentials:

```java
private static final String HOST = "localhost";
private static final String PORT = "5432";
private static final String DB_NAME = "your_database_name";
private static final String USER = "your_postgres_username";
private static final String PASSWORD = "your_postgres_password";
```

> ⚠️ These are plain constants for local development simplicity. For anything beyond personal/academic use, move these into environment variables or a config file excluded from version control.

Verify the connection works by right-clicking `DBConnection.java` in NetBeans → **Run File**. You should see:
```
Connected successfully to: your_database_name
```

### 4. Build the project

```bash
mvn clean install
```

Or in NetBeans: right-click the project → **Clean and Build**.

### 5. Run it

```bash
mvn exec:java -Dexec.mainClass="com.bussheba.Main"
```

Or in NetBeans: press **F6**, or click the green ▶ Run button.

The Sign In screen should appear.

---

## 📖 Usage

### First-time setup: create an admin account

1. Launch the app and click **"Don't have an account? Register"**.
2. Fill in your name, email, gender, and a password (6+ characters).
3. New accounts default to the `USER` role. To make yours an admin, run once in pgAdmin:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'your_email_here';
   ```
4. Log back in — you'll now land on the **Admin Dashboard**.

### As an admin

1. **Manage Buses** → add a bus with a seat count.
2. **Manage Routes** → add an origin/destination pair.
3. **Manage Trips** → pick your bus and route from the dropdowns, set a schedule and price. Seats are generated automatically.
4. **Bookings & Revenue** → monitor all activity across the platform.

### As a customer

1. Register a separate account (or use a `USER`-role account).
2. From the dashboard, click **Book a Trip**.
3. Search by origin/destination, select a trip from the results, click an available seat, and confirm.
4. View or cancel your bookings anytime from **My Bookings**.

---

## 📁 Project Structure

```
BusSheba/
├── pom.xml                                # Maven build config & dependencies
├── schema.sql                             # Full database DDL (7 tables, constraints, indexes)
├── test-data.sql                          # Optional sample data for local testing
└── src/main/java/com/bussheba/
    ├── Main.java                          # Application entry point (FlatLaf setup + launches LoginFrame)
    │
    ├── model/                             # Plain data classes — one per DB table
    │   ├── Role.java                      #   enum: USER, ADMIN
    │   ├── User.java
    │   ├── Bus.java
    │   ├── Route.java
    │   ├── Trip.java
    │   ├── Seat.java                      #   includes Status enum: AVAILABLE, LOCKED, BOOKED
    │   ├── Booking.java                   #   includes Status enum: CONFIRMED, CANCELLED
    │   └── Payment.java                   #   includes Status enum: PENDING, SUCCESS, FAILED
    │
    ├── db/
    │   └── DBConnection.java              # JDBC connection factory
    │
    ├── dao/                               # Data access — one class per table, raw SQL via JDBC
    │   ├── UserDAO.java
    │   ├── BusDAO.java
    │   ├── RouteDAO.java
    │   ├── TripDAO.java
    │   ├── SeatDAO.java
    │   ├── BookingDAO.java
    │   └── PaymentDAO.java
    │
    ├── util/
    │   └── PasswordUtil.java              # Salted PBKDF2 password hashing
    │
    ├── service/                           # Business rules & transactional operations
    │   ├── AuthService.java               #   register() / login()
    │   └── BookingService.java            #   bookSeat() / cancelBooking() — transactional, row-locked
    │
    └── ui/
        ├── LoginFrame.java
        ├── RegisterFrame.java
        ├── customer/
        │   ├── CustomerDashboard.java
        │   ├── BookTripFrame.java         #   search → select trip → select seat → book
        │   └── MyBookingsFrame.java       #   view/cancel own bookings
        └── admin/
            ├── AdminDashboard.java
            ├── ManageBusesPanel.java
            ├── ManageRoutesPanel.java
            ├── ManageTripsPanel.java      #   auto-generates seats on trip creation
            ├── ManageUsersPanel.java
            └── BookingsRevenuePanel.java  #   all bookings + total revenue
```

**Architecture at a glance:** `ui` calls `service` for anything transactional or business-rule-bound (auth, booking), and calls `dao` directly for simple reads/writes (listing buses, editing a route). `service` is the only layer that talks to `db` directly for multi-table transactions — everything else goes through a DAO.

---

## 🗺️ Future Roadmap

- [ ] Wire `PaymentDAO` into the booking flow — currently a booking is confirmed instantly with no payment record created
- [ ] Fuzzy/partial route search (currently requires an exact origin/destination match)
- [ ] "Forgot password" flow for account recovery
- [ ] Email notifications on booking confirmation/cancellation
- [ ] Seat class-based pricing (e.g. AC vs Non-AC pricing tiers per trip)
- [ ] Move `DBConnection` credentials to environment variables / a `.env`-style config
- [ ] Automated tests for `service` and `dao` layers
- [ ] Export bookings/revenue report to PDF or CSV
- [ ] Trip edit that safely regenerates seats when bus capacity changes

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2026 [Your Name]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<p align="center">Built as a portfolio project — feedback and contributions welcome.</p>
