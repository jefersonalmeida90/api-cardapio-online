package br.com.cardapioonline.domain.entity;

import br.com.cardapioonline.domain.enums.IntegrationProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "Integrations")
public class Integration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Provider", nullable = false, unique = true, length = 30)
    private IntegrationProvider provider;

    @Column(name = "Enabled", nullable = false)
    private boolean enabled;

    @Column(name = "ClientId", length = 200)
    private String clientId = "";

    @Column(name = "ClientSecret", length = 500)
    private String clientSecret = "";

    @Column(name = "AccountId", length = 200)
    private String accountId = "";

    @Column(name = "ApiKey", length = 500)
    private String apiKey = "";

    @Column(name = "AccessToken", length = 1000)
    private String accessToken = "";

    @Column(name = "AppSecret", length = 500)
    private String appSecret = "";

    @Column(name = "VerifyToken", length = 200)
    private String verifyToken = "";

    @Column(name = "PhoneNumberId", length = 200)
    private String phoneNumberId = "";

    @Column(name = "WebhookUrl", length = 1000)
    private String webhookUrl = "";

    @Column(name = "WebhookSecret", length = 500)
    private String webhookSecret = "";

    @Column(name = "AiProvider", length = 50)
    private String aiProvider = "";

    @Column(name = "Model", length = 100)
    private String model = "";

    @Column(name = "AssistantId", length = 200)
    private String assistantId = "";

    @Column(name = "UpdatedAt", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (updatedAt == null) {
            updatedAt = Instant.now();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public IntegrationProvider getProvider() { return provider; }
    public void setProvider(IntegrationProvider provider) { this.provider = provider; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
    public String getVerifyToken() { return verifyToken; }
    public void setVerifyToken(String verifyToken) { this.verifyToken = verifyToken; }
    public String getPhoneNumberId() { return phoneNumberId; }
    public void setPhoneNumberId(String phoneNumberId) { this.phoneNumberId = phoneNumberId; }
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }
    public String getAiProvider() { return aiProvider; }
    public void setAiProvider(String aiProvider) { this.aiProvider = aiProvider; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getAssistantId() { return assistantId; }
    public void setAssistantId(String assistantId) { this.assistantId = assistantId; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
