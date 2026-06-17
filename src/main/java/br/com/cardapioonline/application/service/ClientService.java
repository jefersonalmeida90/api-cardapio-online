package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.ClientDtos.AuthenticateClientRequest;
import br.com.cardapioonline.application.dto.ClientDtos.ClientResponse;
import br.com.cardapioonline.application.dto.ClientDtos.CreateClientRequest;
import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.common.PasswordHashService;
import br.com.cardapioonline.domain.entity.Client;
import br.com.cardapioonline.application.port.out.ClientRepository;
import br.com.cardapioonline.application.port.out.OrderRepository;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private static final DateTimeFormatter REGISTERED_AT_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final PasswordHashService passwordHashService;

    public ClientService(ClientRepository clientRepository, OrderRepository orderRepository, PasswordHashService passwordHashService) {
        this.clientRepository = clientRepository;
        this.orderRepository = orderRepository;
        this.passwordHashService = passwordHashService;
    }

    public ClientResponse create(CreateClientRequest request) {
        if (clientRepository.existsByPhone(request.phone())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Ja existe um cliente cadastrado com este telefone.");
        }
        if (clientRepository.existsByEmail(request.email())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Ja existe um cliente cadastrado com este e-mail.");
        }

        Client client = new Client();
        client.setName(request.name());
        client.setEmail(request.email());
        client.setPhone(request.phone());
        client.setZipCode(request.zipCode());
        client.setStreet(request.street());
        client.setNumber(request.number());
        client.setNeighborhood(request.neighborhood());
        client.setCity(request.city());
        client.setState(request.state());
        client.setComplement(request.complement() == null ? "" : request.complement());
        client.setPasswordHash(passwordHashService.hash(request.password()));
        Client savedClient = clientRepository.save(client);
        return toResponse(savedClient, Map.of());
    }

    public ClientResponse authenticate(AuthenticateClientRequest request) {
        Client client = clientRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos."));
        if (!passwordHashService.verify(request.password(), client.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos.");
        }
        return toResponse(client, loadOrderSummaries(List.of(client)));
    }

    public PaginatedResult<ClientResponse> getAll(int page, int pageSize, String search) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by("registeredAt").descending());
        Page<Client> result = (search == null || search.isBlank())
                ? clientRepository.findAll(pageable)
                : clientRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(search, search, search, pageable);
        Map<UUID, OrderRepository.ClientOrderSummary> summaries = loadOrderSummaries(result.getContent());
        return PaginatedResult.of(result.map(client -> toResponse(client, summaries)).getContent(), page, pageSize, result.getTotalElements());
    }

    private Map<UUID, OrderRepository.ClientOrderSummary> loadOrderSummaries(Collection<Client> clients) {
        List<UUID> clientIds = clients.stream()
                .map(Client::getId)
                .filter(id -> id != null)
                .toList();
        if (clientIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, OrderRepository.ClientOrderSummary> summaries = new HashMap<>();
        orderRepository.summarizeByClientIds(clientIds)
                .forEach(summary -> summaries.put(summary.getClientId(), summary));
        return summaries;
    }

    private ClientResponse toResponse(Client client, Map<UUID, OrderRepository.ClientOrderSummary> summaries) {
        OrderRepository.ClientOrderSummary summary = summaries.get(client.getId());
        int totalOrders = summary == null ? 0 : Math.toIntExact(summary.getTotalOrders());
        BigDecimal totalSpent = summary == null ? BigDecimal.ZERO : summary.getTotalSpent();
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getZipCode(),
                client.getStreet(),
                client.getNumber(),
                client.getNeighborhood(),
                client.getCity(),
                client.getState(),
                client.getComplement(),
                formatAddress(client),
                client.getRegisteredAt().format(REGISTERED_AT_FORMAT),
                totalOrders,
                totalSpent
        );
    }

    private String formatAddress(Client client) {
        String complement = client.getComplement() == null || client.getComplement().isBlank() ? "" : " - " + client.getComplement();
        return "%s, %s - %s - %s/%s - CEP %s%s".formatted(
                client.getStreet(),
                client.getNumber(),
                client.getNeighborhood(),
                client.getCity(),
                client.getState(),
                client.getZipCode(),
                complement
        );
    }
}
