package com.lima.orderservice.domain.auth.service;

import com.lima.orderservice.config.security.JwtProvider;
import com.lima.orderservice.domain.auth.model.dto.*;
import com.lima.orderservice.domain.auth.model.type.Role;
import com.lima.orderservice.domain.order.model.entity.Customer;
import com.lima.orderservice.domain.order.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
        }

        Customer customer = Customer.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(Role.USER)
                .build();

        customerRepository.save(customer);
        log.info("회원가입 완료: {}", customer.getEmail());

        return generateTokenResponse(customer);
    }

    public AuthResponse login(LoginRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        log.info("로그인 성공: {}", customer.getEmail());
        return generateTokenResponse(customer);
    }

    public AuthResponse refresh(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        // Redis에 저장된 refresh token과 일치하는지 확인
        String stored = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + email);
        if (stored == null || !stored.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh Token이 만료되었거나 이미 사용되었습니다");
        }

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return generateTokenResponse(customer);
    }

    public void logout(String email) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);
        log.info("로그아웃: {}", email);
    }

    private AuthResponse generateTokenResponse(Customer customer) {
        String accessToken = jwtProvider.generateAccessToken(
                customer.getId(), customer.getEmail(), customer.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(customer.getEmail());

        // Redis에 refresh token 저장 (7일)
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + customer.getEmail(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(customer.getEmail())
                .name(customer.getName())
                .role(customer.getRole().name())
                .build();
    }
}
