package br.com.cardapioonline.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.cardapioonline.infrastructure.config.AdminAuthProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private final AdminAuthProperties properties = new AdminAuthProperties(
            "admin@cardapioonline.local",
            "Admin@123",
            "CardapioOnline",
            "CardapioOnlineAdmin",
            "dF9zR3Q4c1VnM2JYbk1zN0tQWmV5Q0h4RjVwTnJBY2R6VjJMbTg5WGpUa0Y=",
            400
    );

    private final JwtService service = new JwtService(properties);

    @Test
    void shouldGenerateAndValidateToken() {
        String token = service.generateToken("admin@cardapioonline.local");

        assertThat(service.isValid(token)).isTrue();
        assertThat(service.parse(token).getSubject()).isEqualTo("admin@cardapioonline.local");
        assertThat(service.parse(token).get("role", String.class)).isEqualTo("Admin");
    }

    @Test
    void shouldThrowWhenTokenIsInvalid() {
        assertThrows(JwtException.class, () -> service.parse("token-invalido"));
    }
}
