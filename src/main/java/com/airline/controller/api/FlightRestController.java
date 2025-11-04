package com.airline.controller.api;

import com.airline.dto.ApiResponse;
import com.airline.entity.Flight;
import com.airline.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightRestController {

    @Autowired
    private FlightService flightService;

    @GetMapping
    public ResponseEntity<?> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<Flight> flights = flightService.searchFlights(origin, destination, date);

        // Giả lập phân trang thủ công (vì chưa dùng Pageable)
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, flights.size());
        List<Flight> paged = flights.subList(Math.min(fromIndex, flights.size()), toIndex);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Flights loaded", paged)
        );
    }
}
