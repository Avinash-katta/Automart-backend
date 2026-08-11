package com.sports.store.controller;

import com.sports.store.dto.CartItemResponse;
import com.sports.store.dto.CartRequest;
import com.sports.store.model.User;
import com.sports.store.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user));
    }

    @PostMapping
    public ResponseEntity<?> addToCart(
            @Valid @RequestBody CartRequest request,
            @AuthenticationPrincipal User user
    ) {
        try {
            CartItemResponse response = cartService.addToCart(request, user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable("id") Integer cartId,
            @RequestParam("quantity") Integer quantity,
            @AuthenticationPrincipal User user
    ) {
        try {
            CartItemResponse response = cartService.updateQuantity(cartId, quantity, user);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable("id") Integer cartId,
            @AuthenticationPrincipal User user
    ) {
        try {
            cartService.removeFromCart(cartId, user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Item removed from cart");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | SecurityException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
