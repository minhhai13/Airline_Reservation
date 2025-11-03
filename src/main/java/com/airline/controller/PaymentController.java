package com.airline.controller;

import com.airline.entity.Booking;
import com.airline.entity.Payment;
import com.airline.service.BookingService;
import com.airline.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    // Payment result callback from VNPAY
    @GetMapping("/result")
    public String paymentResult(
            @RequestParam(required = false) String vnp_ResponseCode,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) String transactionId,
            Model model) {

        if (bookingId == null) {
            return "redirect:/";
        }

        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/";
        }

        Booking booking = bookingOpt.get();

        // Check VNPAY response code (00 = success)
        if ("00".equals(vnp_ResponseCode)) {
            // Payment success
            bookingService.confirmBooking(bookingId);
            model.addAttribute("success", true);
            model.addAttribute("booking", booking);
            model.addAttribute("message", "Payment successful!");
            return "payment/success";
        } else {
            // Payment failed
            model.addAttribute("success", false);
            model.addAttribute("booking", booking);
            model.addAttribute("message", "Payment failed. Please try again.");
            return "payment/failed";
        }
    }
}

