package com.airline.controller.api;

import com.airline.dto.ApiResponse;
import com.airline.dto.FlightResponse;
import com.airline.dto.RouteInfo;
import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationRestController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlightResponse>>> getRecommendations(
            @RequestParam(defaultValue = "5") int limit,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Please login to see recommendations"));
        }

        List<Flight> flights = recommendationService.getRecommendedFlights(user.getId(), limit);
        
        List<FlightResponse> response = flights.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

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

