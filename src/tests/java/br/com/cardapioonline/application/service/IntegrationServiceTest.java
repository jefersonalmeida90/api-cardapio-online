package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cardapioonline.application.dto.IntegrationDtos.AiAgentsIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.IntegrationsOverviewResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.WhatsAppIntegrationRequest;
import br.com.cardapioonline.application.port.out.IntegrationRepository;
import br.com.cardapioonline.domain.entity.Integration;
import br.com.cardapioonline.domain.enums.IntegrationProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IntegrationServiceTest {

    @Mock
    private IntegrationRepository repository;

    @Test
    void shouldReturnOverviewEvenWhenNoIntegrationExists() {
        IntegrationService service = new IntegrationService(repository);
        when(repository.findByProvider(any())).thenReturn(Optional.empty());

        IntegrationsOverviewResponse response = service.getAll();

        assertThat(response.iFood()).isNotNull();
        assertThat(response.anotai()).isNotNull();
        assertThat(response.whatsApp()).isNotNull();
        assertThat(response.iFood().enabled()).isFalse();
    }

    @Test
    void shouldUpsertAiAgentsReplacingNullsWithEmptyStrings() {
        IntegrationService service = new IntegrationService(repository);
        when(repository.findByProvider(IntegrationProvider.AIAGENTS)).thenReturn(Optional.empty());
        when(repository.save(any(Integration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.upsertAiAgents(new AiAgentsIntegrationRequest(true, null, null, "gpt-4o", null, null));

        ArgumentCaptor<Integration> captor = ArgumentCaptor.forClass(Integration.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getProvider()).isEqualTo(IntegrationProvider.AIAGENTS);
        assertThat(captor.getValue().getAiProvider()).isEmpty();
        assertThat(captor.getValue().getApiKey()).isEmpty();
        assertThat(response.model()).isEqualTo("gpt-4o");
    }

    @Test
    void shouldUpdateExistingWhatsAppIntegration() {
        IntegrationService service = new IntegrationService(repository);
        Integration existing = new Integration();
        existing.setProvider(IntegrationProvider.WHATSAPP);
        when(repository.findByProvider(IntegrationProvider.WHATSAPP)).thenReturn(Optional.of(existing));
        when(repository.save(any(Integration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.upsertWhatsApp(new WhatsAppIntegrationRequest(
                true, "phone-id", "account-id", "token", "secret", "verify"));

        assertThat(response.enabled()).isTrue();
        assertThat(response.phoneNumberId()).isEqualTo("phone-id");
        assertThat(existing.getVerifyToken()).isEqualTo("verify");
    }
}
