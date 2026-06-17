package br.com.cardapioonline.presentation.controller;

import br.com.cardapioonline.application.dto.UploadResponse;
import br.com.cardapioonline.application.service.UploadService;
import java.io.IOException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadsController {

    private final UploadService uploadService;

    public UploadsController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/image")
    public UploadResponse uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        return uploadService.uploadImage(file);
    }
}
