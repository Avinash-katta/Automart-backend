package com.sports.store.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboardResponse {

    private BigDecimal totalRevenue;
    private long totalOrders;
    private long totalCustomers;
    private long totalProducts;
    private long totalCategories;
    private long totalSales;

    private List<OrderResponse> recentOrders;
    private List<TopProductInfo> topSellingProducts;
    private List<UserResponse> latestCustomers;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(BigDecimal totalRevenue, long totalOrders, long totalCustomers,
                                  long totalProducts, long totalCategories, long totalSales,
                                  List<OrderResponse> recentOrders, List<TopProductInfo> topSellingProducts,
                                  List<UserResponse> latestCustomers) {
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.totalProducts = totalProducts;
        this.totalCategories = totalCategories;
        this.totalSales = totalSales;
        this.recentOrders = recentOrders;
        this.topSellingProducts = topSellingProducts;
        this.latestCustomers = latestCustomers;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(long totalSales) {
        this.totalSales = totalSales;
    }

    public List<OrderResponse> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderResponse> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<TopProductInfo> getTopSellingProducts() {
        return topSellingProducts;
    }

    public void setTopSellingProducts(List<TopProductInfo> topSellingProducts) {
        this.topSellingProducts = topSellingProducts;
    }

    public List<UserResponse> getLatestCustomers() {
        return latestCustomers;
    }

    public void setLatestCustomers(List<UserResponse> latestCustomers) {
        this.latestCustomers = latestCustomers;
    }

    public static class TopProductInfo {
        private Integer productId;
        private String name;
        private long soldQuantity;
        private BigDecimal revenue;

        public TopProductInfo() {
        }

        public TopProductInfo(Integer productId, String name, long soldQuantity, BigDecimal revenue) {
            this.productId = productId;
            this.name = name;
            this.soldQuantity = soldQuantity;
            this.revenue = revenue;
        }

        public Integer getProductId() {
            return productId;
        }

        public void setProductId(Integer productId) {
            this.productId = productId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSoldQuantity() {
            return soldQuantity;
        }

        public void setSoldQuantity(long soldQuantity) {
            this.soldQuantity = soldQuantity;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }
    }

    public static class UserResponse {
        private Integer userId;
        private String username;
        private String email;
        private String phone;
        private String role;
        private String status;
        private LocalDate createdAt;

        public UserResponse() {
        }

        public UserResponse(Integer userId, String username, String email, String phone, String role, String status, LocalDate createdAt) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.status = status;
            this.createdAt = createdAt;
        }

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDate getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDate createdAt) {
            this.createdAt = createdAt;
        }
    }
}
