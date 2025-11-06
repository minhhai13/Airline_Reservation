package com.airline.controller;

import com.airline.dao.AircraftDAO;
import com.airline.dto.DashboardStats;
import com.airline.entity.Aircraft;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.Route;
import com.airline.entity.User;
import com.airline.service.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

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

    @Autowired
    private AdminService adminService;

    // Check admin role
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }

    // PHƯƠNG THỨC ĐÃ SỬA: Dùng /users/{userId} để tránh xung đột với các route khác dưới /admin/
    @GetMapping("/users/{userId}")
    public String viewUser(@PathVariable(name = "userId") Long userId,
            Model model) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        return "admin/profile";
    }

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Map<String, Object> allStats = adminService.getDashboardStatistics();

        DashboardStats stats = DashboardStats.builder()
                .totalUsers((Long) allStats.get("totalUsers"))
                .totalFlights((Long) allStats.get("totalFlights"))
                .totalBookings((Long) allStats.get("totalBookings"))
                .pendingBookings((Long) allStats.get("pendingBookings"))
                .confirmedBookings((Long) allStats.get("confirmedBookings"))
                .cancelledBookings((Long) allStats.get("cancelledBookings"))
                .totalRevenue((BigDecimal) allStats.get("totalRevenue"))
                .build();

        model.addAttribute("stats", stats);
        model.addAttribute("topUsers", allStats.get("topUsers"));
        model.addAttribute("topFlights", allStats.get("topFlights"));

        return "admin/dashboard";
    }

    // Manage Flights
    @GetMapping("/flights")
    public String manageFlights(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Flight> flights = flightService.findAll();
        List<Route> routes = routeService.findAll();
        List<Aircraft> aircrafts = aircraftDAO.findAll();

        model.addAttribute("flights", flights);
        model.addAttribute("routes", routes);
        model.addAttribute("aircrafts", aircrafts);

        return "admin/flights";
    }

    // Manage Users
    @GetMapping("/users")
    public String manageUsers(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // Manage Bookings
    @GetMapping("/bookings")
    public String manageBookings(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Booking> bookings = bookingService.findAll();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

}
