package com.sports.store.repository;

import com.sports.store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    @Query("SELECT o FROM Order o WHERE o.user.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);

    List<Order> findAllByOrderByCreatedAtDesc();
}
