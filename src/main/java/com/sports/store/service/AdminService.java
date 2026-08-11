package com.sports.store.service;

import com.sports.store.dto.*;
import com.sports.store.dto.AdminDashboardResponse.TopProductInfo;
import com.sports.store.dto.AdminDashboardResponse.UserResponse;
import com.sports.store.dto.AnalyticsResponse.*;
import com.sports.store.model.*;
import com.sports.store.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductImageRepository productImageRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductImageRepository productImageRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productImageRepository = productImageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Dashboard Metrics calculation
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long totalProducts = productRepository.count();
        long totalCategories = categoryRepository.count();
        long totalCustomers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .count();

        List<Order> allOrders = orderRepository.findAll();
        List<Order> successOrders = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
                .collect(Collectors.toList());

        BigDecimal totalRevenue = successOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrdersCount = successOrders.size();

        long totalSales = successOrders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .mapToLong(OrderItem::getQuantity)
                .sum();

        // Recent Orders (last 10)
        List<OrderResponse> recentOrders = orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(10)
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        // Latest Customers (last 10)
        List<UserResponse> latestCustomers = userRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(u -> u.getRole() == Role.CUSTOMER)
                .limit(10)
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        // Top Selling Products calculation
        List<TopProductInfo> topSellingProducts = calculateTopSellingProducts(successOrders);

        return new AdminDashboardResponse(
                totalRevenue,
                totalOrdersCount,
                totalCustomers,
                totalProducts,
                totalCategories,
                totalSales,
                recentOrders,
                topSellingProducts,
                latestCustomers
        );
    }

    // 2. Business Analytics Calculation (Daily, Monthly, Yearly, Overall)
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        List<Order> successOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.SUCCESS)
                .collect(Collectors.toList());

        // A. Daily Metrics
        Map<String, List<Order>> dailyGroups = successOrders.stream()
                .collect(Collectors.groupingBy(o -> {
                    LocalDateTime dt = o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now();
                    return dt.toLocalDate().toString();
                }));

        List<DailyMetric> daily = dailyGroups.entrySet().stream()
                .map(entry -> {
                    String date = entry.getKey();
                    List<Order> orders = entry.getValue();
                    BigDecimal rev = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long itemsSold = orders.stream().flatMap(o -> o.getOrderItems().stream()).mapToLong(OrderItem::getQuantity).sum();
                    return new DailyMetric(date, rev, orders.size(), itemsSold);
                })
                .sorted(Comparator.comparing(DailyMetric::getDate))
                .collect(Collectors.toList());

        // B. Monthly Metrics
        Map<String, List<Order>> monthlyGroups = successOrders.stream()
                .collect(Collectors.groupingBy(o -> {
                    LocalDateTime dt = o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now();
                    return dt.getYear() + "-" + String.format("%02d", dt.getMonthValue());
                }));

        List<MonthlyMetric> monthly = monthlyGroups.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<Order> orders = entry.getValue();
                    BigDecimal rev = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long itemsSold = orders.stream().flatMap(o -> o.getOrderItems().stream()).mapToLong(OrderItem::getQuantity).sum();
                    return new MonthlyMetric(month, rev, orders.size(), itemsSold);
                })
                .sorted(Comparator.comparing(MonthlyMetric::getMonth))
                .collect(Collectors.toList());

        // C. Yearly Metrics
        Map<String, List<Order>> yearlyGroups = successOrders.stream()
                .collect(Collectors.groupingBy(o -> {
                    LocalDateTime dt = o.getCreatedAt() != null ? o.getCreatedAt() : LocalDateTime.now();
                    return String.valueOf(dt.getYear());
                }));

        List<YearlyMetric> yearly = yearlyGroups.entrySet().stream()
                .map(entry -> {
                    String year = entry.getKey();
                    List<Order> orders = entry.getValue();
                    BigDecimal rev = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long itemsSold = orders.stream().flatMap(o -> o.getOrderItems().stream()).mapToLong(OrderItem::getQuantity).sum();
                    return new YearlyMetric(year, rev, orders.size(), itemsSold);
                })
                .sorted(Comparator.comparing(YearlyMetric::getYear))
                .collect(Collectors.toList());

        // D. Overall Metrics
        BigDecimal lifetimeRevenue = successOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = successOrders.size();
        BigDecimal averageOrderValue = totalOrders > 0
                ? lifetimeRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<TopProductInfo> bestSellingProducts = calculateTopSellingProducts(successOrders);

        // Top Categories
        Map<Category, List<OrderItem>> categoryGroups = successOrders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(item -> item.getProduct().getCategory()));

        List<CategorySalesInfo> topCategories = categoryGroups.entrySet().stream()
                .map(entry -> {
                    Category cat = entry.getKey();
                    String catName = cat != null ? cat.getCategoryName() : "Uncategorized";
                    Integer catId = cat != null ? cat.getCategoryId() : null;
                    long sold = entry.getValue().stream().mapToLong(OrderItem::getQuantity).sum();
                    BigDecimal rev = entry.getValue().stream()
                            .map(item -> item.getTotalPrice())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CategorySalesInfo(catId, catName, sold, rev);
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .collect(Collectors.toList());

        // Top Customers
        Map<User, List<Order>> customerGroups = successOrders.stream()
                .collect(Collectors.groupingBy(Order::getUser));

        List<CustomerSalesInfo> topCustomers = customerGroups.entrySet().stream()
                .map(entry -> {
                    User customer = entry.getKey();
                    long ordersCount = entry.getValue().size();
                    BigDecimal totalSpent = entry.getValue().stream()
                            .map(Order::getTotalAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CustomerSalesInfo(
                            customer.getUserId(),
                            customer.getRealUsername(),
                            customer.getEmail(),
                            ordersCount,
                            totalSpent
                    );
                })
                .sorted((a, b) -> b.getTotalSpent().compareTo(a.getTotalSpent()))
                .limit(5)
                .collect(Collectors.toList());

        OverallMetric overall = new OverallMetric(
                lifetimeRevenue,
                totalOrders,
                averageOrderValue,
                bestSellingProducts,
                topCategories,
                topCustomers
        );

        return new AnalyticsResponse(daily, monthly, yearly, overall);
    }

    // Helper: calculate top-selling products
    private List<TopProductInfo> calculateTopSellingProducts(List<Order> successOrders) {
        Map<Product, Long> productQuantities = successOrders.stream()
                .flatMap(o -> o.getOrderItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getProduct, Collectors.summingLong(OrderItem::getQuantity)));

        return productQuantities.entrySet().stream()
                .map(entry -> {
                    Product p = entry.getKey();
                    long qty = entry.getValue();
                    BigDecimal rev = p.getPrice().multiply(BigDecimal.valueOf(qty));
                    return new TopProductInfo(p.getProductId(), p.getName(), qty, rev);
                })
                .sorted((a, b) -> Long.compare(b.getSoldQuantity(), a.getSoldQuantity()))
                .limit(5)
                .collect(Collectors.toList());
    }

    // 3. Product Catalog Management
    @Transactional
    public Product addProduct(ProductRequest request) {
        validateProductRequest(request, null);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + request.getCategoryId()));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setBrand(request.getBrand());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        product.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        product.setCreatedAt(LocalDate.now());
        product.setUpdatedAt(LocalDate.now());

        Product savedProduct = productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> imageEntities = request.getImages().stream()
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .map(url -> new ProductImage(savedProduct, url.trim()))
                    .collect(Collectors.toList());
            productImageRepository.saveAll(imageEntities);
            savedProduct.setImages(imageEntities);
        }

        return savedProduct;
    }

    @Transactional
    public Product editProduct(Integer productId, ProductRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        validateProductRequest(request, productId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);
        product.setBrand(request.getBrand());
        product.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        product.setFeatured(request.getFeatured() != null ? request.getFeatured() : false);
        product.setUpdatedAt(LocalDate.now());

        // Update Images: clear old ones and save new ones
        product.getImages().clear();
        productRepository.save(product);

        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<ProductImage> imageEntities = request.getImages().stream()
                    .filter(url -> url != null && !url.trim().isEmpty())
                    .map(url -> new ProductImage(product, url.trim()))
                    .collect(Collectors.toList());
            productImageRepository.saveAll(imageEntities);
            product.getImages().addAll(imageEntities);
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // Prevent deletion if connected to orders to prevent database constraint issues, soft delete it instead!
        boolean hasOrders = orderItemRepository.findAll().stream()
                .anyMatch(item -> item.getProduct().getProductId().equals(productId));

        if (hasOrders) {
            // Soft delete: set status to DEACTIVATED and hide it
            product.setStatus("DEACTIVATED");
            productRepository.save(product);
        } else {
            productRepository.delete(product);
        }
    }

    private void validateProductRequest(ProductRequest request, Integer editProductId) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product Name is required.");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product Price must be a positive number.");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException("Product Stock cannot be negative.");
        }
        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("Category selection is required.");
        }

        // Duplicate name validation
        boolean duplicate = productRepository.findAll().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(request.getName().trim()) 
                        && (editProductId == null || !p.getProductId().equals(editProductId)));
        if (duplicate) {
            throw new IllegalArgumentException("Product name already exists: " + request.getName());
        }
    }

    // 4. User Directory Management
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Integer userId, AdminUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        // Duplicate username check
        Optional<User> existingUsername = userRepository.findByUsername(request.getUsername().trim());
        if (existingUsername.isPresent() && !existingUsername.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        // Duplicate email check
        Optional<User> existingEmail = userRepository.findByEmail(request.getEmail().trim());
        if (existingEmail.isPresent() && !existingEmail.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPhone(request.getPhone());
        
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus().toUpperCase());
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    // Helper: Map User to DTO
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getRealUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    // Helper: Map Order to DTO
    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> {
                    String imageUrl = "";
                    if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                        imageUrl = item.getProduct().getImages().get(0).getImageUrl();
                    }
                    return new OrderItemResponse(
                            item.getProduct().getProductId(),
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getPricePerUnit(),
                            item.getTotalPrice(),
                            imageUrl
                    );
                })
                .collect(Collectors.toList());

        LocalDateTime createdAt = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();

        return new OrderResponse(
                order.getOrderId(),
                order.getUser().getUserId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                createdAt,
                itemResponses
        );
    }

    @Transactional
    public Category addCategory(String name) {
        boolean exists = categoryRepository.findAll().stream()
                .anyMatch(c -> c.getCategoryName().equalsIgnoreCase(name));
        if (exists) {
            throw new IllegalArgumentException("Category name already exists: " + name);
        }
        Category category = new Category();
        category.setCategoryName(name);
        return categoryRepository.save(category);
    }
}
