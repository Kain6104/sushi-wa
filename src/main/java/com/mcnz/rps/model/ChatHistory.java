package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_history")
public class ChatHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userMessage;
    private String botResponse;

    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructor không tham số
    public ChatHistory() {}

    // Constructor có tham số
    public ChatHistory(String userMessage, String botResponse) {
        this.userMessage = userMessage;
        this.botResponse = botResponse;
        this.timestamp = LocalDateTime.now();
    }

    // Getter và Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getBotResponse() {
        return botResponse;
    }

    public void setBotResponse(String botResponse) {
        this.botResponse = botResponse;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatHistory{" +
                "id=" + id +
                ", userMessage='" + userMessage + '\'' +
                ", botResponse='" + botResponse + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
