package br.com.cardapioonline.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.port.out.EstabelecimentoRepository;
import br.com.cardapioonline.application.port.out.ProductRepository;
import br.com.cardapioonline.domain.entity.Product;
import br.com.cardapioonline.domain.enums.ProductCategory;
import br.com.cardapioonline.infrastructure.security.JwtService;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.Instant;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAndPersistenceIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Test
    void shouldApplyLiquibaseAndExposeTables() {
        Integer changelogCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM DATABASECHANGELOG", Integer.class);
        Integer productsTableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Products'",
                Integer.class
        );

        assertThat(changelogCount).isGreaterThan(0);
        assertThat(productsTableCount).isEqualTo(1);
    }

    @Test
    void shouldPersistProductUsingLiquibaseSchema() {
        Product product = new Product();
        product.setName("Produto Integracao");
        product.setDescription("Criado em teste");
        product.setPrice(new BigDecimal("9.90"));
        product.setCategory(ProductCategory.BEBIDA);
        product.setImageUrl("/img.png");
        product.setActive(true);
        product.setCreatedAt(Instant.now());

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(productRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldAllowPublicEndpointAndProtectPrivateEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/estabelecimento"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/estabelecimento")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Loja Segura",
                                  "logoUrl": "",
                                  "category": "",
                                  "address": "",
                                  "whatsapp": "11999999999",
                                  "openTime": "09:00",
                                  "closeTime": "22:00"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowProtectedEndpointWithValidJwt() throws Exception {
        String token = jwtService.generateToken("admin@cardapioonline.local");

        mockMvc.perform(put("/api/estabelecimento")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Loja Segura",
                                  "logoUrl": "",
                                  "category": "",
                                  "address": "",
                                  "whatsapp": "11999999999",
                                  "openTime": "09:00",
                                  "closeTime": "22:00"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Loja Segura"));

        assertThat(estabelecimentoRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldAllowPublicUploadEndpointWithoutToken() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", pngBytes());

        mockMvc.perform(multipart("/api/uploads/image")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());
    }

    private byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
