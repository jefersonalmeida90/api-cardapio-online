package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.ProductDtos.ProductRequest;
import br.com.cardapioonline.application.dto.ProductDtos.ProductResponse;
import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.common.EnumMapper;
import br.com.cardapioonline.application.common.PaginatedResult;
import br.com.cardapioonline.domain.entity.Product;
import br.com.cardapioonline.domain.enums.ProductCategory;
import br.com.cardapioonline.application.port.out.ProductRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public PaginatedResult<ProductResponse> getAll(int page, int pageSize, String category) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), pageSize, Sort.by("createdAt").ascending());
        ProductCategory parsed = EnumMapper.parseIgnoreCase(ProductCategory.class, category, null);
        Page<Product> result = parsed == null
                ? repository.findByActiveTrue(pageable)
                : repository.findByActiveTrueAndCategory(parsed, pageable);
        return PaginatedResult.of(result.map(this::toResponse).getContent(), page, pageSize, result.getTotalElements());
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return toResponse(repository.save(product));
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product %s not found.".formatted(id)));
        apply(product, request);
        return toResponse(repository.save(product));
    }

    public void delete(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product %s not found.".formatted(id)));
        product.setActive(false);
        repository.save(product);
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description() == null ? "" : request.description());
        product.setPrice(request.price());
        product.setCategory(EnumMapper.parseIgnoreCase(ProductCategory.class, request.category(), ProductCategory.OUTRO));
        product.setImageUrl(request.imageUrl() == null ? "" : request.imageUrl());
        product.setActive(true);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().name().toLowerCase(Locale.ROOT),
                product.getImageUrl()
        );
    }
}
