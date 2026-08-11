package com.sports.store.service;

import com.sports.store.config.JwtService;
import com.sports.store.dto.*;
import com.sports.store.model.JwtToken;
import com.sports.store.model.Role;
import com.sports.store.model.User;
import com.sports.store.repository.JwtTokenRepository;
import com.sports.store.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final TokenBlacklistService blacklistService;

    @Value("${app.reset-token-expiration-minutes}")
    private int resetTokenExpirationMinutes;

    public AuthService(
            UserRepository userRepository,
            JwtTokenRepository jwtTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            EmailService emailService,
            TokenBlacklistService blacklistService
    ) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.blacklistService = blacklistService;
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.CUSTOMER); // Default role CUSTOMER

        userRepository.save(user);
        return "Registration successful. You can log in now.";
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String token = jwtService.generateToken(user);

        // Save or update jwt_token in the database
        LocalDate now = LocalDate.now();
        LocalDate expiresAt = now.plusDays(1); // Mapped token expiration is 24 hours (1 day)

        JwtToken jwtToken = jwtTokenRepository.findByUserUserId(user.getUserId())
                .orElse(new JwtToken());

        jwtToken.setUser(user);
        jwtToken.setToken(token);
        jwtToken.setCreatedAt(now);
        jwtToken.setExpiresAt(expiresAt);

        jwtTokenRepository.save(jwtToken);

        return new LoginResponse(
                token,
                user.getUserId(),
                user.getRealUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + request.getEmail()));

        // Generate a 15-minute password reset token using JWT
        // Extra claim to mark it specifically as a reset token
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("type", "password-reset");

        // Lifetime is 15 minutes
        long resetExpirationMs = resetTokenExpirationMinutes * 60 * 1000L;
        String resetToken = jwtService.generateToken(claims, user);

        // We build token with custom expiration
        // But since JwtService's generateToken defaults to the standard login expiration (24h) if we don't pass custom expiration,
        // let's build the reset token manually here with the 15-minute expiration so it is verified correctly by jwtService
        resetToken = io.jsonwebtoken.Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + resetExpirationMs))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                        io.jsonwebtoken.io.Decoders.BASE64.decode(
                                jwtService.extractClaim(jwtService.generateToken(user), c -> {
                                    // Just read properties from jwtService or inject it
                                    // We will use the same secret key as JwtService
                                    return "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
                                })
                        )
                ))
                .compact();

        try {
            emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email. Please try again later.", e);
        }

        return "Password reset link has been sent to your email.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (blacklistService.isBlacklisted(request.getToken())) {
            throw new IllegalArgumentException("This password reset link has already been used or is invalid.");
        }

        String email;
        try {
            email = jwtService.extractUsername(request.getToken());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or expired password reset link");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Verify the token validity
        if (!jwtService.isTokenValid(request.getToken(), user)) {
            throw new IllegalArgumentException("Password reset link is expired or invalid");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Blacklist the token so it cannot be reused
        blacklistService.blacklistToken(request.getToken());

        return "Password changed successfully. You can log in with your new password.";
    }
}
