package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    private String title;
    private String description;
    private String clientId;       // MongoDB ID do cliente
    private String clientName;     // Nome do cliente para exibição
    private String operatorId;     // Email do operador responsável
    private String category;       // BILLING, SUPPORT, COMMERCIAL, OTHER
    private String priority;       // HIGH, MEDIUM, LOW
    private String status;         // PENDING, IN_PROGRESS, DONE
    private String messageRef;     // Trecho da mensagem de origem
    private String dueDate;        // Data limite (formato ISO)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {
        this.status    = "PENDING";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public String getId()                        { return id; }
    public void setId(String id)                 { this.id = id; }
    public String getTitle()                     { return title; }
    public void setTitle(String title)           { this.title = title; }
    public String getDescription()               { return description; }
    public void setDescription(String d)         { this.description = d; }
    public String getClientId()                  { return clientId; }
    public void setClientId(String clientId)     { this.clientId = clientId; }
    public String getClientName()                { return clientName; }
    public void setClientName(String n)          { this.clientName = n; }
    public String getOperatorId()                { return operatorId; }
    public void setOperatorId(String o)          { this.operatorId = o; }
    public String getCategory()                  { return category; }
    public void setCategory(String category)     { this.category = category; }
    public String getPriority()                  { return priority; }
    public void setPriority(String priority)     { this.priority = priority; }
    public String getStatus()                    { return status; }
    public void setStatus(String status)         { this.status = status; }
    public String getMessageRef()                { return messageRef; }
    public void setMessageRef(String m)          { this.messageRef = m; }
    public String getDueDate()                   { return dueDate; }
    public void setDueDate(String dueDate)       { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime c)    { this.createdAt = c; }
    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime u)    { this.updatedAt = u; }
}