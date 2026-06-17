package br.com.cardapioonline.presentation.controller;

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
import br.com.cardapioonline.application.service.IntegrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationsController {

    private final IntegrationService service;

    public IntegrationsController(IntegrationService service) {
        this.service = service;
    }

    @GetMapping
    public IntegrationsOverviewResponse get() { return service.getAll(); }

    @PutMapping("/ifood")
    public IFoodIntegrationResponse upsertIFood(@RequestBody IFoodIntegrationRequest request) { return service.upsertIFood(request); }

    @PutMapping("/anotai")
    public AnotaiIntegrationResponse upsertAnotai(@RequestBody AnotaiIntegrationRequest request) { return service.upsertAnotai(request); }

    @PutMapping("/ubereats")
    public UberEatsIntegrationResponse upsertUberEats(@RequestBody UberEatsIntegrationRequest request) { return service.upsertUberEats(request); }

    @PutMapping("/99food")
    public NinetyNineFoodIntegrationResponse upsertNinetyNineFood(@RequestBody NinetyNineFoodIntegrationRequest request) { return service.upsertNinetyNineFood(request); }

    @PutMapping("/aiagents")
    public AiAgentsIntegrationResponse upsertAiAgents(@RequestBody AiAgentsIntegrationRequest request) { return service.upsertAiAgents(request); }

    @PutMapping("/whatsapp")
    public WhatsAppIntegrationResponse upsertWhatsApp(@RequestBody WhatsAppIntegrationRequest request) { return service.upsertWhatsApp(request); }

    @PutMapping("/takeblip")
    public TakeBlipIntegrationResponse upsertTakeBlip(@RequestBody TakeBlipIntegrationRequest request) { return service.upsertTakeBlip(request); }

    @PutMapping("/zenvia")
    public ZenviaIntegrationResponse upsertZenvia(@RequestBody ZenviaIntegrationRequest request) { return service.upsertZenvia(request); }
}
