package br.com.cardapioonline.application.common;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.cardapioonline.domain.enums.ProductCategory;
import org.junit.jupiter.api.Test;

class EnumMapperTest {

    @Test
    void shouldParseEnumIgnoringCase() {
        ProductCategory result = EnumMapper.parseIgnoreCase(ProductCategory.class, "hamburguer", ProductCategory.OUTRO);

        assertThat(result).isEqualTo(ProductCategory.HAMBURGUER);
    }

    @Test
    void shouldReturnFallbackWhenValueIsNullOrBlank() {
        assertThat(EnumMapper.parseIgnoreCase(ProductCategory.class, null, ProductCategory.OUTRO))
                .isEqualTo(ProductCategory.OUTRO);
        assertThat(EnumMapper.parseIgnoreCase(ProductCategory.class, "   ", ProductCategory.OUTRO))
                .isEqualTo(ProductCategory.OUTRO);
    }

    @Test
    void shouldReturnFallbackWhenValueDoesNotExist() {
        ProductCategory result = EnumMapper.parseIgnoreCase(ProductCategory.class, "massa", ProductCategory.OUTRO);

        assertThat(result).isEqualTo(ProductCategory.OUTRO);
    }
}
