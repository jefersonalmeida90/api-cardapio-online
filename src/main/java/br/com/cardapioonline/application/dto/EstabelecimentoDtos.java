package br.com.cardapioonline.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class EstabelecimentoDtos {
    private EstabelecimentoDtos() {
    }

    public record UpsertEstabelecimentoRequest(
            @NotBlank @Size(max = 200) String name,
            String logoUrl,
            String category,
            String address,
            @NotBlank @Size(max = 20) String whatsapp,
            @NotBlank @Pattern(regexp = "^\\d{2}:\\d{2}$") String openTime,
            @NotBlank @Pattern(regexp = "^\\d{2}:\\d{2}$") String closeTime
    ) {
    }

    public record EstabelecimentoResponse(
            String name,
            String logoUrl,
            String category,
            String address,
            String whatsapp,
            String openTime,
            String closeTime
    ) {
    }
}
