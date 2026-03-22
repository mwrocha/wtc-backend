package br.com.wtc.web.dto;

import br.com.wtc.domain.model.Message;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CampaignRequest(

        @NotBlank(message = "Título é obrigatório")
        String title,

        @NotBlank(message = "Corpo da mensagem é obrigatório")
        String body,

        String url,

        // Botões interativos — ex: [{"action":"btn1","title":"Inscrever-se"}]
        List<Message.ActionButton> actions,

        // Mapa de URLs por botão — ex: {"btn1":"https://wtc.com/inscricao"}
        Map<String, String> actionUrls,

        // Segmentação — pelo menos um dos três deve ser informado
        List<String> targetTags,
        List<String> targetClientIds,
        String targetGroupId,

        // null = disparo imediato, data futura = agendado
        LocalDateTime scheduledAt
) {}