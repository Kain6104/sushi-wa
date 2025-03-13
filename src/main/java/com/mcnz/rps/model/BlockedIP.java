package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "BlockedIPs")
public class BlockedIP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, unique = true)
    private String ipAddress;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt = LocalDateTime.now();

    // Thêm thuộc tính formattedBlockedAt để lưu thời gian đã định dạng
    private String formattedBlockedAt;

    // Getters và Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getBlockedAt() {
        return blockedAt;
    }

    public void setBlockedAt(LocalDateTime blockedAt) {
        this.blockedAt = blockedAt;
    }

    public String getFormattedBlockedAt() {
        return formattedBlockedAt;
    }

    public void setFormattedBlockedAt(String formattedBlockedAt) {
        this.formattedBlockedAt = formattedBlockedAt;
    }
}
