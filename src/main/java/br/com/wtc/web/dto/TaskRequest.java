package br.com.wtc.web.dto;

public class TaskRequest {
    private String title;
    private String description;
    private String clientId;
    private String clientName;
    private String category;
    private String priority;
    private String status;
    private String messageRef;
    private String dueDate;

    public String getTitle()           { return title; }
    public void setTitle(String t)     { this.title = t; }
    public String getDescription()     { return description; }
    public void setDescription(String d){ this.description = d; }
    public String getClientId()        { return clientId; }
    public void setClientId(String c)  { this.clientId = c; }
    public String getClientName()      { return clientName; }
    public void setClientName(String n){ this.clientName = n; }
    public String getCategory()        { return category; }
    public void setCategory(String c)  { this.category = c; }
    public String getPriority()        { return priority; }
    public void setPriority(String p)  { this.priority = p; }
    public String getStatus()          { return status; }
    public void setStatus(String s)    { this.status = s; }
    public String getMessageRef()      { return messageRef; }
    public void setMessageRef(String m){ this.messageRef = m; }
    public String getDueDate()         { return dueDate; }
    public void setDueDate(String d)   { this.dueDate = d; }
}