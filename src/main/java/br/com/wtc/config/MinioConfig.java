package br.com.wtc.config;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    // Ex: https://<ACCOUNT_ID>.r2.cloudflarestorage.com
    @Value("${r2.endpoint}")
    private String endpoint;

    // R2 Access Key ID (gerado no painel da Cloudflare)
    @Value("${r2.access-key}")
    private String accessKey;

    // R2 Secret Access Key (gerado no painel da Cloudflare)
    @Value("${r2.secret-key}")
    private String secretKey;

    @Value("${r2.bucket}")
    private String bucket;

    @Bean
    public MinioClient minioClient() {
        // O SDK do MinIO é 100% compatível com a API S3 do Cloudflare R2
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        // Verifica se o bucket existe e se a conexão está funcionando
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (exists) {
                log.info("Conectado ao bucket R2 '{}' com sucesso.", bucket);
            } else {
                log.warn("Bucket '{}' não encontrado no R2. Crie-o pelo painel da Cloudflare.", bucket);
            }
        } catch (Exception e) {
            log.error("Erro ao conectar ao Cloudflare R2: {}", e.getMessage());
        }

        return client;
    }
}