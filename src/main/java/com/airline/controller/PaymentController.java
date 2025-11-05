package com.airline.controller;

import com.airline.config.VNPayConfig; // <-- Import
import com.airline.entity.Booking;
// import com.airline.entity.Payment; // <-- Xóa import không dùng
import com.airline.service.BookingService;
import com.airline.service.PaymentService;
import com.airline.util.VNPayUtils; // <-- Import
import jakarta.servlet.http.HttpServletRequest; // <-- Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException; // <-- Import
import java.util.Enumeration; // <-- Import
import java.util.HashMap; // <-- Import
import java.util.Map; // <-- Import
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
            HttpServletRequest request, // <-- Thay đổi: Dùng HttpServletRequest để lấy all params
            @RequestParam(name = "bookingId") Long bookingId, // <-- Lấy bookingId (đã thêm ở RestController)
            Model model) {

        // --- BẮT ĐẦU LOGIC XÁC THỰC HASH TỪ PaymentReturnServlet ---
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");

        // Loại bỏ bookingId khỏi map hash (nếu nó tồn tại)
        // (bookingId là param nội bộ, không phải của VNPAY)
        fields.remove("bookingId");

        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        String signValue;
        try {
            signValue = VNPayUtils.hashAllFields(fields, VNPayConfig.vnp_HashSecret);
        } catch (UnsupportedEncodingException e) {
            model.addAttribute("message", "Lỗi xác thực chữ ký: " + e.getMessage());
            return "payment/failed";
        }

        // --- KẾT THÚC LOGIC XÁC THỰC HASH ---
        // Lấy thông tin booking
        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/";
        }
        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);

        // Bắt đầu kiểm tra kết quả
        if (signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = request.getParameter("vnp_ResponseCode");
            String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Lấy mã giao dịch

            // Check VNPAY response code (00 = success)
            if ("00".equals(vnp_ResponseCode)) {
                // Payment success
                // Cập nhật trạng thái booking (và có thể cả payment nếu cần)
                bookingService.confirmBooking(bookingId);

                // (Tùy chọn: tìm payment bằng vnp_TxnRef và cập nhật)
                paymentService.findByTransactionId(vnp_TxnRef).ifPresent(payment -> {
                    paymentService.processPayment(payment.getId(), vnp_TxnRef);
                });

                model.addAttribute("success", true);
                model.addAttribute("message", "Thanh toán thành công!");
                return "payment/success";
            } else {
                // Payment failed
                model.addAttribute("success", false);
                model.addAttribute("message", "Thanh toán thất bại. Mã lỗi VNPAY: " + vnp_ResponseCode);
                return "payment/failed";
            }
        } else {
            // Hash mismatch - Giao dịch không hợp lệ
            model.addAttribute("success", false);
            model.addAttribute("message", "Giao dịch không hợp lệ: Sai chữ ký xác thực.");
            return "payment/failed";
        }
    }// Thêm phương thức này vào PaymentController

    @GetMapping("/result/simulate")
    public String paymentResultSimulate(
            @RequestParam(name = "bookingId") Long bookingId,
            Model model) {

        // Lấy thông tin booking
        Optional<Booking> bookingOpt = bookingService.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return "redirect:/";
        }

        Booking booking = bookingOpt.get();
        model.addAttribute("booking", booking);

        // Vì đây là giả lập thành công, ta luôn trả về trang success
        model.addAttribute("success", true);
        model.addAttribute("message", "Thanh toán (giả lập) thành công!");
        return "payment/success"; // Tái sử dụng trang success.html
    }
    // (Phương thức này có thể dùng chung cho mọi trường hợp muốn hiển thị trang failed)

    @GetMapping("/show-failed")
    public String showFailedPage(Model model) {
        // model.containsAttribute("message") sẽ tự động kiểm tra
        // "flash attribute" được thêm ở bước 2
        if (!model.containsAttribute("message")) {
            model.addAttribute("message", "Giao dịch của bạn đã thất bại.");
        }
        return "payment/failed"; // Trả về view payment/failed.html
    }
}
