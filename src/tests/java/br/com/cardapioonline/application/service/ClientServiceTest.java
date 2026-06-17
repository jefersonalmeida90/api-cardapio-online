package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.common.PasswordHashService;
import br.com.cardapioonline.application.dto.ClientDtos.AuthenticateClientRequest;
import br.com.cardapioonline.application.dto.ClientDtos.ClientResponse;
import br.com.cardapioonline.application.dto.ClientDtos.CreateClientRequest;
import br.com.cardapioonline.application.port.out.ClientRepository;
import br.com.cardapioonline.application.port.out.OrderRepository;
import br.com.cardapioonline.domain.entity.Client;
import br.com.cardapioonline.domain.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    private final PasswordHashService passwordHashService = new PasswordHashService();

    @Test
    void shouldCreateClient() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        CreateClientRequest request = new CreateClientRequest(
                "Joao",
                "joao@email.com",
                "11999999999",
                "01234-567",
                "Rua A",
                "100",
                "Centro",
                "Sao Paulo",
                "SP",
                null,
                "Senha@123"
        );
        when(clientRepository.existsByPhone(request.phone())).thenReturn(false);
        when(clientRepository.existsByEmail(request.email())).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> {
            Client client = invocation.getArgument(0);
            client.setId(UUID.randomUUID());
            client.setRegisteredAt(LocalDate.of(2026, 6, 16));
            return client;
        });

        ClientResponse response = service.create(request);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        Client saved = captor.getValue();
        assertThat(saved.getComplement()).isEmpty();
        assertThat(saved.getPasswordHash()).isNotEqualTo("Senha@123");
        assertThat(passwordHashService.verify("Senha@123", saved.getPasswordHash())).isTrue();
        assertThat(response.name()).isEqualTo("Joao");
        assertThat(response.address()).contains("Rua A, 100");
        assertThat(response.totalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldRejectCreateWhenPhoneAlreadyExists() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        CreateClientRequest request = new CreateClientRequest(
                "Joao", "joao@email.com", "11999999999", "01234-567", "Rua A", "100", "Centro", "Sao Paulo", "SP", "", "Senha@123");
        when(clientRepository.existsByPhone(request.phone())).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void shouldRejectCreateWhenEmailAlreadyExists() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        CreateClientRequest request = new CreateClientRequest(
                "Joao", "joao@email.com", "11999999999", "01234-567", "Rua A", "100", "Centro", "Sao Paulo", "SP", "", "Senha@123");
        when(clientRepository.existsByPhone(request.phone())).thenReturn(false);
        when(clientRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @Test
    void shouldAuthenticateClient() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        Client client = client("Maria", "maria@email.com", "11988888888");
        client.setPasswordHash(passwordHashService.hash("Senha@123"));
        when(clientRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(client));
        when(orderRepository.summarizeByClientIds(List.of(client.getId())))
                .thenReturn(List.of(summary(client.getId(), 1, "10.00")));

        ClientResponse response = service.authenticate(new AuthenticateClientRequest("maria@email.com", "Senha@123"));

        assertThat(response.email()).isEqualTo("maria@email.com");
        assertThat(response.totalOrders()).isEqualTo(1);
        assertThat(response.totalSpent()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldRejectAuthenticationWhenEmailDoesNotExist() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        when(clientRepository.findByEmail("maria@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(new AuthenticateClientRequest("maria@email.com", "Senha@123")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void shouldRejectAuthenticationWhenPasswordIsInvalid() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        Client client = client("Maria", "maria@email.com", "11988888888");
        client.setPasswordHash(passwordHashService.hash("Outra@123"));
        when(clientRepository.findByEmail("maria@email.com")).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.authenticate(new AuthenticateClientRequest("maria@email.com", "Senha@123")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    void shouldGetAllClientsWithoutSearch() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        Client client = client("Ana", "ana@email.com", "11977777777");
        when(clientRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(client)));
        when(orderRepository.summarizeByClientIds(List.of(client.getId())))
                .thenReturn(List.of(summary(client.getId(), 2, "20.00")));

        PaginatedResult<ClientResponse> result = service.getAll(1, 5, null);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().totalOrders()).isEqualTo(2);
        assertThat(result.items().getFirst().totalSpent()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldGetAllClientsWithSearch() {
        ClientService service = new ClientService(clientRepository, orderRepository, passwordHashService);
        Client client = client("Ana", "ana@email.com", "11977777777");
        when(clientRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                eq("ana"), eq("ana"), eq("ana"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(client)));
        when(orderRepository.summarizeByClientIds(List.of(client.getId()))).thenReturn(List.of());

        PaginatedResult<ClientResponse> result = service.getAll(2, 5, "ana");

        assertThat(result.page()).isEqualTo(2);
        assertThat(result.items()).hasSize(1);
    }

    private Client client(String name, String email, String phone) {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);
        client.setZipCode("01234-567");
        client.setStreet("Rua A");
        client.setNumber("100");
        client.setNeighborhood("Centro");
        client.setCity("Sao Paulo");
        client.setState("SP");
        client.setComplement("Apto 10");
        client.setRegisteredAt(LocalDate.of(2026, 6, 16));
        return client;
    }

    private OrderRepository.ClientOrderSummary summary(UUID clientId, long totalOrders, String totalSpent) {
        return new OrderRepository.ClientOrderSummary() {
            @Override
            public UUID getClientId() {
                return clientId;
            }

            @Override
            public long getTotalOrders() {
                return totalOrders;
            }

            @Override
            public BigDecimal getTotalSpent() {
                return new BigDecimal(totalSpent);
            }
        };
    }
}
