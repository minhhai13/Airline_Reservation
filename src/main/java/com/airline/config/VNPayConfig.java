package com.airline.config;

public class VNPayConfig {

    //on ngrok command: ngrok http 9999
    public static String vnp_TmnCode = "AZ1164IF";
    public static String vnp_HashSecret = "GAWUBU3BV7LL78F5S6F3WV7Q2BZIMNN2";
    public static String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//    public static String vnp_ReturnUrl = "http://localhost:9999/Payment/return";
    public static String vnp_ReturnUrl = "https://pseudorheumatic-leslie-noncaustic.ngrok-free.dev/airline/payment/result";

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
