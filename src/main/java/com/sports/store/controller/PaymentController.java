package com.sports.store.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.sports.store.dto.OrderResponse;
import com.sports.store.model.User;
import com.sports.store.service.OrderService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> requestData) {
        try {
            double amountInRupees = Double.parseDouble(requestData.get("amount").toString());
            int amountInPaise = (int) Math.round(amountInRupees * 100);

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0, 8));

            Order order = client.orders.create(options);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("receipt", order.get("receipt"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, Object> requestData,
            @AuthenticationPrincipal User user
    ) {
        try {
            String razorpayOrderId = (String) requestData.get("razorpayOrderId");
            String razorpayPaymentId = (String) requestData.get("razorpayPaymentId");
            String razorpaySignature = (String) requestData.get("razorpaySignature");
            double amount = Double.parseDouble(requestData.get("amount").toString());

            String signatureData = razorpayOrderId + "|" + razorpayPaymentId;
            String calculatedSignature = calculateHmacSha256(signatureData, keySecret);

            if (!calculatedSignature.equalsIgnoreCase(razorpaySignature)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Payment verification failed: Signature mismatch.");
                return ResponseEntity.badRequest().body(error);
            }

            OrderResponse orderResponse = orderService.placePaidOrder(
                    user,
                    razorpayOrderId,
                    razorpayPaymentId,
                    BigDecimal.valueOf(amount)
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment verified and order created successfully.");
            response.put("order", orderResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String calculateHmacSha256(String data, String secret) throws Exception {
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : rawHmac) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
