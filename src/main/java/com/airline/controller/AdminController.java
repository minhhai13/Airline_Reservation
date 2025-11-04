package com.airline.controller;

import com.airline.dto.DashboardStats;
import com.airline.entity.Aircraft;
import com.airline.entity.Booking;
import com.airline.entity.Flight;
import com.airline.entity.Route;
import com.airline.entity.User;
import com.airline.service.*;
import com.airline.dao.AircraftDAO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

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
    private RouteService routeService; // <-- Đã thêm

    @Autowired
    private AircraftDAO aircraftDAO; // <-- Đã thêm

    // Check admin role
    private boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return user != null && user.isAdmin();
    }

    @GetMapping("/{userId}")
    public String bookingForm(@PathVariable(name = "userId") Long userId,
            Model model) {
        User user = userService.findById(userId).get();
        model.addAttribute("user", user);
        return "admin/profile";
    }

    // Admin Dashboard
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> allUsers = userService.findAllUsers();
        List<Flight> allFlights = flightService.findAll();
        List<Booking> allBookings = bookingService.findAll();

        long pendingCount = allBookings.stream().filter(Booking::isPending).count();
        long confirmedCount = allBookings.stream().filter(Booking::isConfirmed).count();
        long cancelledCount = allBookings.stream().filter(Booking::isCancelled).count();

        BigDecimal totalRevenue = allBookings.stream()
                .filter(Booking::isConfirmed)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        DashboardStats stats = DashboardStats.builder()
                .totalUsers((long) allUsers.size())
                .totalFlights((long) allFlights.size())
                .totalBookings((long) allBookings.size())
                .pendingBookings(pendingCount)
                .confirmedBookings(confirmedCount)
                .cancelledBookings(cancelledCount)
                .totalRevenue(totalRevenue)
                .build();

        model.addAttribute("stats", stats);
        return "admin/dashboard";
    }

    // Manage Flights
    @GetMapping("/flights")
    public String manageFlights(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Flight> flights = flightService.findAll();
        List<Route> routes = routeService.findAll(); // <-- Đã thêm
        List<Aircraft> aircrafts = aircraftDAO.findAll(); // <-- Đã thêm

        model.addAttribute("flights", flights);
        model.addAttribute("routes", routes); // <-- Đã thêm
        model.addAttribute("aircrafts", aircrafts); // <-- Đã thêm

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
