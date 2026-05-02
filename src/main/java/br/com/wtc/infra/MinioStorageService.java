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

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf"   // ← PDF adicionado
    );

    // Aumentado para 10 MB para comportar PDFs
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

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
     * Aceita imagens (JPEG, PNG, GIF, WEBP) e PDFs.
     */
    public String upload(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Tipo de arquivo não permitido. Aceitos: JPEG, PNG, GIF, WEBP, PDF.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 10 MB.");
        }

        String extension = getExtension(contentType);
        String objectName = "images/" + UUID.randomUUID() + extension;

        try {
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectName).stream(file.getInputStream(), file.getSize(), -1).contentType(contentType).build());
            log.info("Upload concluído: {}", objectName);
            return objectName;

        } catch (Exception e) {
            log.error("Erro no upload MinIO: {}", e.getMessage());
            throw new RuntimeException("Falha ao fazer upload do arquivo.", e);
        }
    }

    /**
     * Gera uma URL pré-assinada (GET) com validade configurável.
     */
    public String generatePresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET).bucket(bucket).object(objectName).expiry(presignedExpiryMinutes, TimeUnit.MINUTES).build());
        } catch (Exception e) {
            log.error("Erro ao gerar URL pré-assinada: {}", e.getMessage());
            throw new RuntimeException("Falha ao gerar URL de acesso ao arquivo.", e);
        }
    }

    /**
     * Remove um objeto do bucket.
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectName).build());
            log.info("Objeto removido: {}", objectName);
        } catch (Exception e) {
            log.error("Erro ao remover objeto MinIO: {}", e.getMessage());
        }
    }

    private String getExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}