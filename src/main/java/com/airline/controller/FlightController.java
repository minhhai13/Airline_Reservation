package com.airline.controller;

import com.airline.service.FlightService;
import com.airline.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private RouteService routeService;

    /**
     * Search flights - First load (từ index.html) Trả về 10 flights đầu tiên
     * (page 0)
     *
     * @param origin
     * @param destination
     * @param date
     * @param passengers
     * @param model
     * @return
     */
    @GetMapping("/search")
    public String searchFlights(
            @RequestParam(name = "origin", required = false) String origin,
            @RequestParam(name = "destination", required = false) String destination,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "passengers", defaultValue = "1") int passengers,
            Model model) {
        // Load origins và destinations cho dropdown
        model.addAttribute("origins", routeService.getAllOrigins());
        model.addAttribute("destinations", routeService.getAllDestinations());

        // Giữ lại search criteria
        model.addAttribute("origin", origin);
        model.addAttribute("destination", destination);
        model.addAttribute("date", date);
        model.addAttribute("passengers", passengers);

        // Nếu có đủ thông tin tìm kiếm
        if (origin != null && destination != null && date != null) {
            Map<String, Object> result = flightService.searchFlightsWithPaging(origin, destination, date, 0, 10);

            model.addAttribute("flights", result.get("flights"));
            model.addAttribute("currentPage", result.get("currentPage"));
            model.addAttribute("totalPages", result.get("totalPages"));
            model.addAttribute("totalFlights", result.get("totalFlights"));
        } else {
            // Chưa có search criteria
            model.addAttribute("flights", List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalFlights", 0L);
        }

        return "flights/search";
    }

    @GetMapping("/{id}")
    public String flightDetail(@PathVariable("id") Long id, Model model) {
        flightService.findById(id).ifPresentOrElse(
                flight -> model.addAttribute("flight", flight),
                () -> {
                    throw new IllegalArgumentException("Flight not found");
                }
        );
        return "flights/detail";
    }
}
