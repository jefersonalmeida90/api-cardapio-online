package br.com.cardapioonline.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.dto.OrderDtos.OrderItemResponse;
import br.com.cardapioonline.application.dto.OrderDtos.OrderResponse;
import br.com.cardapioonline.application.service.OrderService;
import br.com.cardapioonline.infrastructure.security.JwtService;
import br.com.cardapioonline.presentation.exception.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrdersController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldListOrders() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.getAll(1, 5, null)).thenReturn(new PaginatedResult<>(
                List.of(orderResponse(id, "PENDENTE")),
                1, 5, 1, 1, false, false));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id.toString()))
                .andExpect(jsonPath("$.items[0].status").value("PENDENTE"));
    }

    @Test
    void shouldCreateOrder() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.create(any())).thenReturn(orderResponse(id, "PENDENTE"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientName": "Maria",
                                  "clientPhone": "11999999999",
                                  "address": "Rua A",
                                  "source": "site",
                                  "items": [
                                    {
                                      "productId": "11111111-1111-1111-1111-111111111111",
                                      "quantity": 2
                                    }
                                  ],
                                  "note": "Sem cebola"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/orders/" + id))
                .andExpect(jsonPath("$.clientName").value("Maria"));
    }

    @Test
    void shouldValidateCreateOrderPayload() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientName": "",
                                  "clientPhone": "",
                                  "address": "",
                                  "source": "",
                                  "items": []
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldAdvanceAndCancelOrder() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.advance(eq(id))).thenReturn(orderResponse(id, "EM_PREPARO"));
        when(orderService.cancel(eq(id))).thenReturn(orderResponse(id, "CANCELADO"));

        mockMvc.perform(put("/api/orders/{id}/advance", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));

        mockMvc.perform(put("/api/orders/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    private OrderResponse orderResponse(UUID id, String status) {
        return new OrderResponse(
                id,
                "P123456001",
                "Maria",
                "11999999999",
                "Rua A",
                new BigDecimal("30.00"),
                status,
                "2026-06-16",
                "16/06 12:00",
                "SITE",
                "Sem cebola",
                List.of(new OrderItemResponse("Burger", 2, new BigDecimal("15.00"), new BigDecimal("30.00")))
        );
    }
}
