package com.airline.controller;

import com.airline.dto.FlightSearchRequest;
import com.airline.entity.Flight;
import com.airline.service.FlightService;
import com.airline.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/flights")
public class FlightController {

    @Autowired(required = false)
    private FlightService flightService;

    @Autowired(required = false)
    private RouteService routeService;

    @GetMapping("/search")
    public String searchFlights(
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") int passengers,
            Model model) {

        List<Flight> flights = List.of();
        
        if (origin != null && destination != null && date != null && flightService != null) {
            flights = flightService.searchFlights(origin, destination, date);
        }

        model.addAttribute("flights", flights);
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        model.addAttribute("date", date);
        model.addAttribute("passengers", passengers);
        model.addAttribute("origins", routeService != null ? routeService.getAllOrigins() : List.of());
        model.addAttribute("destinations", routeService != null ? routeService.getAllDestinations() : List.of());

        return "flights/search";
    }

    @GetMapping("/{id}")
    public String flightDetail(@PathVariable Long id, Model model) {
        Flight flight = flightService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        
        model.addAttribute("flight", flight);
        return "flights/detail";
    }
}

