package br.com.cardapioonline.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.cardapioonline.application.dto.UploadResponse;
import br.com.cardapioonline.application.service.UploadService;
import br.com.cardapioonline.infrastructure.security.JwtService;
import br.com.cardapioonline.presentation.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UploadsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class UploadsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadService uploadService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldUploadImage() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.png", "image/png", "conteudo".getBytes());
        when(uploadService.uploadImage(any())).thenReturn(new UploadResponse("http://localhost:8080/uploads/20260616/foto.png"));

        mockMvc.perform(multipart("/api/uploads/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:8080/uploads/20260616/foto.png"));
    }
}
