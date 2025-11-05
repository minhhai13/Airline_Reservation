package com.airline.controller.api;

import com.airline.dto.*;
import com.airline.entity.Aircraft;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.Route;
import com.airline.entity.User;
import com.airline.service.*;
import com.airline.dao.AircraftDAO;
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

    @Autowired
    private RouteService routeService; // <-- Đã thêm

    @Autowired
    private AircraftDAO aircraftDAO; // <-- Đã thêm

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
                .map(this::convertToFlightResponse) // Sửa để dùng DTO
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

    // === API ĐỂ TẠO FLIGHT (ĐÃ SỬA LỖI JSON LOOP) ===
    @PostMapping("/flights")
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight( // Trả về FlightResponse
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

    // Thêm vào src/main/java/com/airline/controller/api/AdminRestController.java
    // Thêm vào src/main/java/com/airline/controller/api/AdminRestController.java
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

            // === SAFETY CHECK (ĐIỂM QUAN TRỌNG NHẤT) ===
            // Tìm tất cả booking của flight này
            List<Booking> bookings = bookingService.findByFlightId(id);

            // Đếm số booking "đang hoạt động" (chưa bị huỷ)
            long activeBookings = bookings.stream()
                    .filter(b -> b.isPending() || b.isConfirmed())
                    .count();

            if (activeBookings > 0) {
                // Nếu có booking, trả về lỗi, không cho update
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(
                                "Không thể cập nhật. Chuyến bay này đang có " + activeBookings
                                + " booking (Pending hoặc Confirmed). Vui lòng huỷ các booking này trước."
                        ));
            }
            // === KẾT THÚC SAFETY CHECK ===

            // Nếu không có booking, tiến hành update như bình thường
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
// === API ĐỂ XÓA USER (MỚI) ===

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable(name = "id") Long id,
            HttpSession session) {

        if (!isAdmin(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Admin access required"));
        }

        try {
            // KIỂM TRA AN TOÀN: Không cho admin tự xóa mình
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
}
