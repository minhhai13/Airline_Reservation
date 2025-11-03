package com.airline.controller.api;

import com.airline.dto.*;
import com.airline.entity.Booking;
import com.airline.entity.BookingPassenger;
import com.airline.entity.User;
import com.airline.service.BookingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Please login to book"));
        }

        try {
            List<BookingPassenger> passengers = request.getPassengers().stream()
                .map(p -> BookingPassenger.builder()
                    .fullName(p.getFullName())
                    .email(p.getEmail())
                    .phone(p.getPhone())
                    .seatNumber(p.getSeatNumber())
                    .build())
                .collect(Collectors.toList());

            Booking booking = bookingService.createBooking(
                user.getId(),
                request.getFlightId(),
                passengers
            );

            // Store booking in session for payment flow
            session.setAttribute("bookingId", booking.getId());

            BookingResponse response = mapToResponse(booking);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully", response));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        List<Booking> bookings = bookingService.findByUserId(user.getId());
        List<BookingResponse> response = bookings.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable(name = "id") Long id,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        return bookingService.findById(id)
            .map(booking -> {
                // Check if user owns this booking or is admin
                if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .<ApiResponse<BookingResponse>>body(ApiResponse.error("Access denied"));
                }
                return ResponseEntity.ok(ApiResponse.success(mapToResponse(booking)));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable(name = "id") Long id,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Not authenticated"));
        }

        try {
            Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            if (!booking.getUser().getId().equals(user.getId()) && !user.isAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
            }

            Booking cancelled = bookingService.cancelBooking(id);
            return ResponseEntity.ok(
                ApiResponse.success("Booking cancelled", mapToResponse(cancelled))
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Helper method
    private BookingResponse mapToResponse(Booking booking) {
        FlightResponse flightResponse = FlightResponse.builder()
            .id(booking.getFlight().getId())
            .flightNumber(booking.getFlight().getFlightNumber())
            .departureTime(booking.getFlight().getDepartureTime())
            .arrivalTime(booking.getFlight().getArrivalTime())
            .price(booking.getFlight().getPrice())
            .build();

        List<PassengerInfo> passengers = booking.getPassengers().stream()
            .map(p -> new PassengerInfo(p.getFullName(), p.getEmail(), p.getPhone(), p.getSeatNumber()))
            .collect(Collectors.toList());

        return BookingResponse.builder()
            .id(booking.getId())
            .bookingDate(booking.getBookingDate())
            .status(booking.getStatus().name())
            .totalPrice(booking.getTotalPrice())
            .flight(flightResponse)
            .passengers(passengers)
            .build();
    }
}

