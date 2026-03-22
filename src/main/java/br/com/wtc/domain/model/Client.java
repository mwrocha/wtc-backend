package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "clients")
public class Client {

    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String phone;
    private String status;
    private int score;
    private List<String> tags;
    private String divisionId;
    private String groupId;
    private List<String> noteIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Client() {}

    public Client(String id, String name, String email, String phone,
                  String status, int score, List<String> tags,
                  String divisionId, String groupId, List<String> noteIds,
                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id; this.name = name; this.email = email;
        this.phone = phone; this.status = status; this.score = score;
        this.tags = tags; this.divisionId = divisionId;
        this.groupId = groupId; this.noteIds = noteIds;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public String getId()                   { return id; }
    public String getName()                 { return name; }
    public String getEmail()                { return email; }
    public String getPhone()                { return phone; }
    public String getStatus()               { return status; }
    public int getScore()                   { return score; }
    public List<String> getTags()           { return tags; }
    public String getDivisionId()           { return divisionId; }
    public String getGroupId()              { return groupId; }
    public List<String> getNoteIds()        { return noteIds; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getUpdatedAt()     { return updatedAt; }

    public void setId(String id)                        { this.id = id; }
    public void setName(String name)                    { this.name = name; }
    public void setEmail(String email)                  { this.email = email; }
    public void setPhone(String phone)                  { this.phone = phone; }
    public void setStatus(String status)                { this.status = status; }
    public void setScore(int score)                     { this.score = score; }
    public void setTags(List<String> tags)              { this.tags = tags; }
    public void setDivisionId(String divisionId)        { this.divisionId = divisionId; }
    public void setGroupId(String groupId)              { this.groupId = groupId; }
    public void setNoteIds(List<String> noteIds)        { this.noteIds = noteIds; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id, name, email, phone, status, divisionId, groupId;
        private int score;
        private List<String> tags, noteIds;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(String id)                    { this.id = id; return this; }
        public Builder name(String name)                { this.name = name; return this; }
        public Builder email(String email)              { this.email = email; return this; }
        public Builder phone(String phone)              { this.phone = phone; return this; }
        public Builder status(String status)            { this.status = status; return this; }
        public Builder score(int score)                 { this.score = score; return this; }
        public Builder tags(List<String> tags)          { this.tags = tags; return this; }
        public Builder divisionId(String d)             { this.divisionId = d; return this; }
        public Builder groupId(String g)                { this.groupId = g; return this; }
        public Builder noteIds(List<String> n)          { this.noteIds = n; return this; }
        public Builder createdAt(LocalDateTime t)       { this.createdAt = t; return this; }
        public Builder updatedAt(LocalDateTime t)       { this.updatedAt = t; return this; }

        public Client build() {
            return new Client(id, name, email, phone, status, score,
                    tags, divisionId, groupId, noteIds,
                    createdAt, updatedAt);
        }
    }
}