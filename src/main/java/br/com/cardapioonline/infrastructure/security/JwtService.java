package br.com.cardapioonline.infrastructure.security;

import br.com.cardapioonline.infrastructure.config.AdminAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final AdminAuthProperties properties;
    private final SecretKey secretKey;

    public JwtService(AdminAuthProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email) {
        Instant expiresAt = Instant.now().plusSeconds(properties.tokenExpirationMinutes() * 60L);
        return Jwts.builder()
                .issuer(properties.jwtIssuer())
                .audience().add(properties.jwtAudience()).and()
                .subject(email)
                .claims(Map.of("email", email, "role", "Admin"))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        Claims claims = parse(token);
        return properties.jwtIssuer().equals(claims.getIssuer())
                && claims.getAudience().contains(properties.jwtAudience())
                && claims.getExpiration().after(new Date());
    }
}
