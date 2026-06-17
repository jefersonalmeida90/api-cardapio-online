package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.EstabelecimentoDtos.EstabelecimentoResponse;
import br.com.cardapioonline.application.dto.EstabelecimentoDtos.UpsertEstabelecimentoRequest;
import br.com.cardapioonline.domain.entity.Estabelecimento;
import br.com.cardapioonline.application.port.out.EstabelecimentoRepository;
import java.time.Instant;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class EstabelecimentoService {

    private final EstabelecimentoRepository repository;

    public EstabelecimentoService(EstabelecimentoRepository repository) {
        this.repository = repository;
    }

    public EstabelecimentoResponse get() {
        return repository.findAll().stream().findFirst()
                .map(this::toResponse)
                .orElse(new EstabelecimentoResponse("Cardapio Online", "", "hamburgueria", "", "", "18:00", "22:00"));
    }

    public EstabelecimentoResponse upsert(UpsertEstabelecimentoRequest request) {
        Estabelecimento est = repository.findAll().stream().findFirst().orElseGet(Estabelecimento::new);
        est.setName(request.name());
        est.setLogoUrl(request.logoUrl() == null ? "" : request.logoUrl());
        est.setCategory(request.category() == null ? "" : request.category());
        est.setAddress(request.address() == null ? "" : request.address());
        est.setWhatsapp(request.whatsapp());
        est.setOpenTime(LocalTime.parse(request.openTime()));
        est.setCloseTime(LocalTime.parse(request.closeTime()));
        est.setUpdatedAt(Instant.now());
        return toResponse(repository.save(est));
    }

    private EstabelecimentoResponse toResponse(Estabelecimento est) {
        return new EstabelecimentoResponse(
                est.getName(),
                est.getLogoUrl(),
                est.getCategory(),
                est.getAddress(),
                est.getWhatsapp(),
                est.getOpenTime().toString(),
                est.getCloseTime().toString()
        );
    }
}
