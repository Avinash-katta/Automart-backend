package com.sports.store.controller;

import com.sports.store.dto.OrderResponse;
import com.sports.store.model.User;
import com.sports.store.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> placeOrder(@AuthenticationPrincipal User user) {
        try {
            OrderResponse response = orderService.placeOrder(user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrderHistory(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable("id") String orderId,
            @AuthenticationPrincipal User user
    ) {
        try {
            OrderResponse response = orderService.getOrderDetails(orderId, user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
