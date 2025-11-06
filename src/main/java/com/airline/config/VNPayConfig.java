package com.airline.config;

public class VNPayConfig {

    public static String vnp_TmnCode = "800L34J2";
    public static String vnp_HashSecret = "LIH4VEKXUWE99G9XZBP0BOYIF7X1U1M4";
    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // === SỬA LỖI: THÊM /airline TRỞ LẠI ===
    // URL này phải khớp với URL "Forwarding" mà ngrok trả về
    public static String vnp_ReturnUrl = "https://broodless-michiko-evolutionarily.ngrok-free.dev/airline/payment/result";

    public static String getRandomNumber(int len) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
