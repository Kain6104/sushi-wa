package com.mcnz.rps.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")  // Chỉ định tên bảng là 'notification'
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;  // Người nhận thông báo
    private String message;   // Nội dung thông báo
    private String link;      // Liên kết đến chi tiết
    private boolean isRead;   // Trạng thái đã đọc (đổi tên từ "read" thành "isRead")
    private LocalDateTime timestamp;  // Thời gian tạo thông báo
    private int priority;     // Độ ưu tiên (1: bình thường, 2: khẩn cấp)
    private LocalDateTime scheduledTime; // Thời gian gửi thông báo (dành cho thông báo hẹn giờ)
    private boolean isPopup;  // Hiển thị dưới dạng pop-up hay banner

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getLink() {
        return link;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public boolean isPopup() {
        return isPopup;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public void setPopup(boolean isPopup) {
        this.isPopup = isPopup;
    }

    public boolean isRead() {
        return isRead;  // Đổi lại tên phương thức getter cho đúng
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;  // Đổi lại tên phương thức setter cho đúng
    }
}
