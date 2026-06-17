package br.com.cardapioonline.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.dto.EstabelecimentoDtos.EstabelecimentoResponse;
import br.com.cardapioonline.application.service.EstabelecimentoService;
import br.com.cardapioonline.infrastructure.security.JwtService;
import br.com.cardapioonline.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EstabelecimentoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class EstabelecimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstabelecimentoService estabelecimentoService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldGetEstabelecimento() throws Exception {
        when(estabelecimentoService.get()).thenReturn(new EstabelecimentoResponse(
                "Loja", "/logo.png", "hamburgueria", "Rua A", "11999999999", "18:00", "22:00"));

        mockMvc.perform(get("/api/estabelecimento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Loja"));
    }

    @Test
    void shouldUpsertEstabelecimento() throws Exception {
        when(estabelecimentoService.upsert(any())).thenReturn(new EstabelecimentoResponse(
                "Nova Loja", "", "", "", "11999999999", "09:00", "21:00"));

        mockMvc.perform(put("/api/estabelecimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nova Loja",
                                  "logoUrl": "",
                                  "category": "",
                                  "address": "",
                                  "whatsapp": "11999999999",
                                  "openTime": "09:00",
                                  "closeTime": "21:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nova Loja"));
    }

    @Test
    void shouldValidateEstabelecimentoPayload() throws Exception {
        mockMvc.perform(put("/api/estabelecimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "whatsapp": "",
                                  "openTime": "9:00",
                                  "closeTime": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}
