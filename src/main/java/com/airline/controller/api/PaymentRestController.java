package com.airline.controller.api;

import com.airline.config.VNPayConfig;
import com.airline.dto.ApiResponse;
import com.airline.dto.PaymentRequest;
import com.airline.entity.Booking;
import com.airline.entity.Payment;
import com.airline.service.BookingService;
import com.airline.service.PaymentService;
// import com.airline.service.impl.PaymentServiceImpl; // <-- Xóa import không dùng
import com.airline.util.VNPayUtils;
import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpSession; // <-- Xóa import không dùng
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException; // <-- Import
import java.net.URLEncoder; // <-- Import
import java.nio.charset.StandardCharsets; // <-- Import
import java.text.SimpleDateFormat; // <-- Import
import java.util.*; // <-- Import

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

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
            Payment payment = paymentService.createPayment(
                    booking.getId(), // Lấy từ booking entity
                    booking.getTotalPrice(), // Lấy từ booking entity
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
            // Lấy tổng tiền từ Booking (đã được xác thực) * 100
            vnp_Params.put("vnp_Amount", String.valueOf(booking.getTotalPrice().longValue() * 100));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang " + booking.getId());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");

            // === THAY ĐỔI QUAN TRỌNG: Thêm bookingId vào vnp_ReturnUrl ===
            // Điều này là cần thiết để PaymentController có thể xác định
            // booking nào cần cập nhật khi VNPAY gọi về.
            String returnUrl = VNPayConfig.vnp_ReturnUrl + "?bookingId=" + booking.getId();
            vnp_Params.put("vnp_ReturnUrl", returnUrl);

            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            // 5. Tạo Query string và Hash (LOGIC TỪ CreatePaymentServlet)
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    // Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    // Sử dụng US_ASCII theo VNPayUtils để hash
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    // Build query string
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));

                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            // 6. Tạo vnp_SecureHash
            // Sử dụng VNPayUtils (đã có trong project airline)
            String vnp_SecureHash = VNPayUtils.hashAllFields(vnp_Params, VNPayConfig.vnp_HashSecret);
            vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

            // 7. Tạo URL thanh toán
            // String paymentUrl = VNPayConfig.vnp_Url + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
            // CẬP NHẬT: query đã bao gồm hash, chỉ cần thêm vào URL
            query.append("&vnp_SecureHash=" + URLEncoder.encode(vnp_SecureHash, StandardCharsets.UTF_8.toString()));
            String paymentUrl = VNPayConfig.vnp_Url + "?" + query.toString();

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
