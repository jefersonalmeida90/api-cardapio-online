package br.com.cardapioonline.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public final class ClientDtos {
    private ClientDtos() {
    }

    public record CreateClientRequest(
            @NotBlank @Size(max = 200) String name,
            @Email @NotBlank @Size(max = 200) String email,
            @NotBlank @Size(max = 20) String phone,
            @NotBlank @Size(max = 9) String zipCode,
            @NotBlank @Size(max = 200) String street,
            @NotBlank @Size(max = 20) String number,
            @NotBlank @Size(max = 100) String neighborhood,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(min = 2, max = 2) String state,
            @Size(max = 100) String complement,
            @NotBlank @Size(min = 8, max = 64) String password
    ) {
    }

    public record AuthenticateClientRequest(
            @Email @NotBlank @Size(max = 200) String email,
            @NotBlank @Size(max = 64) String password
    ) {
    }

    public record ClientResponse(
            UUID id,
            String name,
            String email,
            String phone,
            String zipCode,
            String street,
            String number,
            String neighborhood,
            String city,
            String state,
            String complement,
            String address,
            String registeredAt,
            int totalOrders,
            BigDecimal totalSpent
    ) {
    }
}
