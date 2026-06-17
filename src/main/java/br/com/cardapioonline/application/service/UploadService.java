package br.com.cardapioonline.application.service;

import br.com.cardapioonline.application.dto.UploadResponse;
import br.com.cardapioonline.application.common.ApiException;
import br.com.cardapioonline.infrastructure.config.ApiProperties;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/gif", ".gif"
    );

    private final ApiProperties apiProperties;

    public UploadService(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    public UploadResponse uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Arquivo nao informado.");
        }
        String extension = validateAndResolveExtension(file);
        String subFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path targetFolder = Path.of("uploads", subFolder);
        Files.createDirectories(targetFolder);

        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        Path filePath = targetFolder.resolve(fileName);
        file.transferTo(filePath);

        return new UploadResponse(apiProperties.baseUrl() + "/uploads/" + subFolder + "/" + fileName);
    }

    private String validateAndResolveExtension(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Somente arquivos PNG, JPG ou GIF sao aceitos.");
        }
        String extension = ALLOWED_CONTENT_TYPES.get(contentType.toLowerCase(Locale.ROOT));
        if (extension == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Somente arquivos PNG, JPG ou GIF sao aceitos.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image;
            try {
                image = ImageIO.read(inputStream);
            } catch (IOException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Arquivo de imagem invalido.");
            }
            if (image == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Arquivo de imagem invalido.");
            }
        }

        return extension;
    }
}
