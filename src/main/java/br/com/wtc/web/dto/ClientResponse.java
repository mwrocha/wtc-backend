package br.com.wtc.web.dto;

import br.com.wtc.domain.model.Client;

import java.time.LocalDateTime;
import java.util.List;

public record ClientResponse(
        String id,
        String name,
        String email,
        String phone,
        String status,
        int score,
        List<String> tags,
        String divisionId,
        String groupId,
        LocalDateTime createdAt
) {
    // Factory method — converte Client (model) → ClientResponse (DTO)
    // Nunca exponha o @Document diretamente no endpoint
    public static ClientResponse from(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getPhone(),
                client.getStatus(),
                client.getScore(),
                client.getTags(),
                client.getDivisionId(),
                client.getGroupId(),
                client.getCreatedAt()
        );
    }
}