package br.com.cardapioonline.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.dto.ProductDtos.ProductResponse;
import br.com.cardapioonline.application.service.ProductService;
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

@WebMvcTest(ProductsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class ProductsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldListProducts() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.getAll(1, 5, null)).thenReturn(new PaginatedResult<>(
                List.of(new ProductResponse(id, "Burger", "Desc", new BigDecimal("20.00"), "hamburguer", "/img.png")),
                1, 5, 1, 1, false, false));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id.toString()))
                .andExpect(jsonPath("$.items[0].name").value("Burger"));
    }

    @Test
    void shouldCreateProduct() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.create(any())).thenReturn(new ProductResponse(id, "Burger", "Desc", new BigDecimal("20.00"), "hamburguer", "/img.png"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Burger",
                                  "description": "Desc",
                                  "price": 20.00,
                                  "category": "hamburguer",
                                  "imageUrl": "/img.png"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/" + id))
                .andExpect(jsonPath("$.name").value("Burger"));
    }

    @Test
    void shouldValidateCreatePayload() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "price": 0,
                                  "category": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldUpdateAndDeleteProduct() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any())).thenReturn(new ProductResponse(id, "Novo", "Desc", new BigDecimal("10.00"), "bebida", ""));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Novo",
                                  "description": "Desc",
                                  "price": 10.00,
                                  "category": "bebida",
                                  "imageUrl": ""
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Novo"));

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());
    }
}
