package br.com.wtc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "br.com.wtc.domain.repository")
public class MongoConfig {
    // O Spring Boot autoconfigura o MongoDB pela URI do application.yml
    // Esta classe ativa auditoria (@CreatedDate, @LastModifiedDate)
    // e garante que os repositories são escaneados no pacote correto
}