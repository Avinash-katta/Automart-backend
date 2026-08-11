package com.sports.store.service;

import com.sports.store.dto.CartItemResponse;
import com.sports.store.dto.CartRequest;
import com.sports.store.model.CartItem;
import com.sports.store.model.Product;
import com.sports.store.model.User;
import com.sports.store.repository.CartItemRepository;
import com.sports.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartItemResponse> getCart(User user) {
        return cartItemRepository.findByUserUserId(user.getUserId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartItemResponse addToCart(CartRequest request, User user) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock available. Remaining stock: " + product.getStock());
        }

        CartItem cartItem = cartItemRepository.findByUserUserIdAndProductProductId(user.getUserId(), product.getProductId())
                .orElse(new CartItem(user, product, 0));

        int newQuantity = cartItem.getQuantity() + request.getQuantity();
        if (product.getStock() < newQuantity) {
            throw new IllegalArgumentException("Cannot add requested quantity. Total cart quantity exceeds stock. Remaining stock: " + product.getStock());
        }

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);

        return mapToResponse(cartItem);
    }

    @Transactional
    public CartItemResponse updateQuantity(Integer cartId, Integer quantity, User user) {
        CartItem cartItem = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Unauthorized access to cart item");
        }

        Product product = cartItem.getProduct();
        if (product.getStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock available. Remaining stock: " + product.getStock());
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return mapToResponse(cartItem);
    }

    @Transactional
    public void removeFromCart(Integer cartId, User user) {
        CartItem cartItem = cartItemRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("Unauthorized access to cart item");
        }

        cartItemRepository.delete(cartItem);
    }

    @Transactional
    public void clearCart(User user) {
        List<CartItem> items = cartItemRepository.findByUserUserId(user.getUserId());
        cartItemRepository.deleteAll(items);
    }

    private CartItemResponse mapToResponse(CartItem item) {
        String imageUrl = "";
        if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
            imageUrl = item.getProduct().getImages().get(0).getImageUrl();
        }
        return new CartItemResponse(
                item.getCartId(),
                item.getProduct().getProductId(),
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                imageUrl,
                item.getProduct().getStock()
        );
    }
}
