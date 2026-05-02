package br.com.wtc.web.controller;

import br.com.wtc.infra.MinioStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * POST /api/upload/image
 * <p>
 * Recebe um arquivo multipart/form-data (campo "file"),
 * valida tipo e tamanho, faz upload no MinIO e retorna
 * a chave do objeto + URL pré-assinada para exibição imediata.
 * <p>
 * Apenas usuários autenticados podem fazer upload.
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final MinioStorageService storageService;

    public UploadController(MinioStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Arquivo não enviado ou vazio."));
        }

        try {
            // 1. Faz upload e obtém a chave do objeto no MinIO
            String objectKey = storageService.upload(file);

            // 2. Gera URL pré-assinada para acesso temporário
            String presignedUrl = storageService.generatePresignedUrl(objectKey);

            // 3. Retorna ambos: chave (para salvar na mensagem) + URL (para exibir no app)
            return ResponseEntity.ok(Map.of("objectKey", objectKey, "url", presignedUrl, "contentType", file.getContentType(), "size", file.getSize()));

        } catch (IllegalArgumentException e) {
            // Tipo ou tamanho inválido
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro interno ao fazer upload."));
        }
    }

    /**
     * GET /api/upload/presigned?key=images/uuid.jpg
     * <p>
     * Gera (ou renova) uma URL pré-assinada para um objeto já existente.
     * Útil para recarregar imagens cujas URLs expiraram no app.
     */
    @GetMapping("/presigned")
    public ResponseEntity<?> getPresignedUrl(@RequestParam("key") String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Parâmetro 'key' é obrigatório."));
        }
        try {
            String url = storageService.generatePresignedUrl(objectKey);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao gerar URL."));
        }
    }
}