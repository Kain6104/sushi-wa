package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;
    // Trường mới để lưu đường dẫn hình ảnh
    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Đầu vào từ form
    private LocalDate startDate;
    
    @Column(nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Đầu vào từ form
    private LocalDate endDate;

    @Column(nullable = false, unique = true, updatable = false)
    private String token;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
        this.token = UUID.randomUUID().toString(); // Sinh token duy nhất khi tạo mới
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    @Column(nullable = false, updatable = false)
    private LocalDate createdAt;
    @Column(name = "`order`")
    private int order;
    private boolean pinned;
    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    private String formattedStartDate; // Thêm trường này
    private String formattedEndDate;  // Thêm trường này
    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

  
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public String getFormattedStartDate() {
        return formattedStartDate;
    }

    public void setFormattedStartDate(String formattedStartDate) {
        this.formattedStartDate = formattedStartDate;
    }

    public String getFormattedEndDate() {
        return formattedEndDate;
    }

    public void setFormattedEndDate(String formattedEndDate) {
        this.formattedEndDate = formattedEndDate;
    }
    private int likes = 0;
    private int loves = 0;

    // Getters và Setters
    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLoves() {
        return loves;
    }

    public void setLoves(int loves) {
        this.loves = loves;
    }
    private String name; // Make sure the 'name' property exists here

    @Column(nullable = false)
    private String privacy;

    // Getter và Setter cho privacy
    public String getPrivacy() {
        return privacy;
    }

    public void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

}
