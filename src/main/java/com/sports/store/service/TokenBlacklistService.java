package com.sports.store.service;

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    // Thread-safe set to store used tokens
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public void blacklistToken(String token) {
        if (token != null) {
            blacklistedTokens.add(token);
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null) {
            return true;
        }
        return blacklistedTokens.contains(token);
    }
}
