package br.com.cardapioonline.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.dto.ClientDtos.ClientResponse;
import br.com.cardapioonline.application.service.ClientService;
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

@WebMvcTest(ClientsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class ClientsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClientService clientService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldCreateClient() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientService.create(any())).thenReturn(clientResponse(id));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joao",
                                  "email": "joao@email.com",
                                  "phone": "11999999999",
                                  "zipCode": "01234-567",
                                  "street": "Rua A",
                                  "number": "100",
                                  "neighborhood": "Centro",
                                  "city": "Sao Paulo",
                                  "state": "SP",
                                  "complement": "",
                                  "password": "Senha@123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clients/" + id))
                .andExpect(jsonPath("$.name").value("Joao"));
    }

    @Test
    void shouldAuthenticateClient() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientService.authenticate(any())).thenReturn(clientResponse(id));

        mockMvc.perform(post("/api/clients/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "joao@email.com",
                                  "password": "Senha@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("joao@email.com"));
    }

    @Test
    void shouldListClients() throws Exception {
        UUID id = UUID.randomUUID();
        when(clientService.getAll(1, 5, null)).thenReturn(new PaginatedResult<>(List.of(clientResponse(id)), 1, 5, 1, 1, false, false));

        mockMvc.perform(get("/api/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(id.toString()));
    }

    @Test
    void shouldValidateClientPayload() throws Exception {
        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "invalido",
                                  "phone": "",
                                  "zipCode": "",
                                  "street": "",
                                  "number": "",
                                  "neighborhood": "",
                                  "city": "",
                                  "state": "S",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    private ClientResponse clientResponse(UUID id) {
        return new ClientResponse(
                id,
                "Joao",
                "joao@email.com",
                "11999999999",
                "01234-567",
                "Rua A",
                "100",
                "Centro",
                "Sao Paulo",
                "SP",
                "",
                "Rua A, 100 - Centro - Sao Paulo/SP - CEP 01234-567",
                "16/06/2026",
                1,
                new BigDecimal("20.00")
        );
    }
}
