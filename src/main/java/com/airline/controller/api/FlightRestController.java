package com.airline.controller.api;

import com.airline.dto.*;
import com.airline.entity.Flight;
import com.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
public class FlightRestController {

    @Autowired
    private FlightService flightService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlightResponse>>> searchFlights(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<Flight> flights;
        
        if (origin != null && destination != null && date != null) {
            flights = flightService.searchFlights(origin, destination, date);
        } else {
            flights = flightService.findAvailableFlights();
        }

        List<FlightResponse> response = flights.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(@PathVariable Long id) {
        return flightService.findById(id)
            .map(flight -> ResponseEntity.ok(ApiResponse.success(mapToResponse(flight))))
            .orElse(ResponseEntity.notFound().build());
    }

    // Helper method to map Flight entity to FlightResponse DTO
    private FlightResponse mapToResponse(Flight flight) {
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

