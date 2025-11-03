package com.airline.controller.api;

import com.airline.dto.*;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    // Check admin authorization
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }

    // Dashboard Stats
    @GetMapping("/reports/summary")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Admin access required"));
        }

        List<User> users = userService.findAllUsers();
        List<Flight> flights = flightService.findAll();
        List<Booking> bookings = bookingService.findAll();

        long pending = bookings.stream().filter(Booking::isPending).count();
        long confirmed = bookings.stream().filter(Booking::isConfirmed).count();
        long cancelled = bookings.stream().filter(Booking::isCancelled).count();

        BigDecimal revenue = bookings.stream()
            .filter(Booking::isConfirmed)
            .map(Booking::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardStats stats = DashboardStats.builder()
            .totalUsers((long) users.size())
            .totalFlights((long) flights.size())
            .totalBookings((long) bookings.size())
            .pendingBookings(pending)
            .confirmedBookings(confirmed)
            .cancelledBookings(cancelled)
            .totalRevenue(revenue)
            .build();

        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Admin access required"));
        }

        List<UserResponse> users = userService.findAllUsers().stream()
            .map(u -> UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .role(u.getRole().name())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    // Get all flights (admin)
    @GetMapping("/flights")
    public ResponseEntity<ApiResponse<List<FlightResponse>>> getAllFlights(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Admin access required"));
        }

        List<FlightResponse> flights = flightService.findAll().stream()
            .map(f -> FlightResponse.builder()
                .id(f.getId())
                .flightNumber(f.getFlightNumber())
                .departureTime(f.getDepartureTime())
                .arrivalTime(f.getArrivalTime())
                .price(f.getPrice())
                .availableSeats(f.getAvailableSeats())
                .aircraftModel(f.getAircraft().getModelName())
                .build())
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    // Delete flight
    @DeleteMapping("/flights/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(
            @PathVariable Long id,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Admin access required"));
        }

        try {
            flightService.deleteFlight(id);
            return ResponseEntity.ok(ApiResponse.success("Flight deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Cancel booking (admin)
    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            HttpSession session) {
        
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Admin access required"));
        }

        try {
            bookingService.cancelBooking(id);
            return ResponseEntity.ok(ApiResponse.success("Booking cancelled", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}

