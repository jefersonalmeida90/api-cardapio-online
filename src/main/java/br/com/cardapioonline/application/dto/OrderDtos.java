package br.com.cardapioonline.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record CreateOrderItemRequest(@NotNull UUID productId, @Min(1) int quantity) {
    }

    public record CreateOrderRequest(
            @NotBlank String clientName,
            @NotBlank String clientPhone,
            @NotBlank String address,
            @NotBlank String source,
            @Valid @NotEmpty List<CreateOrderItemRequest> items,
            String note
    ) {
    }

    public record OrderItemResponse(
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }

    public record OrderResponse(
            UUID id,
            String number,
            String clientName,
            String clientPhone,
            String address,
            BigDecimal total,
            String status,
            String date,
            String createdAt,
            String source,
            String note,
            List<OrderItemResponse> items
    ) {
    }
}
