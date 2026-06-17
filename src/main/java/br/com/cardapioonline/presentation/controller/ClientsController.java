package br.com.cardapioonline.presentation.controller;

import br.com.cardapioonline.application.dto.ClientDtos.AuthenticateClientRequest;
import br.com.cardapioonline.application.dto.ClientDtos.ClientResponse;
import br.com.cardapioonline.application.dto.ClientDtos.CreateClientRequest;
import br.com.cardapioonline.application.service.ClientService;
import br.com.cardapioonline.application.common.PaginatedResult;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientsController {

    private final ClientService service;

    public ClientsController(ClientService service) {
        this.service = service;
    }

    @PostMapping("/authenticate")
    public ClientResponse authenticate(@Valid @RequestBody AuthenticateClientRequest request) {
        return service.authenticate(request);
    }

    @GetMapping
    public PaginatedResult<ClientResponse> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(required = false) String search) {
        return service.getAll(page, pageSize, search);
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        ClientResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/clients/" + response.id())).body(response);
    }
}
