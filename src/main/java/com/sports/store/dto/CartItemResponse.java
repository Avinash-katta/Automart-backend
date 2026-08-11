package com.sports.store.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Integer cartId;
    private Integer productId;
    private String name;
    private BigDecimal price;
    private Integer quantity;
    private String imageUrl;
    private Integer stock;

    public CartItemResponse() {
    }

    public CartItemResponse(Integer cartId, Integer productId, String name, BigDecimal price, Integer quantity, String imageUrl, Integer stock) {
        this.cartId = cartId;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.stock = stock;
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
