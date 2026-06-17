package br.com.cardapioonline.application.dto;

public final class IntegrationDtos {
    private IntegrationDtos() {
    }

    public record IFoodIntegrationRequest(boolean enabled, String clientId, String clientSecret, String merchantId) {
    }
    public record AnotaiIntegrationRequest(boolean enabled, String apiKey, String accountId, String webhookUrl) {
    }
    public record UberEatsIntegrationRequest(boolean enabled, String clientId, String clientSecret, String storeId, String webhookSecret) {
    }
    public record NinetyNineFoodIntegrationRequest(boolean enabled, String clientId, String clientSecret, String storeId, String webhookUrl) {
    }
    public record AiAgentsIntegrationRequest(boolean enabled, String aiProvider, String apiKey, String model, String assistantId, String webhookUrl) {
    }
    public record WhatsAppIntegrationRequest(boolean enabled, String phoneNumberId, String businessAccountId, String accessToken, String appSecret, String verifyToken) {
    }
    public record TakeBlipIntegrationRequest(boolean enabled, String accountId, String apiKey, String webhookUrl) {
    }
    public record ZenviaIntegrationRequest(boolean enabled, String apiKey, String accountId, String webhookUrl) {
    }

    public record IFoodIntegrationResponse(boolean enabled, String clientId, String clientSecret, String merchantId) {
    }
    public record AnotaiIntegrationResponse(boolean enabled, String apiKey, String accountId, String webhookUrl) {
    }
    public record UberEatsIntegrationResponse(boolean enabled, String clientId, String clientSecret, String storeId, String webhookSecret) {
    }
    public record NinetyNineFoodIntegrationResponse(boolean enabled, String clientId, String clientSecret, String storeId, String webhookUrl) {
    }
    public record AiAgentsIntegrationResponse(boolean enabled, String aiProvider, String apiKey, String model, String assistantId, String webhookUrl) {
    }
    public record WhatsAppIntegrationResponse(boolean enabled, String phoneNumberId, String businessAccountId, String accessToken, String appSecret, String verifyToken) {
    }
    public record TakeBlipIntegrationResponse(boolean enabled, String accountId, String apiKey, String webhookUrl) {
    }
    public record ZenviaIntegrationResponse(boolean enabled, String apiKey, String accountId, String webhookUrl) {
    }

    public record IntegrationsOverviewResponse(
            IFoodIntegrationResponse iFood,
            AnotaiIntegrationResponse anotai,
            UberEatsIntegrationResponse uberEats,
            NinetyNineFoodIntegrationResponse ninetyNineFood,
            AiAgentsIntegrationResponse aiAgents,
            WhatsAppIntegrationResponse whatsApp,
            TakeBlipIntegrationResponse takeBlip,
            ZenviaIntegrationResponse zenvia
    ) {
    }
}
