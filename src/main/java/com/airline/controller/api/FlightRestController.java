package com.airline.controller.api;

import com.airline.dto.ApiResponse;
import com.airline.dto.FlightResponse;
import com.airline.dto.RouteInfo;
import com.airline.entity.Flight;
import com.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
public class FlightRestController {

    @Autowired
    private FlightService flightService;

    /**
     * API search flights with pagination Dùng cho AJAX calls từ search.html
     *
     * @param origin
     * @param destination
     * @param date
     * @param page
     * @param size
     * @return
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchFlights(
            @RequestParam("origin") String origin,
            @RequestParam("destination") String destination,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        try {
            Map<String, Object> result = flightService.searchFlightsWithPaging(origin, destination, date, page, size);

            // Convert Flight entities to FlightResponse DTOs
            @SuppressWarnings("unchecked")
            List<Flight> flights = (List<Flight>) result.get("flights");

            List<FlightResponse> flightResponses = flights.stream()
                    .map(this::convertToFlightResponse)
                    .collect(Collectors.toList());

            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("flights", flightResponses);
            responseData.put("currentPage", result.get("currentPage"));
            responseData.put("totalPages", result.get("totalPages"));
            responseData.put("totalFlights", result.get("totalFlights"));
            responseData.put("pageSize", result.get("pageSize"));

            return ResponseEntity.ok(ApiResponse.success("Flights loaded successfully", responseData));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error searching flights: " + e.getMessage()));
        }
    }

    /**
     * Convert Flight entity to FlightResponse DTO
     */
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
                .build();
    }
}
