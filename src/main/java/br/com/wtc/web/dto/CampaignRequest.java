package br.com.wtc.web.dto;

import br.com.wtc.domain.model.Message;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CampaignRequest(

        @NotBlank(message = "Título é obrigatório") String title,

        @NotBlank(message = "Corpo da mensagem é obrigatório") String body,

        String url,

        List<Message.ActionButton> actions, Map<String, String> actionUrls,

        // Segmentação
        List<String> targetTags, List<String> targetClientIds, String targetGroupId, String targetDivisionId,
        // ← novo

        LocalDateTime scheduledAt) {
}