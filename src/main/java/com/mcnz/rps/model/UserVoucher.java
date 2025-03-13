package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_vouchers")
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_code", referencedColumnName = "code", nullable = false)
    private Voucher voucher; // Lưu toàn bộ thông tin voucher
    
    @Column(nullable = false)
    private LocalDateTime savedAt;

    public UserVoucher() {
        this.savedAt = LocalDateTime.now(); // Đặt giá trị mặc định
    }
    @Column(name = "used", nullable = false)
    private boolean used = false;
   
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    // Constructor cần thêm
    public UserVoucher(User user, Voucher voucher) {
        this.user = user;
        this.voucher = voucher;
        this.isUsed = false;
    }
    
}
