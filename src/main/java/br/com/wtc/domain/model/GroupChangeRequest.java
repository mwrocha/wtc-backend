package br.com.wtc.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "group_change_requests")
public class GroupChangeRequest {

    @Id
    private String id;
    private String clientId;
    private String clientEmail;
    private String clientName;
    private String currentGroupId;
    private String currentGroupName;
    private String requestedGroupId;
    private String requestedGroupName;
    private String reason;
    private String status; // PENDING, APPROVED, REJECTED
    private String reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;

    public GroupChangeRequest() {}

    // Getters e Setters
    public String getId()                           { return id; }
    public void setId(String id)                    { this.id = id; }
    public String getClientId()                     { return clientId; }
    public void setClientId(String clientId)        { this.clientId = clientId; }
    public String getClientEmail()                  { return clientEmail; }
    public void setClientEmail(String e)            { this.clientEmail = e; }
    public String getClientName()                   { return clientName; }
    public void setClientName(String n)             { this.clientName = n; }
    public String getCurrentGroupId()               { return currentGroupId; }
    public void setCurrentGroupId(String g)         { this.currentGroupId = g; }
    public String getCurrentGroupName()             { return currentGroupName; }
    public void setCurrentGroupName(String g)       { this.currentGroupName = g; }
    public String getRequestedGroupId()             { return requestedGroupId; }
    public void setRequestedGroupId(String g)       { this.requestedGroupId = g; }
    public String getRequestedGroupName()           { return requestedGroupName; }
    public void setRequestedGroupName(String g)     { this.requestedGroupName = g; }
    public String getReason()                       { return reason; }
    public void setReason(String reason)            { this.reason = reason; }
    public String getStatus()                       { return status; }
    public void setStatus(String status)            { this.status = status; }
    public String getReviewedBy()                   { return reviewedBy; }
    public void setReviewedBy(String r)             { this.reviewedBy = r; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime t)       { this.createdAt = t; }
    public LocalDateTime getReviewedAt()            { return reviewedAt; }
    public void setReviewedAt(LocalDateTime t)      { this.reviewedAt = t; }
}