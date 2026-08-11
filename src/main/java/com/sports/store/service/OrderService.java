package com.sports.store.service;

import com.sports.store.dto.OrderItemResponse;
import com.sports.store.dto.OrderResponse;
import com.sports.store.model.*;
import com.sports.store.repository.CartItemRepository;
import com.sports.store.repository.OrderItemRepository;
import com.sports.store.repository.OrderRepository;
import com.sports.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse placeOrder(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUserUserId(user.getUserId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot place order. Shopping cart is empty.");
        }

        // 1. Verify stock and calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName()
                        + ". Available stock: " + product.getStock());
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // 2. Create and save parent Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, user, totalAmount, OrderStatus.PENDING);
        order = orderRepository.save(order);

        // 3. Create order items, deduct stock, and save
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            savedOrderItems.add(orderItemRepository.save(orderItem));
        }

        order.setOrderItems(savedOrderItems);

        // 4. Clear User Cart
        cartItemRepository.deleteAll(cartItems);

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse placePaidOrder(User user, String razorpayOrderId, String razorpayPaymentId, java.math.BigDecimal totalAmount) {
        List<CartItem> cartItems = cartItemRepository.findByUserUserId(user.getUserId());
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot place order. Shopping cart is empty.");
        }

        // 1. Verify stock
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName()
                        + ". Available stock: " + product.getStock());
            }
        }

        // 2. Create and save parent Order with Razorpay order ID and SUCCESS status
        Order order = new Order(razorpayOrderId, user, totalAmount, OrderStatus.SUCCESS);
        order = orderRepository.save(order);

        // 3. Create order items, deduct stock, and save
        List<OrderItem> savedOrderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // Deduct stock
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // Create OrderItem
            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            savedOrderItems.add(orderItemRepository.save(orderItem));
        }

        order.setOrderItems(savedOrderItems);

        // 4. Clear User Cart
        cartItemRepository.deleteAll(cartItems);

        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrderHistory(User user) {
        return orderRepository.findByUserUserIdOrderByCreatedAtDesc(user.getUserId()).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderDetails(String orderId, User user) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Unauthorized access to order details");
        }

        return mapToOrderResponse(order);
    }

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
}
