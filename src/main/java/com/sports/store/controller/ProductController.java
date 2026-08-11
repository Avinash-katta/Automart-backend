package com.sports.store.controller;

import com.sports.store.model.Product;
import com.sports.store.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Integer id) {
        return productRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{id}")
    public List<Product> getProductsByCategory(@PathVariable("id") Integer categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam("name") String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> addProductReview(@PathVariable("id") Integer id, @RequestBody Map<String, Object> payload) {
        return productRepository.findById(id)
                .map(product -> {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> reviewsList = new ArrayList<>();
                    if (product.getReviewsJson() != null && !product.getReviewsJson().trim().isEmpty()) {
                        try {
                            reviewsList = mapper.readValue(product.getReviewsJson(), new TypeReference<List<Map<String, Object>>>() {});
                        } catch (Exception e) {
                            // If parse error, start with empty list
                        }
                    }
                    
                    Map<String, Object> newReview = new HashMap<>();
                    newReview.put("id", System.currentTimeMillis());
                    newReview.put("author", payload.getOrDefault("author", "Verified Buyer"));
                    newReview.put("rating", payload.getOrDefault("rating", 5));
                    newReview.put("comment", payload.getOrDefault("comment", ""));
                    newReview.put("date", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM d, yyyy")));
                    
                    reviewsList.add(0, newReview);
                    
                    try {
                        String updatedJson = mapper.writeValueAsString(reviewsList);
                        product.setReviewsJson(updatedJson);
                        productRepository.save(product);
                        return ResponseEntity.ok(product);
                    } catch (Exception e) {
                        return ResponseEntity.internalServerError().body("Failed to serialize reviews");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
