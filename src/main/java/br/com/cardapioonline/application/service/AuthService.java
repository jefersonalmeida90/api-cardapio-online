package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.AuthDtos.LoginRequest;
import br.com.cardapioonline.application.dto.AuthDtos.LoginResponse;
import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.infrastructure.config.AdminAuthProperties;
import br.com.cardapioonline.infrastructure.security.JwtService;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminAuthProperties properties;
    private final JwtService jwtService;

    public AuthService(AdminAuthProperties properties, JwtService jwtService) {
        this.properties = properties;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        if (!properties.email().equalsIgnoreCase(request.email()) || !properties.password().equals(request.password())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas.");
        }

        Instant expiresAt = Instant.now().plusSeconds(properties.tokenExpirationMinutes() * 60L);
        return new LoginResponse(jwtService.generateToken(request.email()), expiresAt, request.email());
    }
}
