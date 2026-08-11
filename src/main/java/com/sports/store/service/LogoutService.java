package com.sports.store.service;

import com.sports.store.model.User;
import com.sports.store.repository.JwtTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LogoutService {

    private final JwtTokenRepository jwtTokenRepository;

    public LogoutService(JwtTokenRepository jwtTokenRepository) {
        this.jwtTokenRepository = jwtTokenRepository;
    }

    @Transactional
    public void logout(User user) {
        if (user == null || user.getUserId() == null) {
            return;
        }
        jwtTokenRepository.findByUserUserId(user.getUserId())
                .ifPresent(jwtTokenRepository::delete);
    }
}
