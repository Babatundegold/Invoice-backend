package com.invoicesaas.service;

import com.invoicesaas.dto.AuthDtos;
import com.invoicesaas.entity.Company;
import com.invoicesaas.entity.User;
import com.invoicesaas.exception.BadRequestException;
import com.invoicesaas.repository.CompanyRepository;
import com.invoicesaas.repository.UserRepository;
import com.invoicesaas.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        user = userRepository.save(user);

        // Company profile created at registration - logo stays null (optional) until uploaded later
        Company company = Company.builder()
                .owner(user)
                .businessName(request.getBusinessName())
                .currency("NGN")
                .invoicePrefix("INV-")
                .build();
        companyRepository.save(company);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return buildResponse(user, token);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return buildResponse(user, token);
    }

    private AuthDtos.AuthResponse buildResponse(User user, String token) {
        AuthDtos.AuthResponse response = new AuthDtos.AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPremium(user.isPremium());
        return response;
    }
}
