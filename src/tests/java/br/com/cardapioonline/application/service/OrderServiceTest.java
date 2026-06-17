package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.dto.OrderDtos.CreateOrderItemRequest;
import br.com.cardapioonline.application.dto.OrderDtos.CreateOrderRequest;
import br.com.cardapioonline.application.dto.OrderDtos.OrderResponse;
import br.com.cardapioonline.application.port.out.ClientRepository;
import br.com.cardapioonline.application.port.out.OrderRepository;
import br.com.cardapioonline.application.port.out.ProductRepository;
import br.com.cardapioonline.domain.entity.Client;
import br.com.cardapioonline.domain.entity.Order;
import br.com.cardapioonline.domain.entity.Product;
import br.com.cardapioonline.domain.enums.OrderSource;
import br.com.cardapioonline.domain.enums.OrderStatus;
import br.com.cardapioonline.domain.enums.ProductCategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ClientRepository clientRepository;

    @Test
    void shouldListOrdersWithoutDateFilter() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        Order order = order(OrderStatus.PENDENTE);
        when(orderRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(order)));

        PaginatedResult<OrderResponse> result = service.getAll(1, 5, null);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().status()).isEqualTo("PENDENTE");
    }

    @Test
    void shouldListOrdersWithDateFilter() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        LocalDate date = LocalDate.of(2026, 6, 16);
        when(orderRepository.findAllByDate(eq(date), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(order(OrderStatus.ENTREGUE))));

        PaginatedResult<OrderResponse> result = service.getAll(1, 5, date);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().date()).isEqualTo("2026-06-16");
    }

    @Test
    void shouldCreateOrderUsingExistingClientAndProducts() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID productId = UUID.randomUUID();
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Cliente Antigo");
        when(clientRepository.findByPhone("11999999999")).thenReturn(Optional.of(client));
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(activeProduct(productId, "Burger", "15.00")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            order.setDate(LocalDate.of(2026, 6, 16));
            order.setCreatedAt(Instant.parse("2026-06-16T12:00:00Z"));
            return order;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                "Cliente Novo",
                "11999999999",
                "Rua A, 100",
                "site",
                List.of(new CreateOrderItemRequest(productId, 2)),
                "Sem cebola"
        );

        OrderResponse response = service.create(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order saved = captor.getValue();
        assertThat(client.getName()).isEqualTo("Cliente Novo");
        assertThat(saved.getClient()).isEqualTo(client);
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDENTE);
        assertThat(saved.getSource()).isEqualTo(OrderSource.SITE);
        assertThat(saved.getNumber()).startsWith("P").hasSize(10);
        assertThat(response.total()).isEqualByComparingTo("30.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().subtotal()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldUseFallbackSourceWhenSourceIsInvalid() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID productId = UUID.randomUUID();
        when(clientRepository.findByPhone("11999999999")).thenReturn(Optional.empty());
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of(activeProduct(productId, "Burger", "10.00")));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(UUID.randomUUID());
            order.setDate(LocalDate.of(2026, 6, 16));
            order.setCreatedAt(Instant.parse("2026-06-16T12:00:00Z"));
            return order;
        });

        service.create(new CreateOrderRequest(
                "Cliente",
                "11999999999",
                "Rua A",
                "fonte-invalida",
                List.of(new CreateOrderItemRequest(productId, 1)),
                null
        ));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        assertThat(captor.getValue().getSource()).isEqualTo(OrderSource.SITE);
    }

    @Test
    void shouldRejectCreateWhenProductIsMissingOrInactive() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID productId = UUID.randomUUID();
        when(clientRepository.findByPhone("11999999999")).thenReturn(Optional.empty());
        when(productRepository.findAllById(List.of(productId))).thenReturn(List.of());

        assertThatThrownBy(() -> service.create(new CreateOrderRequest(
                "Cliente",
                "11999999999",
                "Rua A",
                "site",
                List.of(new CreateOrderItemRequest(productId, 1)),
                null
        )))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @ParameterizedTest
    @CsvSource({
            "PENDENTE,EM_PREPARO",
            "EM_PREPARO,EM_ENTREGA",
            "EM_ENTREGA,ENTREGUE",
            "ENTREGUE,ENTREGUE"
    })
    void shouldAdvanceOrderStatus(OrderStatus current, OrderStatus next) {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID id = UUID.randomUUID();
        Order order = order(current);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.advance(id);

        assertThat(response.status()).isEqualTo(next.name());
    }

    @Test
    void shouldRejectAdvanceWhenOrderDoesNotExist() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.advance(id))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldRejectAdvanceWhenOrderIsCancelled() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID id = UUID.randomUUID();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order(OrderStatus.CANCELADO)));

        assertThatThrownBy(() -> service.advance(id))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void shouldCancelOrder() {
        OrderService service = new OrderService(orderRepository, productRepository, clientRepository);
        UUID id = UUID.randomUUID();
        Order order = order(OrderStatus.PENDENTE);
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = service.cancel(id);

        assertThat(response.status()).isEqualTo("CANCELADO");
    }

    private Product activeProduct(UUID id, String name, String price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(new BigDecimal(price));
        product.setCategory(ProductCategory.HAMBURGUER);
        product.setActive(true);
        return product;
    }

    private Order order(OrderStatus status) {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setNumber("P123456001");
        order.setClientName("Cliente");
        order.setClientPhone("11999999999");
        order.setAddress("Rua A");
        order.setTotal(new BigDecimal("10.00"));
        order.setStatus(status);
        order.setDate(LocalDate.of(2026, 6, 16));
        order.setCreatedAt(Instant.parse("2026-06-16T12:00:00Z"));
        order.setSource(OrderSource.SITE);
        order.setItems(List.of());
        return order;
    }
}
