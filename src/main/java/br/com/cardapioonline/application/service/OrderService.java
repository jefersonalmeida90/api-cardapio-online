package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.OrderDtos.CreateOrderItemRequest;
import br.com.cardapioonline.application.dto.OrderDtos.CreateOrderRequest;
import br.com.cardapioonline.application.dto.OrderDtos.OrderItemResponse;
import br.com.cardapioonline.application.dto.OrderDtos.OrderResponse;
import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.common.EnumMapper;
import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.domain.entity.Client;
import br.com.cardapioonline.domain.entity.Order;
import br.com.cardapioonline.domain.entity.OrderItem;
import br.com.cardapioonline.domain.entity.Product;
import br.com.cardapioonline.domain.enums.OrderSource;
import br.com.cardapioonline.domain.enums.OrderStatus;
import br.com.cardapioonline.application.port.out.ClientRepository;
import br.com.cardapioonline.application.port.out.OrderRepository;
import br.com.cardapioonline.application.port.out.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final AtomicInteger ORDER_SEQUENCE = new AtomicInteger();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter CREATED_AT_FORMAT = DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneOffset.UTC);
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, ClientRepository clientRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.clientRepository = clientRepository;
    }

    public PaginatedResult<OrderResponse> getAll(int page, int pageSize, LocalDate date) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by("createdAt").descending());
        Page<Order> result = date == null ? orderRepository.findAll(pageable) : orderRepository.findAllByDate(date, pageable);
        return PaginatedResult.of(result.map(this::toResponse).getContent(), page, pageSize, result.getTotalElements());
    }

    public OrderResponse create(CreateOrderRequest request) {
        Client client = clientRepository.findByPhone(request.clientPhone()).orElse(null);
        if (client != null) {
            client.setName(request.clientName());
        }

        Map<UUID, Product> productsById = loadProducts(request.items());
        Order order = new Order();
        order.setNumber(generateOrderNumber());
        order.setClient(client);
        order.setClientName(request.clientName());
        order.setClientPhone(request.clientPhone());
        order.setAddress(request.address());
        order.setSource(EnumMapper.parseIgnoreCase(OrderSource.class, request.source(), OrderSource.SITE));
        order.setStatus(OrderStatus.PENDENTE);
        order.setNote(request.note());

        List<OrderItem> items = request.items().stream().map(item -> toOrderItem(order, item, productsById.get(item.productId()))).toList();
        order.setItems(items);
        order.setTotal(items.stream().map(OrderItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));

        return toResponse(orderRepository.save(order));
    }

    public OrderResponse advance(UUID id) {
        Order order = findOrder(id);
        order.setStatus(nextStatus(order.getStatus()));
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse cancel(UUID id) {
        Order order = findOrder(id);
        order.setStatus(OrderStatus.CANCELADO);
        return toResponse(orderRepository.save(order));
    }

    private Order findOrder(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order %s not found.".formatted(id)));
    }

    private Map<UUID, Product> loadProducts(List<CreateOrderItemRequest> items) {
        List<UUID> productIds = items.stream().map(CreateOrderItemRequest::productId).distinct().toList();
        Map<UUID, Product> products = new HashMap<>();
        productRepository.findAllById(productIds).stream()
                .filter(Product::isActive)
                .forEach(product -> products.put(product.getId(), product));

        for (CreateOrderItemRequest item : items) {
            if (!products.containsKey(item.productId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Produto '%s' nao encontrado.".formatted(item.productId()));
            }
        }
        return products;
    }

    private OrderItem toOrderItem(Order order, CreateOrderItemRequest request, Product product) {
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProductName(product.getName());
        item.setQuantity(request.quantity());
        item.setUnitPrice(product.getPrice());
        return item;
    }

    private OrderStatus nextStatus(OrderStatus status) {
        return switch (status) {
            case PENDENTE -> OrderStatus.EM_PREPARO;
            case EM_PREPARO -> OrderStatus.EM_ENTREGA;
            case EM_ENTREGA -> OrderStatus.ENTREGUE;
            case ENTREGUE -> OrderStatus.ENTREGUE;
            case CANCELADO -> throw new ApiException(HttpStatus.CONFLICT, "Pedido cancelado nao pode avancar.");
        };
    }

    private String generateOrderNumber() {
        int sequence = ORDER_SEQUENCE.incrementAndGet() % 1000;
        return "P" + java.time.LocalTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HHmmss")) + "%03d".formatted(sequence);
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getNumber(),
                order.getClientName(),
                order.getClientPhone(),
                order.getAddress(),
                order.getTotal(),
                order.getStatus().name(),
                order.getDate().format(DATE_FORMAT),
                CREATED_AT_FORMAT.format(order.getCreatedAt()),
                order.getSource().name(),
                order.getNote(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(item.getProductName(), item.getQuantity(), item.getUnitPrice(), item.getSubtotal()))
                        .toList()
        );
    }
}
