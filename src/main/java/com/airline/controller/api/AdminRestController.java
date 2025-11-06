package com.airline.controller.api;

import com.airline.dao.AircraftDAO;
import com.airline.dto.*;
import com.airline.entity.Aircraft;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.Route;
import com.airline.entity.User;
import com.airline.entity.User.UserRole;
import com.airline.service.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    // === QUY TẮC: MASTER ADMIN ID (KHÔNG THỂ THAO TÁC) ===
    private static final Long MASTER_ADMIN_ID = 1L;

    @Autowired
    private UserService userService;

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private AircraftDAO aircraftDAO;

    // Check admin authorization
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }

    // Get all flights (admin)
    @GetMapping("/flights")
    public ResponseEntity<ApiResponse<List<FlightResponse>>> getAllFlights(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin access required"));
        }

        List<FlightResponse> flights = flightService.findAll().stream()
                .map(this::convertToFlightResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    // Delete flight
    @DeleteMapping("/flights/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFlight(
            @PathVariable(name = "id") Long id,
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
            @PathVariable(name = "id") Long id,
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

    // === API ĐỂ TẠO FLIGHT ===
    @PostMapping("/flights")
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
            @RequestBody FlightRequest request,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin access required"));
        }

        try {
            Route route = routeService.findById(request.getRouteId())
                    .orElseThrow(() -> new IllegalArgumentException("Route not found"));

            Aircraft aircraft = aircraftDAO.findById(request.getAircraftId())
                    .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));

            Flight flight = Flight.builder()
                    .flightNumber(request.getFlightNumber())
                    .departureTime(request.getDepartureTime())
                    .arrivalTime(request.getArrivalTime())
                    .price(request.getPrice())
                    .availableSeats(request.getAvailableSeats())
                    .route(route)
                    .aircraft(aircraft)
                    .build();

            Flight savedFlight = flightService.createFlight(flight);

            // Chuyển đổi Entity sang DTO trước khi trả về
            FlightResponse responseDto = convertToFlightResponse(savedFlight);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Flight created successfully", responseDto));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // API CẬP NHẬT FLIGHT
    @PutMapping("/flights/{id}")
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @PathVariable(name = "id") Long id,
            @RequestBody FlightRequest request,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin access required"));
        }

        try {
            Flight existingFlight = flightService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

            // === SAFETY CHECK ===
            List<Booking> bookings = bookingService.findByFlightId(id);
            long activeBookings = bookings.stream()
                    .filter(b -> b.isPending() || b.isConfirmed())
                    .count();

            if (activeBookings > 0) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "Không thể cập nhật. Chuyến bay này đang có " + activeBookings
                                + " booking (Pending hoặc Confirmed). Vui lòng huỷ các booking này trước."
                        ));
            }
            // === KẾT THÚC SAFETY CHECK ===

            Route route = routeService.findById(request.getRouteId())
                    .orElseThrow(() -> new IllegalArgumentException("Route not found"));

            Aircraft aircraft = aircraftDAO.findById(request.getAircraftId())
                    .orElseThrow(() -> new IllegalArgumentException("Aircraft not found"));

            // Cập nhật thông tin
            existingFlight.setFlightNumber(request.getFlightNumber());
            existingFlight.setDepartureTime(request.getDepartureTime());
            existingFlight.setArrivalTime(request.getArrivalTime());
            existingFlight.setPrice(request.getPrice());
            existingFlight.setAvailableSeats(request.getAvailableSeats());
            existingFlight.setRoute(route);
            existingFlight.setAircraft(aircraft);

            Flight updatedFlight = flightService.updateFlight(existingFlight);
            FlightResponse responseDto = convertToFlightResponse(updatedFlight);

            return ResponseEntity.ok()
                    .body(ApiResponse.success("Flight updated successfully", responseDto));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
// === API ĐỂ XÓA USER (ĐÃ SỬA: THÊM MASTER ADMIN CHECK) ===

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable(name = "id") Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin access required"));
        }

        try {
            // KIỂM TRA AN TOÀN 1: KHÔNG CHO XÓA MASTER ADMIN
            if (id.equals(MASTER_ADMIN_ID)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot delete the Master Admin account."));
            }

            // KIỂM TRA AN TOÀN 2: Không cho admin tự xóa mình
            User adminUser = (User) session.getAttribute("user");
            if (adminUser.getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot delete your own account while logged in."));
            }

            // Gọi service để xóa
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));

        } catch (Exception e) {
            // Bắt lỗi nếu user có khóa ngoại (ví dụ: đã đặt booking)
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Could not delete user. They may have existing bookings."));
        }
    }

    // === HÀM HELPER ĐỂ CHUYỂN ĐỔI SANG DTO ===
    private FlightResponse convertToFlightResponse(Flight flight) {
        RouteInfo routeInfo = RouteInfo.builder()
                .id(flight.getRoute().getId())
                .origin(flight.getRoute().getOrigin())
                .destination(flight.getRoute().getDestination())
                .distanceKm(flight.getRoute().getDistanceKm())
                .build();

        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .price(flight.getPrice())
                .availableSeats(flight.getAvailableSeats())
                .route(routeInfo)
                .aircraftModel(flight.getAircraft().getModelName())
                .aircraftId(flight.getAircraft().getId())
                .build();
    }

    // === API ĐỂ CẬP NHẬT ROLE (ĐÃ SỬA: THÊM MASTER ADMIN CHECK) ===
    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable(name = "id") Long id,
            @RequestParam("role") String role,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin access required"));
        }

        try {
            // KIỂM TRA AN TOÀN 1: KHÔNG CHO CẬP NHẬT ROLE CỦA MASTER ADMIN
            if (id.equals(MASTER_ADMIN_ID)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot change the role of the Master Admin account."));
            }

            // KIỂM TRA AN TOÀN 2: Không cho admin tự thay đổi role của mình
            User adminUser = (User) session.getAttribute("user");
            if (adminUser.getId().equals(id)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot change your own role."));
            }

            UserRole newRole = UserRole.valueOf(role.toUpperCase());
            userService.updateUserRole(id, newRole);

            return ResponseEntity.ok(ApiResponse.success("User role updated successfully", null));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid user ID or role: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating user role: " + e.getMessage()));
        }
    }
}
