package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender; // user/admin
    private String receiver; // user/admin
    private String message;
    private LocalDateTime timestamp;

    @Column(name = "chat_with_admin", nullable = false)
    private boolean chatWithAdmin;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isChatWithAdmin() { return chatWithAdmin; }
    public void setChatWithAdmin(boolean chatWithAdmin) { this.chatWithAdmin = chatWithAdmin; }
}
