package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.IntegrationDtos.AiAgentsIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.AiAgentsIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.AnotaiIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.AnotaiIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.IFoodIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.IFoodIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.IntegrationsOverviewResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.NinetyNineFoodIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.NinetyNineFoodIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.TakeBlipIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.TakeBlipIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.UberEatsIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.UberEatsIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.WhatsAppIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.WhatsAppIntegrationResponse;
import br.com.cardapioonline.application.dto.IntegrationDtos.ZenviaIntegrationRequest;
import br.com.cardapioonline.application.dto.IntegrationDtos.ZenviaIntegrationResponse;
import br.com.cardapioonline.domain.entity.Integration;
import br.com.cardapioonline.domain.enums.IntegrationProvider;
import br.com.cardapioonline.application.port.out.IntegrationRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class IntegrationService {

    private final IntegrationRepository repository;

    public IntegrationService(IntegrationRepository repository) {
        this.repository = repository;
    }

    public IntegrationsOverviewResponse getAll() {
        return new IntegrationsOverviewResponse(
                toIFood(find(IntegrationProvider.IFOOD)),
                toAnotai(find(IntegrationProvider.ANOTAI)),
                toUberEats(find(IntegrationProvider.UBEREATS)),
                toNinetyNineFood(find(IntegrationProvider.NINETYNINEFOOD)),
                toAiAgents(find(IntegrationProvider.AIAGENTS)),
                toWhatsApp(find(IntegrationProvider.WHATSAPP)),
                toTakeBlip(find(IntegrationProvider.TAKEBLIP)),
                toZenvia(find(IntegrationProvider.ZENVIA))
        );
    }

    public IFoodIntegrationResponse upsertIFood(IFoodIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.IFOOD);
        integration.setEnabled(request.enabled());
        integration.setClientId(nvl(request.clientId()));
        integration.setClientSecret(nvl(request.clientSecret()));
        integration.setAccountId(nvl(request.merchantId()));
        return toIFood(save(integration));
    }

    public AnotaiIntegrationResponse upsertAnotai(AnotaiIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.ANOTAI);
        integration.setEnabled(request.enabled());
        integration.setApiKey(nvl(request.apiKey()));
        integration.setAccountId(nvl(request.accountId()));
        integration.setWebhookUrl(nvl(request.webhookUrl()));
        return toAnotai(save(integration));
    }

    public UberEatsIntegrationResponse upsertUberEats(UberEatsIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.UBEREATS);
        integration.setEnabled(request.enabled());
        integration.setClientId(nvl(request.clientId()));
        integration.setClientSecret(nvl(request.clientSecret()));
        integration.setAccountId(nvl(request.storeId()));
        integration.setWebhookSecret(nvl(request.webhookSecret()));
        return toUberEats(save(integration));
    }

    public NinetyNineFoodIntegrationResponse upsertNinetyNineFood(NinetyNineFoodIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.NINETYNINEFOOD);
        integration.setEnabled(request.enabled());
        integration.setClientId(nvl(request.clientId()));
        integration.setClientSecret(nvl(request.clientSecret()));
        integration.setAccountId(nvl(request.storeId()));
        integration.setWebhookUrl(nvl(request.webhookUrl()));
        return toNinetyNineFood(save(integration));
    }

    public AiAgentsIntegrationResponse upsertAiAgents(AiAgentsIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.AIAGENTS);
        integration.setEnabled(request.enabled());
        integration.setAiProvider(nvl(request.aiProvider()));
        integration.setApiKey(nvl(request.apiKey()));
        integration.setModel(nvl(request.model()));
        integration.setAssistantId(nvl(request.assistantId()));
        integration.setWebhookUrl(nvl(request.webhookUrl()));
        return toAiAgents(save(integration));
    }

    public WhatsAppIntegrationResponse upsertWhatsApp(WhatsAppIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.WHATSAPP);
        integration.setEnabled(request.enabled());
        integration.setPhoneNumberId(nvl(request.phoneNumberId()));
        integration.setAccountId(nvl(request.businessAccountId()));
        integration.setAccessToken(nvl(request.accessToken()));
        integration.setAppSecret(nvl(request.appSecret()));
        integration.setVerifyToken(nvl(request.verifyToken()));
        return toWhatsApp(save(integration));
    }

    public TakeBlipIntegrationResponse upsertTakeBlip(TakeBlipIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.TAKEBLIP);
        integration.setEnabled(request.enabled());
        integration.setAccountId(nvl(request.accountId()));
        integration.setApiKey(nvl(request.apiKey()));
        integration.setWebhookUrl(nvl(request.webhookUrl()));
        return toTakeBlip(save(integration));
    }

    public ZenviaIntegrationResponse upsertZenvia(ZenviaIntegrationRequest request) {
        Integration integration = find(IntegrationProvider.ZENVIA);
        integration.setEnabled(request.enabled());
        integration.setApiKey(nvl(request.apiKey()));
        integration.setAccountId(nvl(request.accountId()));
        integration.setWebhookUrl(nvl(request.webhookUrl()));
        return toZenvia(save(integration));
    }

    private Integration save(Integration integration) {
        integration.setUpdatedAt(Instant.now());
        return repository.save(integration);
    }

    private Integration find(IntegrationProvider provider) {
        return repository.findByProvider(provider).orElseGet(() -> {
            Integration integration = new Integration();
            integration.setProvider(provider);
            return integration;
        });
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private IFoodIntegrationResponse toIFood(Integration i) { return new IFoodIntegrationResponse(i.isEnabled(), i.getClientId(), i.getClientSecret(), i.getAccountId()); }
    private AnotaiIntegrationResponse toAnotai(Integration i) { return new AnotaiIntegrationResponse(i.isEnabled(), i.getApiKey(), i.getAccountId(), i.getWebhookUrl()); }
    private UberEatsIntegrationResponse toUberEats(Integration i) { return new UberEatsIntegrationResponse(i.isEnabled(), i.getClientId(), i.getClientSecret(), i.getAccountId(), i.getWebhookSecret()); }
    private NinetyNineFoodIntegrationResponse toNinetyNineFood(Integration i) { return new NinetyNineFoodIntegrationResponse(i.isEnabled(), i.getClientId(), i.getClientSecret(), i.getAccountId(), i.getWebhookUrl()); }
    private AiAgentsIntegrationResponse toAiAgents(Integration i) { return new AiAgentsIntegrationResponse(i.isEnabled(), i.getAiProvider(), i.getApiKey(), i.getModel(), i.getAssistantId(), i.getWebhookUrl()); }
    private WhatsAppIntegrationResponse toWhatsApp(Integration i) { return new WhatsAppIntegrationResponse(i.isEnabled(), i.getPhoneNumberId(), i.getAccountId(), i.getAccessToken(), i.getAppSecret(), i.getVerifyToken()); }
    private TakeBlipIntegrationResponse toTakeBlip(Integration i) { return new TakeBlipIntegrationResponse(i.isEnabled(), i.getAccountId(), i.getApiKey(), i.getWebhookUrl()); }
    private ZenviaIntegrationResponse toZenvia(Integration i) { return new ZenviaIntegrationResponse(i.isEnabled(), i.getApiKey(), i.getAccountId(), i.getWebhookUrl()); }
}
