-- =============================================
-- Airline Reservation System - Database Script (Tối ưu)
-- SQL Server
-- =============================================

-- Tạo Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'AirlineReservationDB')
BEGIN
    CREATE DATABASE AirlineReservationDB;
END
GO

USE AirlineReservationDB;
GO

-- =============================================
-- Xóa các bảng cũ nếu tồn tại (theo thứ tự phụ thuộc)
-- =============================================
IF OBJECT_ID('Payments', 'U') IS NOT NULL DROP TABLE Payments;
IF OBJECT_ID('BookingPassengers', 'U') IS NOT NULL DROP TABLE BookingPassengers; -- MỚI: Xóa bảng hành khách
IF OBJECT_ID('Bookings', 'U') IS NOT NULL DROP TABLE Bookings;
IF OBJECT_ID('Flights', 'U') IS NOT NULL DROP TABLE Flights;
IF OBJECT_ID('Routes', 'U') IS NOT NULL DROP TABLE Routes;
IF OBJECT_ID('Aircrafts', 'U') IS NOT NULL DROP TABLE Aircrafts;
IF OBJECT_ID('Users', 'U') IS NOT NULL DROP TABLE Users;
GO

-- =============================================
-- Tạo bảng Users
-- =============================================
CREATE TABLE Users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- =============================================
-- Tạo bảng Aircrafts
-- =============================================
CREATE TABLE Aircrafts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE() -- MỚI: Thêm cột updated_at
);

-- =============================================
-- Tạo bảng Routes
-- =============================================
CREATE TABLE Routes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL CHECK (distance_km > 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(), -- MỚI: Thêm cột updated_at
    CONSTRAINT unique_route UNIQUE (origin, destination),
    CONSTRAINT check_route_origin_dest CHECK (origin != destination) -- MỚI: Ràng buộc
);

-- =============================================
-- Tạo bảng Flights
-- =============================================
CREATE TABLE Flights (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL UNIQUE,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    available_seats INT NOT NULL CHECK (available_seats >= 0),
    route_id BIGINT NOT NULL,
    aircraft_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(), -- MỚI: Thêm cột updated_at
    CONSTRAINT fk_flight_route FOREIGN KEY (route_id) REFERENCES Routes(id),
    CONSTRAINT fk_flight_aircraft FOREIGN KEY (aircraft_id) REFERENCES Aircrafts(id),
    CONSTRAINT check_flight_times CHECK (arrival_time > departure_time)
);

-- =============================================
-- Tạo bảng Bookings
-- =============================================
CREATE TABLE Bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flight_id BIGINT NOT NULL,
    booking_date DATETIME NOT NULL DEFAULT GETDATE(),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    -- XÓA: Các cột passenger_* đã được chuyển sang bảng mới
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES Users(id),
    CONSTRAINT fk_booking_flight FOREIGN KEY (flight_id) REFERENCES Flights(id)
);

-- =============================================
-- Bảng này mô hình hóa việc 1 Booking có thể có NHIỀU hành khách
-- =============================================
CREATE TABLE BookingPassengers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    seat_number VARCHAR(10), -- Có thể thêm (ví dụ: '23A')
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_passenger_booking FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);

-- =============================================
-- Tạo bảng Payments
-- =============================================
CREATE TABLE Payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payment_date DATETIME NOT NULL DEFAULT GETDATE(),
    amount DECIMAL(10,2) NOT NULL CHECK (amount >= 0),
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),
    transaction_id VARCHAR(100),
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);

-- =============================================
-- Tạo các INDEX để tối ưu truy vấn (Lý do 2)
-- =============================================
CREATE INDEX idx_users_username ON Users(username);
CREATE INDEX idx_users_email ON Users(email);

CREATE INDEX idx_flights_number ON Flights(flight_number);
-- TỐI ƯU: Index gộp cho tìm kiếm chính
CREATE INDEX idx_flights_route_departure ON Flights(route_id, departure_time);
-- XÓA: idx_flights_departure (đã gộp)
-- XÓA: idx_flights_route (đã gộp)

CREATE INDEX idx_bookings_user ON Bookings(user_id);
CREATE INDEX idx_bookings_flight ON Bookings(flight_id);
CREATE INDEX idx_bookings_status ON Bookings(status);

CREATE INDEX idx_payments_booking ON Payments(booking_id);
CREATE INDEX idx_passengers_booking ON BookingPassengers(booking_id); -- MỚI: Index cho bảng mới

-- =============================================
-- Chèn dữ liệu mẫu
-- =============================================

-- Users (Password: "123456")
INSERT INTO Users (username, password, email, full_name, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@airline.com', 'System Administrator', 'ADMIN'),
('john_doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'john.doe@email.com', 'John Doe', 'USER'),
('jane_smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'jane.smith@email.com', 'Jane Smith', 'USER'),
('bob_wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bob.wilson@email.com', 'Bob Wilson', 'USER'),
('alice_brown', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'alice.brown@email.com', 'Alice Brown', 'USER');

-- Aircrafts
INSERT INTO Aircrafts (model_name, capacity) VALUES
('Boeing 737-800', 189),
('Airbus A320', 180),
('Boeing 787 Dreamliner', 242),
('Airbus A350', 300),
('Boeing 777-300ER', 396);

-- Routes
INSERT INTO Routes (origin, destination, distance_km) VALUES
('Hanoi', 'Ho Chi Minh City', 1166.00),
('Hanoi', 'Da Nang', 616.00),
('Ho Chi Minh City', 'Da Nang', 608.00),
('Hanoi', 'Nha Trang', 1072.00),
('Ho Chi Minh City', 'Phu Quoc', 300.00),
('Hanoi', 'Bangkok', 892.00),
('Ho Chi Minh City', 'Singapore', 1074.00);

-- Flights
INSERT INTO Flights (flight_number, departure_time, arrival_time, price, available_seats, route_id, aircraft_id) VALUES
('VN101', '2025-11-05 06:00:00', '2025-11-05 08:15:00', 1500000.00, 150, 1, 1),
('VN102', '2025-11-05 09:30:00', '2025-11-05 11:45:00', 1500000.00, 140, 1, 2),
('VN201', '2025-11-05 07:00:00', '2025-11-05 08:30:00', 800000.00, 170, 2, 1),
('VN202', '2025-11-05 14:00:00', '2025-11-05 15:30:00', 800000.00, 165, 2, 2),
('VN301', '2025-11-06 08:00:00', '2025-11-06 09:30:00', 900000.00, 160, 3, 1),
('VN401', '2025-11-06 10:00:00', '2025-11-06 12:00:00', 1200000.00, 200, 4, 3),
('VN501', '2025-11-07 06:30:00', '2025-11-07 07:30:00', 600000.00, 175, 5, 2),
('VN601', '2025-11-08 09:00:00', '2025-11-08 11:30:00', 3500000.00, 240, 6, 3),
('VN701', '2025-11-09 11:00:00', '2025-11-09 13:45:00', 4200000.00, 290, 7, 4);

-- Bookings (Đã bỏ thông tin hành khách)
-- (Giả sử Booking 2 là đặt cho 2 người)
INSERT INTO Bookings (user_id, flight_id, booking_date, status, total_price) VALUES
(2, 1, '2025-11-01 10:30:00', 'CONFIRMED', 1500000.00), -- Booking 1 (1 vé)
(3, 2, '2025-11-01 11:00:00', 'CONFIRMED', 3000000.00), -- Booking 2 (2 vé)
(2, 3, '2025-11-02 09:15:00', 'PENDING', 800000.00),   -- Booking 3 (1 vé)
(4, 5, '2025-11-02 14:20:00', 'CONFIRMED', 900000.00),  -- Booking 4 (1 vé)
(5, 7, '2025-11-03 08:45:00', 'CANCELLED', 600000.00);  -- Booking 5 (1 vé)

-- BookingPassengers (Dữ liệu hành khách cho các booking)
INSERT INTO BookingPassengers (booking_id, full_name, email, phone) VALUES
(1, 'John Doe', 'john.doe@email.com', '0901234567'), -- Hành khách của Booking 1
(2, 'Jane Smith', 'jane.smith@email.com', '0912345678'), -- Hành khách 1 của Booking 2
(2, 'Mike Smith', 'mike.smith@email.com', '0912345679'), -- Hành khách 2 của Booking 2
(3, 'John Doe', 'john.doe@email.com', '0901234567'), -- Hành khách của Booking 3
(4, 'Bob Wilson', 'bob.wilson@email.com', '0923456789'), -- Hành khách của Booking 4
(5, 'Alice Brown', 'alice.brown@email.com', '0934567890'); -- Hành khách của Booking 5

-- Payments (Cập nhật `amount` cho Booking 2)
INSERT INTO Payments (booking_id, payment_date, amount, payment_method, status, transaction_id) VALUES
(1, '2025-11-01 10:35:00', 1500000.00, 'CREDIT_CARD', 'SUCCESS', 'TXN1234567890'),
(2, '2025-11-01 11:05:00', 3000000.00, 'BANKING', 'SUCCESS', 'TXN1234567891'), -- SỬA: amount = 3M
(4, '2025-11-02 14:25:00', 900000.00, 'E_WALLET', 'SUCCESS', 'TXN1234567892');

GO

-- =============================================
-- Thống kê dữ liệu
-- =============================================
DECLARE @count_users INT,
        @count_aircrafts INT,
        @count_routes INT,
        @count_flights INT,
        @count_bookings INT,
        @count_passengers INT, -- MỚI
        @count_payments INT;

SELECT @count_users = COUNT(*) FROM Users;
SELECT @count_aircrafts = COUNT(*) FROM Aircrafts;
SELECT @count_routes = COUNT(*) FROM Routes;
SELECT @count_flights = COUNT(*) FROM Flights;
SELECT @count_bookings = COUNT(*) FROM Bookings;
SELECT @count_passengers = COUNT(*) FROM BookingPassengers; -- MỚI
SELECT @count_payments = COUNT(*) FROM Payments;

PRINT '=== DATABASE STATISTICS ===';
PRINT 'Users: ' + CAST(@count_users AS VARCHAR(10));
PRINT 'Aircrafts: ' + CAST(@count_aircrafts AS VARCHAR(10));
PRINT 'Routes: ' + CAST(@count_routes AS VARCHAR(10));
PRINT 'Flights: ' + CAST(@count_flights AS VARCHAR(10));
PRINT 'Bookings: ' + CAST(@count_bookings AS VARCHAR(10));
PRINT 'BookingPassengers: ' + CAST(@count_passengers AS VARCHAR(10)); -- MỚI
PRINT 'Payments: ' + CAST(@count_payments AS VARCHAR(10));
PRINT '=========================';

GO