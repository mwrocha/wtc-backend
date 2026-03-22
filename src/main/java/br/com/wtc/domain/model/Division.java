package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "divisions")
public class Division {

    @Id
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public Division() {}

    public Division(String id, String name, String description, LocalDateTime createdAt) {
        this.id = id; this.name = name;
        this.description = description; this.createdAt = createdAt;
    }

    public String getId()               { return id; }
    public String getName()             { return name; }
    public String getDescription()      { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(String id)                      { this.id = id; }
    public void setName(String name)                  { this.name = name; }
    public void setDescription(String description)    { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, name, description;
        private LocalDateTime createdAt;

        public Builder id(String id)              { this.id = id; return this; }
        public Builder name(String name)          { this.name = name; return this; }
        public Builder description(String d)      { this.description = d; return this; }
        public Builder createdAt(LocalDateTime t) { this.createdAt = t; return this; }

        public Division build() {
            return new Division(id, name, description, createdAt);
        }
    }
}