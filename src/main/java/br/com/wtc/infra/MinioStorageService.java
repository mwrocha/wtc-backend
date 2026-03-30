package br.com.wtc.infra;

import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    // Tipos permitidos (validação de segurança)
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    // Tamanho máximo: 5 MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final MinioClient minioClient;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.presigned-expiry-minutes:60}")
    private int presignedExpiryMinutes;

    public MinioStorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Faz o upload do arquivo para o MinIO e retorna a chave (objectName) gerada.
     * Lança IllegalArgumentException se o tipo ou tamanho for inválido.
     */
    public String upload(MultipartFile file) {
        // ── Validação de tipo ─────────────────────────────────────────────────
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido. Aceitos: JPEG, PNG, GIF, WEBP.");
        }

        // ── Validação de tamanho ──────────────────────────────────────────────
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Arquivo muito grande. Tamanho máximo: 5 MB.");
        }

        // ── Gera nome único para evitar colisão ───────────────────────────────
        String extension = getExtension(contentType);
        String objectName = "images/" + UUID.randomUUID() + extension;

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Upload concluído: {}", objectName);
            return objectName;

        } catch (Exception e) {
            log.error("Erro no upload MinIO: {}", e.getMessage());
            throw new RuntimeException("Falha ao fazer upload da imagem.", e);
        }
    }

    /**
     * Gera uma URL pré-assinada (GET) com validade configurável.
     * Permite acesso temporário sem expor credenciais.
     */
    public String generatePresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectName)
                            .expiry(presignedExpiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            log.error("Erro ao gerar URL pré-assinada: {}", e.getMessage());
            throw new RuntimeException("Falha ao gerar URL de acesso à imagem.", e);
        }
    }

    /**
     * Remove um objeto do bucket (ex.: ao excluir mensagem com imagem).
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("Objeto removido: {}", objectName);
        } catch (Exception e) {
            log.error("Erro ao remover objeto MinIO: {}", e.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/gif"  -> ".gif";
            case "image/webp" -> ".webp";
            default           -> "";
        };
    }
}