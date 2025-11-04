package com.airline.controller.api;

import com.airline.config.VNPayConfig; // <-- Import mới
import com.airline.dto.ApiResponse;
import com.airline.dto.PaymentRequest;
import com.airline.entity.Booking; // <-- Import mới
import com.airline.entity.Payment;
import com.airline.service.BookingService; // <-- Import mới
import com.airline.service.PaymentService;
import com.airline.service.impl.PaymentServiceImpl;
import com.airline.util.VNPayUtils; // <-- Import mới
import jakarta.servlet.http.HttpServletRequest; // <-- Import mới
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException; // <-- Import mới
import java.net.URLEncoder; // <-- Import mới
import java.nio.charset.StandardCharsets; // <-- Import mới
import java.text.SimpleDateFormat; // <-- Import mới
import java.util.*; // <-- Import mới

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService; // <-- Thêm service

    // === THAY THẾ HOÀN TOÀN PHƯƠNG THỨC NÀY ===
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPayment(
            @Valid @RequestBody PaymentRequest request,
            HttpServletRequest httpServletRequest) { // <-- Thêm HttpServletRequest

        try {
            // Lấy thông tin Booking
            Booking booking = bookingService.findById(request.getBookingId())
                    .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

            // 1. Tạo vnp_TxnRef (Mã đơn hàng)
            String vnp_TxnRef = VNPayConfig.getRandomNumber(8);

            // 2. Tạo Payment (PENDING) và LƯU vnp_TxnRef vào DB
            // (Sửa lại service impl để chấp nhận txnRef)
            Payment payment = paymentService.createPayment(
                    request.getBookingId(),
                    request.getAmount(),
                    request.getPaymentMethod(),
                    vnp_TxnRef
            );

            // 3. Lấy IP Address
            String vnp_IpAddr = httpServletRequest.getRemoteAddr();

            // 4. Tạo Map tham số VNPAY
            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(booking.getTotalPrice().longValue() * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + booking.getId());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // 5. Tạo Query string và Hash
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data (dùng US_ASCII theo VNPayUtils)
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));

                    // Build query string (dùng UTF-8)
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    hashData.append('&');
                    query.append('&');
                }
            }

            // Xóa dấu & cuối
            if (query.length() > 0) {
                query.deleteCharAt(query.length() - 1);
            }
            if (hashData.length() > 0) {
                hashData.deleteCharAt(hashData.length() - 1);
            }

            // 6. Tạo vnp_SecureHash
            // (Chúng ta dùng hashData đã build, thay vì gọi hashAllFields để đảm bảo
            // logic y hệt CreatePaymentServlet, dù US_ASCII mới là đúng)
            // String vnp_SecureHash = VNPayUtils.hmacSHA512(VNPayConfig.vnp_HashSecret, hashData.toString());
            // CẬP NHẬT: Dùng VNPayUtils.hashAllFields cho chuẩn
            String vnp_SecureHash = VNPayUtils.hashAllFields(vnp_Params, VNPayConfig.vnp_HashSecret);

            // 7. Tạo URL thanh toán
            String paymentUrl = VNPayConfig.vnp_Url + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;

            Map<String, Object> data = new HashMap<>();
            data.put("paymentId", payment.getId());
            data.put("paymentUrl", paymentUrl); // <-- Trả về URL thật
            data.put("amount", payment.getAmount());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payment URL created", data));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
