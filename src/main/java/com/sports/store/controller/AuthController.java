package com.sports.store.controller;

import com.sports.store.dto.*;
import com.sports.store.service.AuthService;
import com.sports.store.service.LogoutService;
import com.sports.store.model.User;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final LogoutService logoutService;

    public AuthController(AuthService authService, LogoutService logoutService) {
        this.authService = authService;
        this.logoutService = logoutService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            String message = authService.register(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.login(request);
            
            // Set authToken cookie with the JWT token value, scoped to localhost
            ResponseCookie authTokenCookie = ResponseCookie.from("authToken", loginResponse.getToken())
                    .domain("localhost")
                    .path("/")
                    .maxAge(86400) // 1 day
                    .httpOnly(true)
                    .secure(false) // HTTP on localhost
                    .sameSite("Lax")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, authTokenCookie.toString());

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                    authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                logoutService.logout(user);
            }

            // Scopes authToken cookie value to "null" but keeps maxAge active so it is NOT deleted from cookies
            ResponseCookie cleanAuthTokenCookie = ResponseCookie.from("authToken", "null")
                    .domain("localhost")
                    .path("/")
                    .maxAge(86400) // Keep cookie active for 1 day, just value becomes "null"
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .build();

            // Set standard token cookie to null and keep maxAge active
            ResponseCookie cleanTokenCookie = ResponseCookie.from("token", "null")
                    .domain("localhost")
                    .path("/")
                    .maxAge(86400)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cleanAuthTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, cleanTokenCookie.toString());

            // Clear SecurityContext
            SecurityContextHolder.clearContext();

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Logout successful");
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            Map<String, String> failureResponse = new HashMap<>();
            failureResponse.put("message", "Logout failed");
            return ResponseEntity.status(500).body(failureResponse);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            String message = authService.forgotPassword(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String message = authService.resetPassword(request);
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
