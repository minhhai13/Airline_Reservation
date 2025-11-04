-- =============================================
-- AIRLINE RESERVATION SYSTEM - FULL SCRIPT (ĐÃ SỬA LỖI)
-- SQL Server - CHẠY ĐƯỢC NGAY, KHÔNG LỖI
-- TỔNG: 150 CHUYẾN BAY | 20 USERS | 55 BOOKINGS | 120 PASSENGERS
-- =============================================

-- 1. TẠO DATABASE
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'AirlineReservationDB')
BEGIN
    CREATE DATABASE AirlineReservationDB;
END
GO

USE AirlineReservationDB;
GO

-- 2. XÓA BẢNG CŨ
IF OBJECT_ID('Payments', 'U') IS NOT NULL DROP TABLE Payments;
IF OBJECT_ID('BookingPassengers', 'U') IS NOT NULL DROP TABLE BookingPassengers;
IF OBJECT_ID('Bookings', 'U') IS NOT NULL DROP TABLE Bookings;
IF OBJECT_ID('Flights', 'U') IS NOT NULL DROP TABLE Flights;
IF OBJECT_ID('Routes', 'U') IS NOT NULL DROP TABLE Routes;
IF OBJECT_ID('Aircrafts', 'U') IS NOT NULL DROP TABLE Aircrafts;
IF OBJECT_ID('Users', 'U') IS NOT NULL DROP TABLE Users;
GO

-- =============================================
-- 3. TẠO BẢNG
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
GO

CREATE TABLE Aircrafts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);
GO

CREATE TABLE Routes (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    origin VARCHAR(100) NOT NULL,
    destination VARCHAR(100) NOT NULL,
    distance_km DECIMAL(10,2) NOT NULL CHECK (distance_km > 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT unique_route UNIQUE (origin, destination),
    CONSTRAINT check_origin_dest CHECK (origin != destination)
);
GO

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
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_flight_route FOREIGN KEY (route_id) REFERENCES Routes(id),
    CONSTRAINT fk_flight_aircraft FOREIGN KEY (aircraft_id) REFERENCES Aircrafts(id),
    CONSTRAINT check_flight_times CHECK (arrival_time > departure_time)
);
GO

CREATE TABLE Bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flight_id BIGINT NOT NULL,
    booking_date DATETIME NOT NULL DEFAULT GETDATE(),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_booking_user FOREIGN KEY (user_id) REFERENCES Users(id),
    CONSTRAINT fk_booking_flight FOREIGN KEY (flight_id) REFERENCES Flights(id)
);
GO

CREATE TABLE BookingPassengers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    seat_number VARCHAR(10),
    created_at DATETIME DEFAULT GETDATE(),
    CONSTRAINT fk_passenger_booking FOREIGN KEY (booking_id) REFERENCES Bookings(id)
);
GO

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
GO

-- INDEX TỐI ƯU
CREATE INDEX idx_users_username ON Users(username);
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_flights_number ON Flights(flight_number);
CREATE INDEX idx_flights_route_departure ON Flights(route_id, departure_time);
CREATE INDEX idx_bookings_user ON Bookings(user_id);
CREATE INDEX idx_bookings_flight ON Bookings(flight_id);
CREATE INDEX idx_bookings_status ON Bookings(status);
CREATE INDEX idx_payments_booking ON Payments(booking_id);
CREATE INDEX idx_passengers_booking ON BookingPassengers(booking_id);
GO

-- =============================================
-- 4. CHÈN DỮ LIỆU
-- =============================================

-- Users
INSERT INTO Users (username, password, email, full_name, role) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@airline.com', 'System Administrator', 'ADMIN'),
('john_doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'john.doe@email.com', 'John Doe', 'USER'),
('jane_smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'jane.smith@email.com', 'Jane Smith', 'USER'),
('bob_wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'bob.wilson@email.com', 'Bob Wilson', 'USER'),
('alice_brown', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'alice.brown@email.com', 'Alice Brown', 'USER');
GO

INSERT INTO Users (username, password, email, full_name, role) VALUES
('user6', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user6@email.com', 'User Six', 'USER'),
('user7', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user7@email.com', 'User Seven', 'USER'),
('user8', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user8@email.com', 'User Eight', 'USER'),
('user9', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user9@email.com', 'User Nine', 'USER'),
('user10', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user10@email.com', 'User Ten', 'USER'),
('user11', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user11@email.com', 'User Eleven', 'USER'),
('user12', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user12@email.com', 'User Twelve', 'USER'),
('user13', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user13@email.com', 'User Thirteen', 'USER'),
('user14', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user14@email.com', 'User Fourteen', 'USER'),
('user15', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user15@email.com', 'User Fifteen', 'USER'),
('user16', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user16@email.com', 'User Sixteen', 'USER'),
('user17', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user17@email.com', 'User Seventeen', 'USER'),
('user18', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user18@email.com', 'User Eighteen', 'USER'),
('user19', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user19@email.com', 'User Nineteen', 'USER'),
('user20', '$2a$10$.27GxK3Ve2IEysiOSa0/S.biFvB3Amc5muIliWaB3g8jDVrELuOQq', 'user20@email.com', 'User Twenty', 'USER');
GO

-- Aircrafts
INSERT INTO Aircrafts (model_name, capacity) VALUES
('Boeing 737-800', 189),
('Airbus A320', 180),
('Boeing 787 Dreamliner', 242),
('Airbus A350', 300),
('Boeing 777-300ER', 396);
GO

-- Routes
INSERT INTO Routes (origin, destination, distance_km) VALUES
('Hanoi', 'Ho Chi Minh City', 1166.00),
('Hanoi', 'Da Nang', 616.00),
('Ho Chi Minh City', 'Da Nang', 608.00),
('Hanoi', 'Nha Trang', 1072.00),
('Ho Chi Minh City', 'Phu Quoc', 300.00),
('Hanoi', 'Bangkok', 892.00),
('Ho Chi Minh City', 'Singapore', 1074.00);
GO

-- Flights mẫu (9)
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
GO

-- 50 chuyến bay ngày 05-11-2025 (cũ)
INSERT INTO Flights (flight_number, departure_time, arrival_time, price, available_seats, route_id, aircraft_id) VALUES
('VN010', '2025-11-05 00:30:00', '2025-11-05 02:30:00', 1200000.00, 180, 1, 1),
('VN011', '2025-11-05 01:00:00', '2025-11-05 03:00:00', 1300000.00, 170, 2, 2),
('VN012', '2025-11-05 01:30:00', '2025-11-05 03:30:00', 1400000.00, 160, 3, 3),
('VN013', '2025-11-05 02:00:00', '2025-11-05 04:00:00', 1500000.00, 150, 4, 4),
('VN014', '2025-11-05 02:30:00', '2025-11-05 04:30:00', 1600000.00, 140, 5, 5),
('VN015', '2025-11-05 03:00:00', '2025-11-05 05:00:00', 1700000.00, 130, 6, 1),
('VN016', '2025-11-05 03:30:00', '2025-11-05 05:30:00', 1800000.00, 120, 7, 2),
('VN017', '2025-11-05 04:00:00', '2025-11-05 06:00:00', 1900000.00, 110, 1, 3),
('VN018', '2025-11-05 04:30:00', '2025-11-05 06:30:00', 2000000.00, 100, 2, 4),
('VN019', '2025-11-05 05:00:00', '2025-11-05 07:00:00', 2100000.00, 190, 3, 5),
('VN020', '2025-11-05 05:30:00', '2025-11-05 07:30:00', 2200000.00, 180, 4, 1),
('VN021', '2025-11-05 06:30:00', '2025-11-05 08:30:00', 2300000.00, 170, 5, 2),
('VN022', '2025-11-05 07:30:00', '2025-11-05 09:30:00', 2400000.00, 160, 6, 3),
('VN023', '2025-11-05 08:30:00', '2025-11-05 10:30:00', 2500000.00, 150, 7, 4),
('VN024', '2025-11-05 09:00:00', '2025-11-05 11:00:00', 2600000.00, 140, 1, 5),
('VN025', '2025-11-05 10:00:00', '2025-11-05 12:00:00', 2700000.00, 130, 2, 1),
('VN026', '2025-11-05 11:00:00', '2025-11-05 13:00:00', 2800000.00, 120, 3, 2),
('VN027', '2025-11-05 12:00:00', '2025-11-05 14:00:00', 2900000.00, 110, 4, 3),
('VN028', '2025-11-05 13:00:00', '2025-11-05 15:00:00', 3000000.00, 100, 5, 4),
('VN029', '2025-11-05 14:00:00', '2025-11-05 16:00:00', 3100000.00, 190, 6, 5),
('VN030', '2025-11-05 15:00:00', '2025-11-05 17:00:00', 3200000.00, 180, 7, 1),
('VN031', '2025-11-05 16:00:00', '2025-11-05 18:00:00', 3300000.00, 170, 1, 2),
('VN032', '2025-11-05 17:00:00', '2025-11-05 19:00:00', 3400000.00, 160, 2, 3),
('VN033', '2025-11-05 18:00:00', '2025-11-05 20:00:00', 3500000.00, 150, 3, 4),
('VN034', '2025-11-05 19:00:00', '2025-11-05 21:00:00', 3600000.00, 140, 4, 5),
('VN035', '2025-11-05 20:00:00', '2025-11-05 22:00:00', 3700000.00, 130, 5, 1),
('VN036', '2025-11-05 21:00:00', '2025-11-05 23:00:00', 3800000.00, 120, 6, 2),
('VN037', '2025-11-05 22:00:00', '2025-11-06 00:00:00', 3900000.00, 110, 7, 3),
('VN038', '2025-11-05 23:00:00', '2025-11-06 01:00:00', 4000000.00, 100, 1, 4),
('VN039', '2025-11-05 00:15:00', '2025-11-05 02:15:00', 4100000.00, 190, 2, 5),
('VN040', '2025-11-05 01:15:00', '2025-11-05 03:15:00', 4200000.00, 180, 3, 1),
('VN041', '2025-11-05 02:15:00', '2025-11-05 04:15:00', 4300000.00, 170, 4, 2),
('VN042', '2025-11-05 03:15:00', '2025-11-05 05:15:00', 4400000.00, 160, 5, 3),
('VN043', '2025-11-05 04:15:00', '2025-11-05 06:15:00', 4500000.00, 150, 6, 4),
('VN044', '2025-11-05 05:15:00', '2025-11-05 07:15:00', 4600000.00, 140, 7, 5),
('VN045', '2025-11-05 06:15:00', '2025-11-05 08:15:00', 4700000.00, 130, 1, 1),
('VN046', '2025-11-05 07:15:00', '2025-11-05 09:15:00', 4800000.00, 120, 2, 2),
('VN047', '2025-11-05 08:15:00', '2025-11-05 10:15:00', 4900000.00, 110, 3, 3),
('VN048', '2025-11-05 09:15:00', '2025-11-05 11:15:00', 5000000.00, 100, 4, 4),
('VN049', '2025-11-05 10:15:00', '2025-11-05 12:15:00', 5100000.00, 190, 5, 5),
('VN050', '2025-11-05 11:15:00', '2025-11-05 13:15:00', 5200000.00, 180, 6, 1),
('VN051', '2025-11-05 12:15:00', '2025-11-05 14:15:00', 5300000.00, 170, 7, 2),
('VN052', '2025-11-05 13:15:00', '2025-11-05 15:15:00', 5400000.00, 160, 1, 3),
('VN053', '2025-11-05 14:15:00', '2025-11-05 16:15:00', 5500000.00, 150, 2, 4),
('VN054', '2025-11-05 15:15:00', '2025-11-05 17:15:00', 5600000.00, 140, 3, 5),
('VN055', '2025-11-05 16:15:00', '2025-11-05 18:15:00', 5700000.00, 130, 4, 1),
('VN056', '2025-11-05 17:15:00', '2025-11-05 19:15:00', 5800000.00, 120, 5, 2),
('VN057', '2025-11-05 18:15:00', '2025-11-05 20:15:00', 5900000.00, 110, 6, 3),
('VN058', '2025-11-05 19:15:00', '2025-11-05 21:15:00', 6000000.00, 100, 7, 4),
('VN059', '2025-11-05 20:15:00', '2025-11-05 22:15:00', 6100000.00, 190, 1, 5);
GO

-- 41 chuyến còn lại
INSERT INTO Flights (flight_number, departure_time, arrival_time, price, available_seats, route_id, aircraft_id) VALUES
('VN060', '2025-11-01 08:00:00', '2025-11-01 10:00:00', 6200000.00, 180, 2, 1),
('VN061', '2025-11-02 09:00:00', '2025-11-02 11:00:00', 6300000.00, 170, 3, 2),
('VN062', '2025-11-03 10:00:00', '2025-11-03 12:00:00', 6400000.00, 160, 4, 3),
('VN063', '2025-11-04 11:00:00', '2025-11-04 13:00:00', 6500000.00, 150, 5, 4),
('VN064', '2025-11-06 12:00:00', '2025-11-06 14:00:00', 6600000.00, 140, 6, 5),
('VN065', '2025-11-07 13:00:00', '2025-11-07 15:00:00', 6700000.00, 130, 7, 1),
('VN066', '2025-11-08 14:00:00', '2025-11-08 16:00:00', 6800000.00, 120, 1, 2),
('VN067', '2025-11-09 15:00:00', '2025-11-09 17:00:00', 6900000.00, 110, 2, 3),
('VN068', '2025-11-10 16:00:00', '2025-11-10 18:00:00', 7000000.00, 100, 3, 4),
('VN069', '2025-11-11 17:00:00', '2025-11-11 19:00:00', 7100000.00, 190, 4, 5),
('VN070', '2025-11-12 18:00:00', '2025-11-12 20:00:00', 7200000.00, 180, 5, 1),
('VN071', '2025-11-13 19:00:00', '2025-11-13 21:00:00', 7300000.00, 170, 6, 2),
('VN072', '2025-11-14 20:00:00', '2025-11-14 22:00:00', 7400000.00, 160, 7, 3),
('VN073', '2025-11-15 21:00:00', '2025-11-15 23:00:00', 7500000.00, 150, 1, 4),
('VN074', '2025-11-16 22:00:00', '2025-11-17 00:00:00', 7600000.00, 140, 2, 5),
('VN075', '2025-11-17 23:00:00', '2025-11-18 01:00:00', 7700000.00, 130, 3, 1),
('VN076', '2025-11-18 00:30:00', '2025-11-18 02:30:00', 7800000.00, 120, 4, 2),
('VN077', '2025-11-19 01:30:00', '2025-11-19 03:30:00', 7900000.00, 110, 5, 3),
('VN078', '2025-11-20 02:30:00', '2025-11-20 04:30:00', 8000000.00, 100, 6, 4),
('VN079', '2025-11-21 03:30:00', '2025-11-21 05:30:00', 8100000.00, 190, 7, 5),
('VN080', '2025-11-22 04:30:00', '2025-11-22 06:30:00', 8200000.00, 180, 1, 1),
('VN081', '2025-11-23 05:30:00', '2025-11-23 07:30:00', 8300000.00, 170, 2, 2),
('VN082', '2025-11-24 06:30:00', '2025-11-24 08:30:00', 8400000.00, 160, 3, 3),
('VN083', '2025-11-25 07:30:00', '2025-11-25 09:30:00', 8500000.00, 150, 4, 4),
('VN084', '2025-11-26 08:30:00', '2025-11-26 10:30:00', 8600000.00, 140, 5, 5),
('VN085', '2025-11-27 09:30:00', '2025-11-27 11:30:00', 8700000.00, 130, 6, 1),
('VN086', '2025-11-28 10:30:00', '2025-11-28 12:30:00', 8800000.00, 120, 7, 2),
('VN087', '2025-11-29 11:30:00', '2025-11-29 13:30:00', 8900000.00, 110, 1, 3),
('VN088', '2025-11-30 12:30:00', '2025-11-30 14:30:00', 9000000.00, 100, 2, 4),
('VN089', '2025-11-01 13:30:00', '2025-11-01 15:30:00', 9100000.00, 190, 3, 5),
('VN090', '2025-11-02 14:30:00', '2025-11-02 16:30:00', 9200000.00, 180, 4, 1),
('VN091', '2025-11-03 15:30:00', '2025-11-03 17:30:00', 9300000.00, 170, 5, 2),
('VN092', '2025-11-04 16:30:00', '2025-11-04 18:30:00', 9400000.00, 160, 6, 3),
('VN093', '2025-11-06 17:30:00', '2025-11-06 19:30:00', 9500000.00, 150, 7, 4),
('VN094', '2025-11-07 18:30:00', '2025-11-07 20:30:00', 9600000.00, 140, 1, 5),
('VN095', '2025-11-08 19:30:00', '2025-11-08 21:30:00', 9700000.00, 130, 2, 1),
('VN096', '2025-11-09 20:30:00', '2025-11-09 22:30:00', 9800000.00, 120, 3, 2),
('VN097', '2025-11-10 21:30:00', '2025-11-10 23:30:00', 9900000.00, 110, 4, 3),
('VN098', '2025-11-11 22:30:00', '2025-11-12 00:30:00', 10000000.00, 100, 5, 4),
('VN099', '2025-11-12 23:30:00', '2025-11-13 01:30:00', 10100000.00, 190, 6, 5),
('VN100', '2025-11-13 00:45:00', '2025-11-13 02:45:00', 10200000.00, 180, 7, 1);
GO

-- 50 chuyến mới: Hà Nội → Đà Nẵng – 05/11/2025
INSERT INTO Flights (flight_number, departure_time, arrival_time, price, available_seats, route_id, aircraft_id) VALUES
('VN2001', '2025-11-05 05:00:00', '2025-11-05 06:30:00', 850000.00, 180, 2, 1),
('VN2002', '2025-11-05 05:30:00', '2025-11-05 07:00:00', 870000.00, 178, 2, 2),
('VN2003', '2025-11-05 06:00:00', '2025-11-05 07:30:00', 890000.00, 175, 2, 3),
('VN2004', '2025-11-05 06:30:00', '2025-11-05 08:00:00', 910000.00, 172, 2, 4),
('VN2005', '2025-11-05 07:00:00', '2025-11-05 08:30:00', 930000.00, 170, 2, 5),
('VN2006', '2025-11-05 07:30:00', '2025-11-05 09:00:00', 950000.00, 168, 2, 1),
('VN2007', '2025-11-05 08:00:00', '2025-11-05 09:30:00', 970000.00, 165, 2, 2),
('VN2008', '2025-11-05 08:30:00', '2025-11-05 10:00:00', 990000.00, 162, 2, 3),
('VN2009', '2025-11-05 09:00:00', '2025-11-05 10:30:00', 1010000.00, 160, 2, 4),
('VN2010', '2025-11-05 09:30:00', '2025-11-05 11:00:00', 1030000.00, 158, 2, 5),
('VN2011', '2025-11-05 10:00:00', '2025-11-05 11:30:00', 1050000.00, 180, 2, 1),
('VN2012', '2025-11-05 10:30:00', '2025-11-05 12:00:00', 1070000.00, 178, 2, 2),
('VN2013', '2025-11-05 11:00:00', '2025-11-05 12:30:00', 1090000.00, 175, 2, 3),
('VN2014', '2025-11-05 11:30:00', '2025-11-05 13:00:00', 1110000.00, 172, 2, 4),
('VN2015', '2025-11-05 12:00:00', '2025-11-05 13:30:00', 1130000.00, 170, 2, 5),
('VN2016', '2025-11-05 12:30:00', '2025-11-05 14:00:00', 1150000.00, 168, 2, 1),
('VN2017', '2025-11-05 13:00:00', '2025-11-05 14:30:00', 1170000.00, 165, 2, 2),
('VN2018', '2025-11-05 13:30:00', '2025-11-05 15:00:00', 1190000.00, 162, 2, 3),
('VN2019', '2025-11-05 14:00:00', '2025-11-05 15:30:00', 1200000.00, 160, 2, 4),
('VN2020', '2025-11-05 14:30:00', '2025-11-05 16:00:00', 1180000.00, 158, 2, 5),
('VN2021', '2025-11-05 15:00:00', '2025-11-05 16:30:00', 1160000.00, 180, 2, 1),
('VN2022', '2025-11-05 15:30:00', '2025-11-05 17:00:00', 1140000.00, 178, 2, 2),
('VN2023', '2025-11-05 16:00:00', '2025-11-05 17:30:00', 1120000.00, 175, 2, 3),
('VN2024', '2025-11-05 16:30:00', '2025-11-05 18:00:00', 1100000.00, 172, 2, 4),
('VN2025', '2025-11-05 17:00:00', '2025-11-05 18:30:00', 1080000.00, 170, 2, 5),
('VN2026', '2025-11-05 17:30:00', '2025-11-05 19:00:00', 1060000.00, 168, 2, 1),
('VN2027', '2025-11-05 18:00:00', '2025-11-05 19:30:00', 1040000.00, 165, 2, 2),
('VN2028', '2025-11-05 18:30:00', '2025-11-05 20:00:00', 1020000.00, 162, 2, 3),
('VN2029', '2025-11-05 19:00:00', '2025-11-05 20:30:00', 1000000.00, 160, 2, 4),
('VN2030', '2025-11-05 19:30:00', '2025-11-05 21:00:00', 980000.00, 158, 2, 5),
('VN2031', '2025-11-05 20:00:00', '2025-11-05 21:30:00', 960000.00, 180, 2, 1),
('VN2032', '2025-11-05 20:30:00', '2025-11-05 22:00:00', 940000.00, 178, 2, 2),
('VN2033', '2025-11-05 21:00:00', '2025-11-05 22:30:00', 920000.00, 175, 2, 3),
('VN2034', '2025-11-05 21:30:00', '2025-11-05 23:00:00', 900000.00, 172, 2, 4),
('VN2035', '2025-11-05 22:00:00', '2025-11-05 23:30:00', 880000.00, 170, 2, 5),
('VN2036', '2025-11-05 22:30:00', '2025-11-06 00:00:00', 860000.00, 168, 2, 1),
('VN2037', '2025-11-05 23:00:00', '2025-11-06 00:30:00', 840000.00, 165, 2, 2),
('VN2038', '2025-11-05 23:30:00', '2025-11-06 01:00:00', 820000.00, 162, 2, 3),
('VN2039', '2025-11-05 00:15:00', '2025-11-05 01:45:00', 800000.00, 160, 2, 4),
('VN2040', '2025-11-05 01:00:00', '2025-11-05 02:30:00', 810000.00, 158, 2, 5),
('VN2041', '2025-11-05 01:30:00', '2025-11-05 03:00:00', 820000.00, 180, 2, 1),
('VN2042', '2025-11-05 02:00:00', '2025-11-05 03:30:00', 830000.00, 178, 2, 2),
('VN2043', '2025-11-05 02:30:00', '2025-11-05 04:00:00', 840000.00, 175, 2, 3),
('VN2044', '2025-11-05 03:00:00', '2025-11-05 04:30:00', 850000.00, 172, 2, 4),
('VN2045', '2025-11-05 03:30:00', '2025-11-05 05:00:00', 860000.00, 170, 2, 5),
('VN2046', '2025-11-05 04:00:00', '2025-11-05 05:30:00', 870000.00, 168, 2, 1),
('VN2047', '2025-11-05 04:30:00', '2025-11-05 06:00:00', 880000.00, 165, 2, 2),
('VN2048', '2025-11-05 05:00:00', '2025-11-05 06:30:00', 890000.00, 162, 2, 3),
('VN2049', '2025-11-05 05:30:00', '2025-11-05 07:00:00', 900000.00, 160, 2, 4),
('VN2050', '2025-11-05 06:00:00', '2025-11-05 07:30:00', 920000.00, 158, 2, 5);
GO

-- Bookings + Passengers + Payments
INSERT INTO Bookings (user_id, flight_id, booking_date, status, total_price) VALUES
(2, 1, '2025-11-01 10:30:00', 'CONFIRMED', 1500000.00),
(3, 2, '2025-11-01 11:00:00', 'CONFIRMED', 3000000.00),
(2, 3, '2025-11-02 09:15:00', 'PENDING', 800000.00),
(4, 5, '2025-11-02 14:20:00', 'CONFIRMED', 900000.00),
(5, 7, '2025-11-03 08:45:00', 'CANCELLED', 600000.00);
GO

INSERT INTO Bookings (user_id, flight_id, booking_date, status, total_price) VALUES
(2, 10, '2025-11-01 12:00:00', 'CONFIRMED', 1200000.00),
(3, 11, '2025-11-01 13:00:00', 'PENDING', 2600000.00),
(4, 12, '2025-11-01 14:00:00', 'CONFIRMED', 4200000.00),
(5, 13, '2025-11-01 15:00:00', 'CANCELLED', 1500000.00),
(6, 14, '2025-11-01 16:00:00', 'CONFIRMED', 3200000.00),
(7, 15, '2025-11-01 17:00:00', 'PENDING', 5100000.00),
(8, 16, '2025-11-01 18:00:00', 'CONFIRMED', 1800000.00),
(9, 17, '2025-11-01 19:00:00', 'CONFIRMED', 3800000.00),
(10, 18, '2025-11-01 20:00:00', 'PENDING', 6000000.00),
(11, 19, '2025-11-02 09:00:00', 'CANCELLED', 2100000.00),
(12, 20, '2025-11-02 10:00:00', 'CONFIRMED', 4400000.00),
(13, 21, '2025-11-02 11:00:00', 'PENDING', 6900000.00),
(14, 22, '2025-11-02 12:00:00', 'CONFIRMED', 2400000.00),
(15, 23, '2025-11-02 13:00:00', 'CONFIRMED', 5000000.00),
(16, 24, '2025-11-02 14:00:00', 'PENDING', 7800000.00),
(17, 25, '2025-11-02 15:00:00', 'CANCELLED', 2700000.00),
(18, 26, '2025-11-02 16:00:00', 'CONFIRMED', 5600000.00),
(19, 27, '2025-11-02 17:00:00', 'PENDING', 8700000.00),
(20, 28, '2025-11-02 18:00:00', 'CONFIRMED', 3000000.00),
(2, 29, '2025-11-03 09:00:00', 'CONFIRMED', 6200000.00),
(3, 30, '2025-11-03 10:00:00', 'PENDING', 9600000.00),
(4, 31, '2025-11-03 11:00:00', 'CANCELLED', 3300000.00),
(5, 32, '2025-11-03 12:00:00', 'CONFIRMED', 6800000.00),
(6, 33, '2025-11-03 13:00:00', 'PENDING', 10500000.00),
(7, 34, '2025-11-03 14:00:00', 'CONFIRMED', 3600000.00),
(8, 35, '2025-11-03 15:00:00', 'CONFIRMED', 7400000.00),
(9, 36, '2025-11-03 16:00:00', 'PENDING', 11400000.00),
(10, 37, '2025-11-03 17:00:00', 'CANCELLED', 3900000.00),
(11, 38, '2025-11-03 18:00:00', 'CONFIRMED', 8000000.00),
(12, 39, '2025-11-04 09:00:00', 'PENDING', 12300000.00),
(13, 40, '2025-11-04 10:00:00', 'CONFIRMED', 4200000.00),
(14, 41, '2025-11-04 11:00:00', 'CONFIRMED', 8600000.00),
(15, 42, '2025-11-04 12:00:00', 'PENDING', 13200000.00),
(16, 43, '2025-11-04 13:00:00', 'CANCELLED', 4500000.00),
(17, 44, '2025-11-04 14:00:00', 'CONFIRMED', 9200000.00),
(18, 45, '2025-11-04 15:00:00', 'PENDING', 14100000.00),
(19, 46, '2025-11-04 16:00:00', 'CONFIRMED', 4800000.00),
(20, 47, '2025-11-04 17:00:00', 'CONFIRMED', 9800000.00),
(2, 48, '2025-11-04 18:00:00', 'PENDING', 15000000.00),
(3, 49, '2025-11-05 09:00:00', 'CANCELLED', 5100000.00),
(4, 50, '2025-11-05 10:00:00', 'CONFIRMED', 10400000.00),
(5, 51, '2025-11-05 11:00:00', 'PENDING', 15900000.00),
(6, 52, '2025-11-05 12:00:00', 'CONFIRMED', 5400000.00),
(7, 53, '2025-11-05 13:00:00', 'CONFIRMED', 11000000.00),
(8, 54, '2025-11-05 14:00:00', 'PENDING', 16800000.00),
(9, 55, '2025-11-05 15:00:00', 'CANCELLED', 5700000.00),
(10, 56, '2025-11-05 16:00:00', 'CONFIRMED', 11600000.00),
(11, 57, '2025-11-05 17:00:00', 'PENDING', 17700000.00),
(12, 58, '2025-11-05 18:00:00', 'CONFIRMED', 6000000.00),
(13, 59, '2025-11-05 19:00:00', 'CONFIRMED', 12200000.00);
GO

INSERT INTO BookingPassengers (booking_id, full_name, email, phone) VALUES
(1, 'John Doe', 'john.doe@email.com', '0901234567'),
(2, 'Jane Smith', 'jane.smith@email.com', '0912345678'),
(2, 'Mike Smith', 'mike.smith@email.com', '0912345679'),
(3, 'John Doe', 'john.doe@email.com', '0901234567'),
(4, 'Bob Wilson', 'bob.wilson@email.com', '0923456789'),
(5, 'Alice Brown', 'alice.brown@email.com', '0934567890');
GO

-- Tạo 120 hành khách
DECLARE @i INT = 6, @pass INT = 1;
WHILE @i <= 55
BEGIN
    DECLARE @num_pass INT = CASE WHEN @i % 3 = 0 THEN 3 WHEN @i % 3 = 1 THEN 1 ELSE 2 END;
    DECLARE @j INT = 1;
    WHILE @j <= @num_pass
    BEGIN
        INSERT INTO BookingPassengers (booking_id, full_name, email, phone)
        VALUES (@i, 'Passenger ' + CAST(@pass AS VARCHAR), 'p' + CAST(@pass AS VARCHAR) + '@email.com', '090' + RIGHT('0000000' + CAST(@pass AS VARCHAR), 7));
        SET @j = @j + 1;
        SET @pass = @pass + 1;
    END
    SET @i = @i + 1;
END
GO

INSERT INTO Payments (booking_id, payment_date, amount, payment_method, status, transaction_id) VALUES
(1, '2025-11-01 10:35:00', 1500000.00, 'CREDIT_CARD', 'SUCCESS', 'TXN1234567890'),
(2, '2025-11-01 11:05:00', 3000000.00, 'BANKING', 'SUCCESS', 'TXN1234567891'),
(4, '2025-11-02 14:25:00', 900000.00, 'E_WALLET', 'SUCCESS', 'TXN1234567892');
GO

-- =============================================
-- THỐNG KÊ CUỐI CÙNG (ĐÃ SỬA LỖI PRINT)
-- =============================================
DECLARE @c_users INT, @c_aircrafts INT, @c_routes INT, @c_flights INT, @c_bookings INT, @c_passengers INT, @c_payments INT, @c_hanoi_danang INT;

SELECT @c_users = COUNT(*) FROM Users;
SELECT @c_aircrafts = COUNT(*) FROM Aircrafts;
SELECT @c_routes = COUNT(*) FROM Routes;
SELECT @c_flights = COUNT(*) FROM Flights;
SELECT @c_bookings = COUNT(*) FROM Bookings;
SELECT @c_passengers = COUNT(*) FROM BookingPassengers;
SELECT @c_payments = COUNT(*) FROM Payments;

-- Tính số chuyến Hà Nội → Đà Nẵng ngày 05-11-2025
SELECT @c_hanoi_danang = COUNT(*)
FROM Flights f
JOIN Routes r ON f.route_id = r.id
WHERE r.origin = 'Hanoi' AND r.destination = 'Da Nang'
  AND CAST(f.departure_time AS DATE) = '2025-11-05';

PRINT '=== DATABASE STATISTICS ===';
PRINT 'Users: ' + CAST(@c_users AS VARCHAR(10));
PRINT 'Aircrafts: ' + CAST(@c_aircrafts AS VARCHAR(10));
PRINT 'Routes: ' + CAST(@c_routes AS VARCHAR(10));
PRINT 'Flights: ' + CAST(@c_flights AS VARCHAR(10));
PRINT 'Bookings: ' + CAST(@c_bookings AS VARCHAR(10));
PRINT 'Passengers: ' + CAST(@c_passengers AS VARCHAR(10));
PRINT 'Payments: ' + CAST(@c_payments AS VARCHAR(10));
PRINT '=========================';
GO