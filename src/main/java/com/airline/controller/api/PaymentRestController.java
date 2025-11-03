package com.airline.controller.api;

import com.airline.dto.ApiResponse;
import com.airline.dto.PaymentRequest;
import com.airline.entity.Payment;
import com.airline.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpSession session) {
        
        try {
            Payment payment = paymentService.createPayment(
                request.getBookingId(),
                request.getAmount(),
                request.getPaymentMethod()
            );

            // In real implementation, this would redirect to VNPAY
            // For now, simulate payment URL
            String vnpayUrl = generateVNPayUrl(payment.getId(), request.getBookingId());

            Map<String, Object> data = new HashMap<>();
            data.put("paymentId", payment.getId());
            data.put("paymentUrl", vnpayUrl);
            data.put("amount", payment.getAmount());

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment created", data));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    // Simulate VNPAY URL generation
    private String generateVNPayUrl(Long paymentId, Long bookingId) {
        String transactionId = UUID.randomUUID().toString();
        return String.format("/payment/result?vnp_ResponseCode=00&bookingId=%d&transactionId=%s",
            bookingId, transactionId);
    }
}

