package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_voucher_order")
public class UserVoucherOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_voucher_usage_id", nullable = false)
    private UserVoucherUsage userVoucherUsage;

    // Quan hệ với bảng User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ với bảng Voucher
    @ManyToOne
    @JoinColumn(name = "voucher_code", nullable = false)
    private Voucher voucher;

    // Quan hệ với bảng Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Mã đơn hàng
    @Column(name = "order_code", nullable = false)
    private String orderCode;

    // Ngày sử dụng voucher
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

   
    // Constructors
    public UserVoucherOrder() {}

    public UserVoucherOrder(User user, Voucher voucher, Order order, String orderCode, LocalDateTime orderDate, Double voucherValue) {
        this.user = user;
        this.voucher = voucher;
        this.order = order;
        this.orderCode = orderCode;
        this.orderDate = orderDate;
        this.voucherValue = voucherValue;
    }

    // Getters và Setters
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

   

   
    // Getter và setter cho userVoucherUsage
    public UserVoucherUsage getUserVoucherUsage() {
        return userVoucherUsage;
    }

    public void setUserVoucherUsage(UserVoucherUsage userVoucherUsage) {
        this.userVoucherUsage = userVoucherUsage;
    }
    @Column(name = "voucher_value")
    private Double voucherValue; // Trường kiểu Double cho giá trị voucher

    // Các trường và phương thức khác...

    public Double getVoucherValue() {
        return voucherValue;
    }

    public void setVoucherValue(Double voucherValue) {
        this.voucherValue = voucherValue;
    }
   
   
    // Constructor với các tham số
    public UserVoucherOrder(Order order, Voucher voucher, User user) {
        this.order = order;
        this.voucher = voucher;
        this.user = user;
    }

   

}
