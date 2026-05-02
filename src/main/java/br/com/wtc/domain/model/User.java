package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String password;
    private String name;
    private String role;
    private String fcmToken;
    private String avatarKey;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Sessão única — invalidação de login em outro aparelho ─────────────────
    private String sessionToken;

    // ── Campos exclusivos de CLIENT ───────────────────────────────────────────
    private String phone;
    private String cpf;
    private String company;
    private String status;
    private int score;
    private List<String> tags;
    private String divisionId;
    private String groupId;
    private List<String> noteIds;

    public User() {
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public String getAvatarKey() {
        return avatarKey;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getPhone() {
        return phone;
    }

    public String getCpf() {
        return cpf;
    }

    public String getCompany() {
        return company;
    }

    public String getStatus() {
        return status;
    }

    public int getScore() {
        return score;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getDivisionId() {
        return divisionId;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<String> getNoteIds() {
        return noteIds;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCreatedAt(LocalDateTime t) {
        this.createdAt = t;
    }

    public void setUpdatedAt(LocalDateTime t) {
        this.updatedAt = t;
    }

    public void setSessionToken(String s) {
        this.sessionToken = s;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setDivisionId(String divisionId) {
        this.divisionId = divisionId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setNoteIds(List<String> noteIds) {
        this.noteIds = noteIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id, email, password, name, role, fcmToken, avatarKey, sessionToken;
        private String phone, cpf, company, status, divisionId, groupId;
        private int score;
        private List<String> tags, noteIds;
        private boolean active;
        private LocalDateTime createdAt, updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder role(String role) {
            this.role = role;
            return this;
        }

        public Builder fcmToken(String t) {
            this.fcmToken = t;
            return this;
        }

        public Builder avatarKey(String a) {
            this.avatarKey = a;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
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

        public Builder sessionToken(String s) {
            this.sessionToken = s;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public Builder company(String company) {
            this.company = company;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder score(int score) {
            this.score = score;
            return this;
        }

        public Builder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder divisionId(String d) {
            this.divisionId = d;
            return this;
        }

        public Builder groupId(String g) {
            this.groupId = g;
            return this;
        }

        public Builder noteIds(List<String> n) {
            this.noteIds = n;
            return this;
        }

        public User build() {
            User u = new User();
            u.id = id;
            u.email = email;
            u.password = password;
            u.name = name;
            u.role = role;
            u.fcmToken = fcmToken;
            u.avatarKey = avatarKey;
            u.active = active;
            u.createdAt = createdAt;
            u.updatedAt = updatedAt;
            u.sessionToken = sessionToken;
            u.phone = phone;
            u.cpf = cpf;
            u.company = company;
            u.status = status;
            u.score = score;
            u.tags = tags;
            u.divisionId = divisionId;
            u.groupId = groupId;
            u.noteIds = noteIds;
            return u;
        }
    }
}