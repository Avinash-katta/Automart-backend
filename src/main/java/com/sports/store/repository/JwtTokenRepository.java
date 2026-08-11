package com.sports.store.repository;

import com.sports.store.model.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Integer> {
    Optional<JwtToken> findByToken(String token);
    Optional<JwtToken> findByUserUserId(Integer userId);
    void deleteByUserUserId(Integer userId);
}
