package br.com.cardapioonline.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.application.dto.UploadResponse;
import br.com.cardapioonline.infrastructure.config.ApiProperties;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

class UploadServiceTest {
    private final UploadService service = new UploadService(new ApiProperties("http://localhost:8080"));

    @AfterEach
    void cleanupUploads() throws IOException {
        Path uploadsPath = Path.of("uploads");
        if (Files.exists(uploadsPath)) {
            try (var paths = Files.walk(uploadsPath)) {
                paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
            }
        }
    }

    @Test
    void shouldRejectMissingFile() {
        assertThatThrownBy(() -> service.uploadImage(null))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldRejectNonImageFile() {
        MockMultipartFile file = new MockMultipartFile("file", "arquivo.txt", "text/plain", "abc".getBytes());

        assertThatThrownBy(() -> service.uploadImage(file))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldUploadImageUsingSafeExtensionFromContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.php", "image/png", pngBytes());

        UploadResponse response = service.uploadImage(file);

        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = response.url().substring(response.url().lastIndexOf('/') + 1);
        Path storedFile = Path.of("uploads", dateFolder, fileName);

        assertThat(response.url()).startsWith("http://localhost:8080/uploads/" + dateFolder + "/");
        assertThat(fileName).endsWith(".png");
        assertThat(Files.exists(storedFile)).isTrue();
    }

    @Test
    void shouldRejectInvalidImagePayload() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "conteudo".getBytes());

        assertThatThrownBy(() -> service.uploadImage(file))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void shouldRejectUnsupportedImageType() {
        MockMultipartFile file = new MockMultipartFile("file", "foto.svg", "image/svg+xml", "<svg/>".getBytes());

        assertThatThrownBy(() -> service.uploadImage(file))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    private byte[] pngBytes() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}
