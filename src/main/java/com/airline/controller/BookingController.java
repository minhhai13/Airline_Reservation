package com.airline.controller;

import com.airline.dto.BookingRequest;
import com.airline.dto.PassengerInfo;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.User;
import com.airline.service.BookingService;
import com.airline.service.FlightService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private FlightService flightService;

    @Autowired
    private BookingService bookingService;

    // Booking Form Page
    @GetMapping("/{flightId}")
    public String bookingForm(@PathVariable(name = "flightId") Long flightId,
                             @RequestParam(name = "passengers", defaultValue = "1") int passengers,
                             HttpSession session,
                             Model model) {
        
        Flight flight = flightService.findById(flightId)
            .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        if (!flight.hasAvailableSeats(passengers)) {
            throw new IllegalStateException("Not enough available seats");
        }

        // Pre-fill with user info if logged in
        User user = (User) session.getAttribute("user");
        List<PassengerInfo> passengerList = new ArrayList<>();
        
        for (int i = 0; i < passengers; i++) {
            PassengerInfo p = new PassengerInfo();
            if (i == 0 && user != null) {
                p.setFullName(user.getFullName());
                p.setEmail(user.getEmail());
            }
            passengerList.add(p);
        }

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setFlightId(flightId);
        bookingRequest.setPassengers(passengerList);

        model.addAttribute("flight", flight);
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("passengerCount", passengers);

        return "booking/form";
    }

    // Booking Confirmation Page (requires login)
    @GetMapping("/confirm")
    public String confirmationPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/booking/confirm";
        }

        BookingRequest bookingRequest = (BookingRequest) session.getAttribute("bookingRequest");
        if (bookingRequest == null) {
            return "redirect:/";
        }

        Flight flight = flightService.findById(bookingRequest.getFlightId())
            .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        model.addAttribute("flight", flight);
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("user", user);

        return "booking/confirm";
    }
}

