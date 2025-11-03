package com.airline.controller;

import com.airline.entity.Booking;
import com.airline.entity.User;
import com.airline.service.BookingService;
import com.airline.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private RecommendationService recommendationService;

    // My Flights Page
    @GetMapping("/my-flights")
    public String myFlights(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/user/my-flights";
        }

        List<Booking> bookings = bookingService.findByUserId(user.getId());
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("user", user);

        return "user/my-flights";
    }

    // Profile Page
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/user/profile";
        }

        model.addAttribute("user", user);
        return "user/profile";
    }

    // Recommendations Page
    @GetMapping("/recommendations")
    public String recommendations(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/user/recommendations";
        }

        var recommended = recommendationService.getRecommendedFlights(user.getId(), 5);
        
        model.addAttribute("flights", recommended);
        model.addAttribute("user", user);

        return "user/recommendations";
    }
}

