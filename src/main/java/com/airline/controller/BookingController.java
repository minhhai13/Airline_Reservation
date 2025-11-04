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
import org.springframework.web.bind.annotation.*; // <-- Phải có import này
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

    // Bước 1: Hiển thị form
    @GetMapping("/{flightId}")
    public String bookingForm(@PathVariable(name = "flightId") Long flightId,
            @RequestParam(name = "passengers", defaultValue = "1") int passengers,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            String redirectUrl = String.format("/booking/%d?passengers=%d", flightId, passengers);
            return "redirect:/login?redirect=" + redirectUrl;
        }

        Flight flight = flightService.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        if (!flight.hasAvailableSeats(passengers)) {
            throw new IllegalStateException("Not enough available seats");
        }

        List<PassengerInfo> passengerList = new ArrayList<>();

        for (int i = 0; i < passengers; i++) {
            PassengerInfo p = new PassengerInfo();
            if (i == 0) {
                p.setFullName(user.getFullName());
                p.setEmail(user.getEmail());
            }
            passengerList.add(p);
        }

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setFlightId(flightId);
        bookingRequest.setPassengers(passengerList);

        // *** CỰC KỲ QUAN TRỌNG: Thêm "bookingRequest" vào model
        // để th:object trong form có thể đọc được
        model.addAttribute("bookingRequest", bookingRequest);

        model.addAttribute("flight", flight);
        model.addAttribute("passengerCount", passengers);

        return "booking/form";
    }

    // ==================================================
    // === PHƯƠNG THỨC MỚI ĐỂ NHẬN SUBMIT TỪ FORM ===
    // ==================================================
    /**
     * Bước 2: Nhận dữ liệu POST từ form Lưu vào session và Redirect
     */
    @PostMapping("/submit")
    public String submitBookingForm(
            @ModelAttribute("bookingRequest") BookingRequest bookingRequest, // Spring tự động bind data từ form
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/";
        }

        // *** Đây là mấu chốt: Lưu vào SESSION CỦA SERVER
        session.setAttribute("bookingRequest", bookingRequest);

        // Chuyển hướng đến trang confirm (GET)
        return "redirect:/booking/confirm";
    }
    // ==================================================

    // Bước 3: Hiển thị trang confirm
    @GetMapping("/confirm")
    public String confirmationPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirect=/booking/confirm";
        }

        // Lấy data từ SESSION CỦA SERVER (đã được lưu ở Bước 2)
        BookingRequest bookingRequest = (BookingRequest) session.getAttribute("bookingRequest");

        // Nếu không có (vì người dùng F5, hoặc vào trực tiếp) -> Về trang chủ
        if (bookingRequest == null) {
            return "redirect:/";
        }

        Flight flight = flightService.findById(bookingRequest.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));

        model.addAttribute("flight", flight);
        model.addAttribute("bookingRequest", bookingRequest);
        model.addAttribute("user", user);

        return "booking/confirm"; // Hiển thị trang
    }
}
