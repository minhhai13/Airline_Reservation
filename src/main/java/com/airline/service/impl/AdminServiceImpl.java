// ========================================
// IMPLEMENTATION
// ========================================
package com.airline.service.impl;

import com.airline.dao.*;
import com.airline.entity.*;
import com.airline.service.AdminService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 * AdminServiceImpl Implements admin-specific business operations
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserDAO userDAO;
    private final FlightDAO flightDAO;
    private final BookingDAO bookingDAO;
    private final PaymentDAO paymentDAO;
    private final AircraftDAO aircraftDAO;
    private final RouteDAO routeDAO;

    @Autowired
    public AdminServiceImpl(UserDAO userDAO,
            FlightDAO flightDAO,
            BookingDAO bookingDAO,
            PaymentDAO paymentDAO,
            AircraftDAO aircraftDAO,
            RouteDAO routeDAO) {
        this.userDAO = userDAO;
        this.flightDAO = flightDAO;
        this.bookingDAO = bookingDAO;
        this.paymentDAO = paymentDAO;
        this.aircraftDAO = aircraftDAO;
        this.routeDAO = routeDAO;
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Map<String, Object> getDashboardStatistics() {
//        Map<String, Object> stats = new HashMap<>();
//
//        // Count statistics
//        stats.put("totalUsers", userDAO.findAll().size());
//        stats.put("totalFlights", flightDAO.findAll().size());
//        stats.put("totalBookings", bookingDAO.findAll().size());
//
//        // Booking status breakdown
//        long confirmedBookings = bookingDAO.findByStatus(Booking.BookingStatus.CONFIRMED).size();
//        long pendingBookings = bookingDAO.findByStatus(Booking.BookingStatus.PENDING).size();
//        long cancelledBookings = bookingDAO.findByStatus(Booking.BookingStatus.CANCELLED).size();
//
//        stats.put("confirmedBookings", confirmedBookings);
//        stats.put("pendingBookings", pendingBookings);
//        stats.put("cancelledBookings", cancelledBookings);
//
//        // Revenue calculation
//        BigDecimal totalRevenue = bookingDAO.findByStatus(Booking.BookingStatus.CONFIRMED)
//                .stream()
//                .map(Booking::getTotalPrice)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        stats.put("totalRevenue", totalRevenue);
//
//        // Payment statistics
//        long successfulPayments = paymentDAO.findAll().stream()
//                .filter(Payment::isSuccess)
//                .count();
//        stats.put("successfulPayments", successfulPayments);
//
//        return stats;
//    }
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count statistics
        stats.put("totalUsers", (long) userDAO.findAll().size()); // <-- CHUYỂN SANG LONG
        stats.put("totalFlights", (long) flightDAO.findAll().size()); // <-- CHUYỂN SANG LONG
        stats.put("totalBookings", (long) bookingDAO.findAll().size()); // <-- CHUYỂN SANG LONG

        // Booking status breakdown
        long confirmedBookings = bookingDAO.findByStatus(Booking.BookingStatus.CONFIRMED).size();
        long pendingBookings = bookingDAO.findByStatus(Booking.BookingStatus.PENDING).size();
        long cancelledBookings = bookingDAO.findByStatus(Booking.BookingStatus.CANCELLED).size();

        stats.put("confirmedBookings", confirmedBookings);
        stats.put("pendingBookings", pendingBookings);
        stats.put("cancelledBookings", cancelledBookings);

        // Revenue calculation
        BigDecimal totalRevenue = bookingDAO.findByStatus(Booking.BookingStatus.CONFIRMED)
                .stream()
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalRevenue", totalRevenue);

        // Payment statistics
        long successfulPayments = paymentDAO.findAll().stream()
                .filter(Payment::isSuccess)
                .count();
        stats.put("successfulPayments", successfulPayments);

        // === THÊM TOP LISTS MỚI ===
        
        // 1. Top 10 Users by Bookings
        List<Object[]> topUsersData = userDAO.findTopUsersByBookingCount(10);
        List<Map<String, Object>> topUsers = topUsersData.stream()
            .map(arr -> {
                User user = (User) arr[0];
                Long count = (Long) arr[1];
                Map<String, Object> userStat = new HashMap<>();
                userStat.put("user", user);
                userStat.put("bookingCount", count);
                return userStat;
            })
            .collect(Collectors.toList());
        stats.put("topUsers", topUsers);


        // 2. Top 10 Flights by Bookings
        List<Object[]> topFlightsData = flightDAO.findTopFlightsByBookingCount(10);
        List<Map<String, Object>> topFlights = topFlightsData.stream()
            .map(arr -> {
                Flight flight = (Flight) arr[0];
                Long count = (Long) arr[1];
                Map<String, Object> flightStat = new HashMap<>();
                flightStat.put("flight", flight);
                flightStat.put("bookingCount", count);
                return flightStat;
            })
            .collect(Collectors.toList());
        stats.put("topFlights", topFlights);
        
        // ============================

        return stats;
    }

    // ========================================
    // Aircraft Management
    // ========================================
    @Override
    public Aircraft createAircraft(Aircraft aircraft) {
        return aircraftDAO.save(aircraft);
    }

    @Override
    public Aircraft updateAircraft(Aircraft aircraft) {
        Aircraft existing = aircraftDAO.findById(aircraft.getId())
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));
        return aircraftDAO.save(aircraft);
    }

    @Override
    public void deleteAircraft(Long id) {
        Aircraft aircraft = aircraftDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));
        aircraftDAO.delete(aircraft);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Aircraft> findAllAircrafts() {
        return aircraftDAO.findAll();
    }

    // ========================================
    // Route Management
    // ========================================
    @Override
    public Route createRoute(Route route) {
        // Check if route already exists
        if (routeDAO.findByOriginAndDestination(route.getOrigin(), route.getDestination()).isPresent()) {
            throw new IllegalArgumentException("Route already exists");
        }
        return routeDAO.save(route);
    }

    @Override
    public Route updateRoute(Route route) {
        Route existing = routeDAO.findById(route.getId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        return routeDAO.save(route);
    }

    @Override
    public void deleteRoute(Long id) {
        Route route = routeDAO.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));
        routeDAO.delete(route);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Route> findAllRoutes() {
        return routeDAO.findAll();
    }
    
    
}
