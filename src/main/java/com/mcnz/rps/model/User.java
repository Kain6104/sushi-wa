package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customers")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "name")
    private String name;

    @Column(unique = true, nullable = false, name = "username")
    private String username;

    @Column(unique = true, nullable = false, name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(name = "points")
    private int points;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role")
    private String role;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    private LocalDateTime createdAt;
    @Column(nullable = false, updatable = false)
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Column(name = "current_points")
    private Double currentPoints; // Thay đổi kiểu dữ liệu sang Double

    @Column(name = "force_relogin", nullable = false)
    private boolean forceRelogin = false; // Default value is false
    
    public boolean isForceRelogin() {
        return forceRelogin;
    }

    public void setForceRelogin(boolean forceRelogin) {
        this.forceRelogin = forceRelogin;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
    @Transient
    private boolean onlineStatus;

    public boolean isOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
    }
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    // Các trường hiện tại
    private boolean isEmailVerified = false;
    private String verificationToken;

    // Getter và Setter
    public boolean isEmailVerified() { return isEmailVerified; }
    public void setEmailVerified(boolean emailVerified) { isEmailVerified = emailVerified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
    private String emailVerificationCode; // Mã xác thực 6 số
    private String emailVerificationToken; // Token xác nhận email mới
    private String pendingEmail; // Email mới tạm thời

    // Getter và Setter
    public String getEmailVerificationCode() { return emailVerificationCode; }
    public void setEmailVerificationCode(String code) { this.emailVerificationCode = code; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String token) { this.emailVerificationToken = token; }

    public String getPendingEmail() { return pendingEmail; }
    public void setPendingEmail(String pendingEmail) { this.pendingEmail = pendingEmail; }
    public boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }
    @Column(name = "is_account_locked", nullable = false)
    private boolean isAccountLocked = false; // Mặc định tài khoản không bị khóa

    public boolean isAccountLocked() {
        return isAccountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        isAccountLocked = accountLocked;
    }
    private double totalSpending;

 // Getter và Setter
 public double getTotalSpending() {
     return totalSpending;
 }

 public void setTotalSpending(double totalSpending) {
     this.totalSpending = totalSpending;
 }
 private String formattedTotalSpending;

//Getter và Setter
public String getFormattedTotalSpending() {
  return formattedTotalSpending;
}

public void setFormattedTotalSpending(String formattedTotalSpending) {
  this.formattedTotalSpending = formattedTotalSpending;
}

@Column(name = "secure_token", unique = true, nullable = false)
private String secureToken;

@PrePersist
public void generateSecureToken() {
    if (this.secureToken == null || this.secureToken.isEmpty()) {
        this.secureToken = UUID.randomUUID().toString();
    }
}

// Getter và Setter
public String getSecureToken() {
    return secureToken;
}

public void setSecureToken(String secureToken) {
    this.secureToken = secureToken;
}
// Các thuộc tính khác của User...
private String formattedCreatedAt;

// Các getter và setter khác...

// Getter và setter cho formattedCreatedAt
public String getFormattedCreatedAt() {
    return formattedCreatedAt;
}

public void setFormattedCreatedAt(String formattedCreatedAt) {
    this.formattedCreatedAt = formattedCreatedAt;
}

// Getters và Setters
public Double getCurrentPoints() {
    return currentPoints;
}

public void setCurrentPoints(Double currentPoints) {
    this.currentPoints = currentPoints;
}
}
