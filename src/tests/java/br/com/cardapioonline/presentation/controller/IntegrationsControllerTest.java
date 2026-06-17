package br.com.cardapioonline.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.dto.IntegrationDtos.AiAgentsIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.AnotaiIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.IFoodIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.IntegrationsOverviewResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.NinetyNineFoodIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.TakeBlipIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.UberEatsIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.WhatsAppIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.ZenviaIntegrationResponse;
import br.com.cardapioonline.application.service.IntegrationService;
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

@WebMvcTest(IntegrationsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class IntegrationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IntegrationService integrationService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldGetIntegrationsOverview() throws Exception {
        when(integrationService.getAll()).thenReturn(new IntegrationsOverviewResponse(
                new IFoodIntegrationResponse(true, "client", "secret", "merchant"),
                new AnotaiIntegrationResponse(false, "", "", ""),
                new UberEatsIntegrationResponse(false, "", "", "", ""),
                new NinetyNineFoodIntegrationResponse(false, "", "", "", ""),
                new AiAgentsIntegrationResponse(true, "openai", "key", "gpt-4o", "assistant", "http://hook"),
                new WhatsAppIntegrationResponse(false, "", "", "", "", ""),
                new TakeBlipIntegrationResponse(false, "", "", ""),
                new ZenviaIntegrationResponse(false, "", "", "")
        ));

        mockMvc.perform(get("/api/integrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iFood.enabled").value(true))
                .andExpect(jsonPath("$.aiAgents.aiProvider").value("openai"));
    }

    @Test
    void shouldUpsertIFood() throws Exception {
        when(integrationService.upsertIFood(any())).thenReturn(new IFoodIntegrationResponse(true, "client", "secret", "merchant"));

        mockMvc.perform(put("/api/integrations/ifood")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": true,
                                  "clientId": "client",
                                  "clientSecret": "secret",
                                  "merchantId": "merchant"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value("merchant"));
    }
}
