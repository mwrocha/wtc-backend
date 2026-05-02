package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "campaigns")
public class Campaign {

    @Id
    private String id;
    private String title;
    private String body;
    private String url;
    private List<Message.ActionButton> actions;
    private Map<String, String> actionUrls;
    private CampaignStatus status;
    private List<String> targetTags;
    private List<String> targetClientIds;
    private String targetGroupId;
    private String targetDivisionId;   // ← novo
    private String createdByUserId;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;

    public Campaign() {
    }

    public Campaign(String id, String title, String body, String url, List<Message.ActionButton> actions, Map<String, String> actionUrls, CampaignStatus status, List<String> targetTags, List<String> targetClientIds, String targetGroupId, String targetDivisionId, String createdByUserId, LocalDateTime scheduledAt, LocalDateTime sentAt, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.url = url;
        this.actions = actions;
        this.actionUrls = actionUrls;
        this.status = status;
        this.targetTags = targetTags;
        this.targetClientIds = targetClientIds;
        this.targetGroupId = targetGroupId;
        this.targetDivisionId = targetDivisionId;
        this.createdByUserId = createdByUserId;
        this.scheduledAt = scheduledAt;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return url;
    }

    public List<Message.ActionButton> getActions() {
        return actions;
    }

    public Map<String, String> getActionUrls() {
        return actionUrls;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public List<String> getTargetTags() {
        return targetTags;
    }

    public List<String> getTargetClientIds() {
        return targetClientIds;
    }

    public String getTargetGroupId() {
        return targetGroupId;
    }

    public String getTargetDivisionId() {
        return targetDivisionId;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setActions(List<Message.ActionButton> actions) {
        this.actions = actions;
    }

    public void setActionUrls(Map<String, String> actionUrls) {
        this.actionUrls = actionUrls;
    }

    public void setStatus(CampaignStatus status) {
        this.status = status;
    }

    public void setTargetTags(List<String> targetTags) {
        this.targetTags = targetTags;
    }

    public void setTargetClientIds(List<String> t) {
        this.targetClientIds = t;
    }

    public void setTargetGroupId(String targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public void setTargetDivisionId(String targetDivisionId) {
        this.targetDivisionId = targetDivisionId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id, title, body, url, targetGroupId, targetDivisionId, createdByUserId;
        private List<Message.ActionButton> actions;
        private Map<String, String> actionUrls;
        private CampaignStatus status;
        private List<String> targetTags, targetClientIds;
        private LocalDateTime scheduledAt, sentAt, createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String t) {
            this.title = t;
            return this;
        }

        public Builder body(String b) {
            this.body = b;
            return this;
        }

        public Builder url(String u) {
            this.url = u;
            return this;
        }

        public Builder actions(List<Message.ActionButton> a) {
            this.actions = a;
            return this;
        }

        public Builder actionUrls(Map<String, String> a) {
            this.actionUrls = a;
            return this;
        }

        public Builder status(CampaignStatus s) {
            this.status = s;
            return this;
        }

        public Builder targetTags(List<String> t) {
            this.targetTags = t;
            return this;
        }

        public Builder targetClientIds(List<String> t) {
            this.targetClientIds = t;
            return this;
        }

        public Builder targetGroupId(String t) {
            this.targetGroupId = t;
            return this;
        }

        public Builder targetDivisionId(String t) {
            this.targetDivisionId = t;
            return this;
        }

        public Builder createdByUserId(String c) {
            this.createdByUserId = c;
            return this;
        }

        public Builder scheduledAt(LocalDateTime t) {
            this.scheduledAt = t;
            return this;
        }

        public Builder sentAt(LocalDateTime t) {
            this.sentAt = t;
            return this;
        }

        public Builder createdAt(LocalDateTime t) {
            this.createdAt = t;
            return this;
        }

        public Campaign build() {
            return new Campaign(id, title, body, url, actions, actionUrls, status, targetTags, targetClientIds, targetGroupId, targetDivisionId, createdByUserId, scheduledAt, sentAt, createdAt);
        }
    }

    public enum CampaignStatus {
        DRAFT, SCHEDULED, SENT, FAILED
    }
}