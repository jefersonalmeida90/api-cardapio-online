package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cardapioonline.application.dto.EstabelecimentoDtos.EstabelecimentoResponse;
import br.com.cardapioonline.application.dto.EstabelecimentoDtos.UpsertEstabelecimentoRequest;
import br.com.cardapioonline.application.port.out.EstabelecimentoRepository;
import br.com.cardapioonline.domain.entity.Estabelecimento;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EstabelecimentoServiceTest {

    @Mock
    private EstabelecimentoRepository repository;

    @Test
    void shouldReturnDefaultEstabelecimentoWhenRepositoryIsEmpty() {
        EstabelecimentoService service = new EstabelecimentoService(repository);
        when(repository.findAll()).thenReturn(List.of());

        EstabelecimentoResponse response = service.get();

        assertThat(response.name()).isEqualTo("Cardapio Online");
        assertThat(response.openTime()).isEqualTo("18:00");
        assertThat(response.closeTime()).isEqualTo("22:00");
    }

    @Test
    void shouldReturnExistingEstabelecimento() {
        EstabelecimentoService service = new EstabelecimentoService(repository);
        Estabelecimento est = new Estabelecimento();
        est.setName("Minha Loja");
        est.setLogoUrl("/logo.png");
        est.setCategory("pizza");
        est.setAddress("Rua B");
        est.setWhatsapp("11999999999");
        est.setOpenTime(LocalTime.of(11, 0));
        est.setCloseTime(LocalTime.of(23, 0));
        when(repository.findAll()).thenReturn(List.of(est));

        EstabelecimentoResponse response = service.get();

        assertThat(response.name()).isEqualTo("Minha Loja");
        assertThat(response.logoUrl()).isEqualTo("/logo.png");
    }

    @Test
    void shouldCreateEstabelecimentoOnUpsert() {
        EstabelecimentoService service = new EstabelecimentoService(repository);
        UpsertEstabelecimentoRequest request = new UpsertEstabelecimentoRequest(
                "Nova Loja", null, null, null, "11999999999", "10:00", "22:00");
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(Estabelecimento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EstabelecimentoResponse response = service.upsert(request);

        ArgumentCaptor<Estabelecimento> captor = ArgumentCaptor.forClass(Estabelecimento.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLogoUrl()).isEmpty();
        assertThat(captor.getValue().getCategory()).isEmpty();
        assertThat(response.name()).isEqualTo("Nova Loja");
    }

    @Test
    void shouldUpdateExistingEstabelecimentoOnUpsert() {
        EstabelecimentoService service = new EstabelecimentoService(repository);
        Estabelecimento est = new Estabelecimento();
        est.setName("Antiga");
        when(repository.findAll()).thenReturn(List.of(est));
        when(repository.save(any(Estabelecimento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EstabelecimentoResponse response = service.upsert(new UpsertEstabelecimentoRequest(
                "Atualizada", "/logo.png", "hamburgueria", "Rua C", "11911111111", "09:30", "21:30"));

        assertThat(response.name()).isEqualTo("Atualizada");
        assertThat(response.openTime()).isEqualTo("09:30");
        assertThat(response.closeTime()).isEqualTo("21:30");
    }
}
