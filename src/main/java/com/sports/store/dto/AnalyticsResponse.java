package com.sports.store.dto;

import java.math.BigDecimal;
import java.util.List;

public class AnalyticsResponse {

    private List<DailyMetric> daily;
    private List<MonthlyMetric> monthly;
    private List<YearlyMetric> yearly;
    private OverallMetric overall;

    public AnalyticsResponse() {
    }

    public AnalyticsResponse(List<DailyMetric> daily, List<MonthlyMetric> monthly, List<YearlyMetric> yearly, OverallMetric overall) {
        this.daily = daily;
        this.monthly = monthly;
        this.yearly = yearly;
        this.overall = overall;
    }

    public List<DailyMetric> getDaily() {
        return daily;
    }

    public void setDaily(List<DailyMetric> daily) {
        this.daily = daily;
    }

    public List<MonthlyMetric> getMonthly() {
        return monthly;
    }

    public void setMonthly(List<MonthlyMetric> monthly) {
        this.monthly = monthly;
    }

    public List<YearlyMetric> getYearly() {
        return yearly;
    }

    public void setYearly(List<YearlyMetric> yearly) {
        this.yearly = yearly;
    }

    public OverallMetric getOverall() {
        return overall;
    }

    public void setOverall(OverallMetric overall) {
        this.overall = overall;
    }

    public static class DailyMetric {
        private String date; // YYYY-MM-DD
        private BigDecimal revenue;
        private long ordersCount;
        private long itemsSold;

        public DailyMetric() {
        }

        public DailyMetric(String date, BigDecimal revenue, long ordersCount, long itemsSold) {
            this.date = date;
            this.revenue = revenue;
            this.ordersCount = ordersCount;
            this.itemsSold = itemsSold;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public long getOrdersCount() {
            return ordersCount;
        }

        public void setOrdersCount(long ordersCount) {
            this.ordersCount = ordersCount;
        }

        public long getItemsSold() {
            return itemsSold;
        }

        public void setItemsSold(long itemsSold) {
            this.itemsSold = itemsSold;
        }
    }

    public static class MonthlyMetric {
        private String month; // YYYY-MM
        private BigDecimal revenue;
        private long ordersCount;
        private long itemsSold;

        public MonthlyMetric() {
        }

        public MonthlyMetric(String month, BigDecimal revenue, long ordersCount, long itemsSold) {
            this.month = month;
            this.revenue = revenue;
            this.ordersCount = ordersCount;
            this.itemsSold = itemsSold;
        }

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public long getOrdersCount() {
            return ordersCount;
        }

        public void setOrdersCount(long ordersCount) {
            this.ordersCount = ordersCount;
        }

        public long getItemsSold() {
            return itemsSold;
        }

        public void setItemsSold(long itemsSold) {
            this.itemsSold = itemsSold;
        }
    }

    public static class YearlyMetric {
        private String year; // YYYY
        private BigDecimal revenue;
        private long ordersCount;
        private long itemsSold;

        public YearlyMetric() {
        }

        public YearlyMetric(String year, BigDecimal revenue, long ordersCount, long itemsSold) {
            this.year = year;
            this.revenue = revenue;
            this.ordersCount = ordersCount;
            this.itemsSold = itemsSold;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public BigDecimal getRevenue() {
            return revenue;
        }

        public void setRevenue(BigDecimal revenue) {
            this.revenue = revenue;
        }

        public long getOrdersCount() {
            return ordersCount;
        }

        public void setOrdersCount(long ordersCount) {
            this.ordersCount = ordersCount;
        }

        public long getItemsSold() {
            return itemsSold;
        }

        public void setItemsSold(long itemsSold) {
            this.itemsSold = itemsSold;
        }
    }

    public static class OverallMetric {
        private BigDecimal lifetimeRevenue;
        private long totalOrders;
        private BigDecimal averageOrderValue;
        private List<AdminDashboardResponse.TopProductInfo> bestSellingProducts;
        private List<CategorySalesInfo> topCategories;
        private List<CustomerSalesInfo> topCustomers;

        public OverallMetric() {
        }

        public OverallMetric(BigDecimal lifetimeRevenue, long totalOrders, BigDecimal averageOrderValue,
                             List<AdminDashboardResponse.TopProductInfo> bestSellingProducts,
                             List<CategorySalesInfo> topCategories, List<CustomerSalesInfo> topCustomers) {
            this.lifetimeRevenue = lifetimeRevenue;
            this.totalOrders = totalOrders;
            this.averageOrderValue = averageOrderValue;
            this.bestSellingProducts = bestSellingProducts;
            this.topCategories = topCategories;
            this.topCustomers = topCustomers;
        }

        public BigDecimal getLifetimeRevenue() {
            return lifetimeRevenue;
        }

        public void setLifetimeRevenue(BigDecimal lifetimeRevenue) {
            this.lifetimeRevenue = lifetimeRevenue;
        }

        public long getTotalOrders() {
            return totalOrders;
        }

        public void setTotalOrders(long totalOrders) {
            this.totalOrders = totalOrders;
        }

        public BigDecimal getAverageOrderValue() {
            return averageOrderValue;
        }

        public void setAverageOrderValue(BigDecimal averageOrderValue) {
            this.averageOrderValue = averageOrderValue;
        }

        public List<AdminDashboardResponse.TopProductInfo> getBestSellingProducts() {
            return bestSellingProducts;
        }

        public void setBestSellingProducts(List<AdminDashboardResponse.TopProductInfo> bestSellingProducts) {
            this.bestSellingProducts = bestSellingProducts;
        }

        public List<CategorySalesInfo> getTopCategories() {
            return topCategories;
        }

        public void setTopCategories(List<CategorySalesInfo> topCategories) {
            this.topCategories = topCategories;
        }

        public List<CustomerSalesInfo> getTopCustomers() {
            return topCustomers;
        }

        public void setTopCustomers(List<CustomerSalesInfo> topCustomers) {
            this.topCustomers = topCustomers;
        }
    }

    public static class CategorySalesInfo {
        private Integer categoryId;
        private String name;
        private long soldQuantity;
        private BigDecimal revenue;

        public CategorySalesInfo() {
        }

        public CategorySalesInfo(Integer categoryId, String name, long soldQuantity, BigDecimal revenue) {
            this.categoryId = categoryId;
            this.name = name;
            this.soldQuantity = soldQuantity;
            this.revenue = revenue;
        }

        public Integer getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Integer categoryId) {
            this.categoryId = categoryId;
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

    public static class CustomerSalesInfo {
        private Integer userId;
        private String username;
        private String email;
        private long ordersCount;
        private BigDecimal totalSpent;

        public CustomerSalesInfo() {
        }

        public CustomerSalesInfo(Integer userId, String username, String email, long ordersCount, BigDecimal totalSpent) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.ordersCount = ordersCount;
            this.totalSpent = totalSpent;
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

        public long getOrdersCount() {
            return ordersCount;
        }

        public void setOrdersCount(long ordersCount) {
            this.ordersCount = ordersCount;
        }

        public BigDecimal getTotalSpent() {
            return totalSpent;
        }

        public void setTotalSpent(BigDecimal totalSpent) {
            this.totalSpent = totalSpent;
        }
    }
}
