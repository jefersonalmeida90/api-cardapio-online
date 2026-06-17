package br.com.cardapioonline.infrastructure.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "admin-auth")
public record AdminAuthProperties(
        @Email @NotBlank String email,
        @NotBlank String password,
        @NotBlank String jwtIssuer,
        @NotBlank String jwtAudience,
        @NotBlank @Size(min = 32) String jwtSecret,
        @Min(1) int tokenExpirationMinutes
) {
}
