package com.sports.store.controller;

import com.sports.store.dto.*;
import com.sports.store.dto.AdminDashboardResponse.UserResponse;
import com.sports.store.dto.AnalyticsResponse.DailyMetric;
import com.sports.store.dto.AnalyticsResponse.MonthlyMetric;
import com.sports.store.dto.AnalyticsResponse.OverallMetric;
import com.sports.store.dto.AnalyticsResponse.YearlyMetric;
import com.sports.store.model.Product;
import com.sports.store.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Integer userId,
            @Valid @RequestBody AdminUserRequest request
    ) {
        try {
            UserResponse response = adminService.updateUser(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@Valid @RequestBody ProductRequest request) {
        try {
            Product product = adminService.addProduct(request);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> editProduct(
            @PathVariable("id") Integer productId,
            @Valid @RequestBody ProductRequest request
    ) {
        try {
            Product product = adminService.editProduct(productId, request);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Integer productId) {
        try {
            adminService.deleteProduct(productId);
            Map<String, String> success = new HashMap<>();
            success.put("message", "Product deleted successfully");
            return ResponseEntity.ok(success);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrders());
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable("id") String orderId,
            @RequestParam("status") String status
    ) {
        try {
            OrderResponse response = adminService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Analytics Group Endpoints
    @GetMapping("/revenue/daily")
    public ResponseEntity<List<DailyMetric>> getDailyRevenue() {
        return ResponseEntity.ok(adminService.getAnalytics().getDaily());
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<MonthlyMetric>> getMonthlyRevenue() {
        return ResponseEntity.ok(adminService.getAnalytics().getMonthly());
    }

    @GetMapping("/revenue/yearly")
    public ResponseEntity<List<YearlyMetric>> getYearlyRevenue() {
        return ResponseEntity.ok(adminService.getAnalytics().getYearly());
    }

    @GetMapping("/revenue/overall")
    public ResponseEntity<OverallMetric> getOverallRevenue() {
        return ResponseEntity.ok(adminService.getAnalytics().getOverall());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> addCategory(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name is required.");
            }
            com.sports.store.model.Category category = adminService.addCategory(name.trim());
            return ResponseEntity.ok(category);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
