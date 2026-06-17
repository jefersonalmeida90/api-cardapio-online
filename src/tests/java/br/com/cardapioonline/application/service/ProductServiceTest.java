package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.application.dto.ProductDtos.ProductRequest;
import br.com.cardapioonline.application.dto.ProductDtos.ProductResponse;
import br.com.cardapioonline.application.port.out.ProductRepository;
import br.com.cardapioonline.domain.entity.Product;
import br.com.cardapioonline.domain.enums.ProductCategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @Test
    void shouldListActiveProductsWithoutCategoryFilter() {
        ProductService service = new ProductService(repository);
        Product product = product(ProductCategory.BEBIDA);
        when(repository.findByActiveTrue(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(product)));

        PaginatedResult<ProductResponse> result = service.getAll(1, 5, null);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().category()).isEqualTo("bebida");
    }

    @Test
    void shouldListActiveProductsWithCategoryFilter() {
        ProductService service = new ProductService(repository);
        Product product = product(ProductCategory.HAMBURGUER);
        when(repository.findByActiveTrueAndCategory(eq(ProductCategory.HAMBURGUER), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(product)));

        PaginatedResult<ProductResponse> result = service.getAll(1, 5, "hamburguer");

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().category()).isEqualTo("hamburguer");
    }

    @Test
    void shouldCreateProductUsingFallbackValues() {
        ProductService service = new ProductService(repository);
        ProductRequest request = new ProductRequest("Burger", null, new BigDecimal("25.00"), "categoria-invalida", null);
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(UUID.randomUUID());
            product.setCreatedAt(Instant.now());
            return product;
        });

        ProductResponse response = service.create(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDescription()).isEmpty();
        assertThat(captor.getValue().getImageUrl()).isEmpty();
        assertThat(captor.getValue().getCategory()).isEqualTo(ProductCategory.OUTRO);
        assertThat(response.name()).isEqualTo("Burger");
    }

    @Test
    void shouldUpdateExistingProduct() {
        ProductService service = new ProductService(repository);
        UUID id = UUID.randomUUID();
        Product product = product(ProductCategory.BEBIDA);
        when(repository.findById(id)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = service.update(id, new ProductRequest(
                "Novo Nome", "Descricao", new BigDecimal("8.50"), "bebida", "/img.png"));

        assertThat(response.name()).isEqualTo("Novo Nome");
        assertThat(response.imageUrl()).isEqualTo("/img.png");
    }

    @Test
    void shouldRejectUpdateWhenProductDoesNotExist() {
        ProductService service = new ProductService(repository);
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new ProductRequest("X", "", new BigDecimal("1.00"), "outro", "")))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void shouldSoftDeleteExistingProduct() {
        ProductService service = new ProductService(repository);
        UUID id = UUID.randomUUID();
        Product product = product(ProductCategory.BEBIDA);
        product.setActive(true);
        when(repository.findById(id)).thenReturn(Optional.of(product));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete(id);

        assertThat(product.isActive()).isFalse();
        verify(repository).save(product);
    }

    @Test
    void shouldRejectDeleteWhenProductDoesNotExist() {
        ProductService service = new ProductService(repository);
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private Product product(ProductCategory category) {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Produto");
        product.setDescription("Descricao");
        product.setPrice(new BigDecimal("7.50"));
        product.setCategory(category);
        product.setImageUrl("/img.png");
        product.setActive(true);
        product.setCreatedAt(Instant.now());
        return product;
    }
}
