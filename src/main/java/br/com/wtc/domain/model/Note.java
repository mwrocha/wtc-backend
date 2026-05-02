package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notes")
public class Note {

    @Id
    private String id;
    private String clientId;
    private String operatorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Note() {
    }

    public Note(String id, String clientId, String operatorId, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.operatorId = operatorId;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getClientId() {
        return clientId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id, clientId, operatorId, content;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder clientId(String c) {
            this.clientId = c;
            return this;
        }

        public Builder operatorId(String o) {
            this.operatorId = o;
            return this;
        }

        public Builder content(String c) {
            this.content = c;
            return this;
        }

        public Builder createdAt(LocalDateTime t) {
            this.createdAt = t;
            return this;
        }

        public Builder updatedAt(LocalDateTime t) {
            this.updatedAt = t;
            return this;
        }

        public Note build() {
            return new Note(id, clientId, operatorId, content, createdAt, updatedAt);
        }
    }
}