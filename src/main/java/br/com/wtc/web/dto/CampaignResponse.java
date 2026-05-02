package br.com.wtc.web.dto;

import br.com.wtc.domain.model.Campaign;
import br.com.wtc.domain.model.Message;

import java.time.LocalDateTime;
import java.util.List;

public record CampaignResponse(String id, String title, String body, String url, List<Message.ActionButton> actions,
                               String status, List<String> targetTags, List<String> targetClientIds,
                               String targetGroupId, LocalDateTime scheduledAt, LocalDateTime sentAt,
                               LocalDateTime createdAt) {
    // Factory method — converte Campaign (model) → CampaignResponse (DTO)
    public static CampaignResponse from(Campaign campaign) {
        return new CampaignResponse(campaign.getId(), campaign.getTitle(), campaign.getBody(), campaign.getUrl(), campaign.getActions(), campaign.getStatus().name(), campaign.getTargetTags(), campaign.getTargetClientIds(), campaign.getTargetGroupId(), campaign.getScheduledAt(), campaign.getSentAt(), campaign.getCreatedAt());
    }
}