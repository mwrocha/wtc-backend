package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "groups")
public class Group {

    @Id
    private String id;
    private String name;
    private String description;
    private String divisionId;
    private List<String> clientIds;
    private LocalDateTime createdAt;

    public Group() {}

    public Group(String id, String name, String description,
                 String divisionId, List<String> clientIds, LocalDateTime createdAt) {
        this.id = id; this.name = name; this.description = description;
        this.divisionId = divisionId; this.clientIds = clientIds;
        this.createdAt = createdAt;
    }

    public String getId()                   { return id; }
    public String getName()                 { return name; }
    public String getDescription()          { return description; }
    public String getDivisionId()           { return divisionId; }
    public List<String> getClientIds()      { return clientIds; }
    public LocalDateTime getCreatedAt()     { return createdAt; }

    public void setId(String id)                        { this.id = id; }
    public void setName(String name)                    { this.name = name; }
    public void setDescription(String description)      { this.description = description; }
    public void setDivisionId(String divisionId)        { this.divisionId = divisionId; }
    public void setClientIds(List<String> clientIds)    { this.clientIds = clientIds; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, name, description, divisionId;
        private List<String> clientIds;
        private LocalDateTime createdAt;

        public Builder id(String id)                { this.id = id; return this; }
        public Builder name(String name)            { this.name = name; return this; }
        public Builder description(String d)        { this.description = d; return this; }
        public Builder divisionId(String d)         { this.divisionId = d; return this; }
        public Builder clientIds(List<String> c)    { this.clientIds = c; return this; }
        public Builder createdAt(LocalDateTime t)   { this.createdAt = t; return this; }

        public Group build() {
            return new Group(id, name, description, divisionId, clientIds, createdAt);
        }
    }
}