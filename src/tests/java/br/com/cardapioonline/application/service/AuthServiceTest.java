package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.dto.AuthDtos.LoginRequest;
import br.com.cardapioonline.application.dto.AuthDtos.LoginResponse;
import br.com.cardapioonline.infrastructure.config.AdminAuthProperties;
import br.com.cardapioonline.infrastructure.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtService jwtService;

    private final AdminAuthProperties properties = new AdminAuthProperties(
            "admin@cardapioonline.local",
            "Admin@123",
            "CardapioOnline",
            "CardapioOnlineAdmin",
            "dF9zR3Q4c1VnM2JYbk1zN0tQWmV5Q0h4RjVwTnJBY2R6VjJMbTg5WGpUa0Y=",
            400
    );

    @Test
    void shouldLoginWithValidCredentials() {
        AuthService service = new AuthService(properties, jwtService);
        when(jwtService.generateToken("ADMIN@cardapioonline.local")).thenReturn("jwt-token");

        LoginResponse response = service.login(new LoginRequest("ADMIN@cardapioonline.local", "Admin@123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("ADMIN@cardapioonline.local");
        assertThat(response.expiresAt()).isNotNull();
        verify(jwtService).generateToken("ADMIN@cardapioonline.local");
    }

    @Test
    void shouldRejectInvalidCredentials() {
        AuthService service = new AuthService(properties, jwtService);

        assertThatThrownBy(() -> service.login(new LoginRequest("errado@cardapioonline.local", "Admin@123")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException apiException = (ApiException) ex;
                    assertThat(apiException.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(apiException.getMessage()).isEqualTo("Credenciais invalidas.");
                });
    }
}
