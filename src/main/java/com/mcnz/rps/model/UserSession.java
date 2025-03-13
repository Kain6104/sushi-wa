package com.mcnz.rps.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class UserSession {

    @Id
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    private LocalDateTime expirationTime;

    // Getter và Setter cho sessionId
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    // Getter và Setter cho user
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Getter và Setter cho loginTime
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    // Getter và Setter cho expirationTime
    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}


