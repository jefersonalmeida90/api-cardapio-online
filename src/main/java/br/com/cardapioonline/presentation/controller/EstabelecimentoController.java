package br.com.cardapioonline.presentation.controller;

import br.com.cardapioonline.application.dto.EstabelecimentoDtos.EstabelecimentoResponse;
import br.com.cardapioonline.application.dto.EstabelecimentoDtos.UpsertEstabelecimentoRequest;
import br.com.cardapioonline.application.service.EstabelecimentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estabelecimento")
public class EstabelecimentoController {

    private final EstabelecimentoService service;

    public EstabelecimentoController(EstabelecimentoService service) {
        this.service = service;
    }

    @GetMapping
    public EstabelecimentoResponse get() {
        return service.get();
    }

    @PutMapping
    public EstabelecimentoResponse upsert(@Valid @RequestBody UpsertEstabelecimentoRequest request) {
        return service.upsert(request);
    }
}
