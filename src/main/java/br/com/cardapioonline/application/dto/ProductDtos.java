package br.com.cardapioonline.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank @Size(max = 200) String name,
            String description,
            @DecimalMin("0.01") BigDecimal price,
            @NotBlank String category,
            String imageUrl
    ) {
    }

    public record ProductResponse(
            UUID id,
            String name,
            String description,
            BigDecimal price,
            String category,
            String imageUrl
    ) {
    }
}
