package com.mcnz.rps.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "user_voucher_usage") // Sử dụng tên bảng chuẩn SQL (snake_case)
public class UserVoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ với bảng User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // user_id là khóa ngoại
    private User user;

    // Quan hệ với bảng Voucher
    @ManyToOne
    @JoinColumn(name = "voucher_code", nullable = false) // voucher_code là khóa ngoại
    private Voucher voucher;

    // Số lần sử dụng voucher
    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    // Quan hệ với bảng Order
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true) // order_id có thể null
    private Order order;

    // Trường để lưu mã đơn hàng nếu cần
    @Column(name = "order_code", nullable = true)
    private String orderCode;

    // Quan hệ với bảng Order
    @OneToMany(mappedBy = "userVoucherUsage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVoucherOrder> userVoucherOrders = new ArrayList<>();

    // Constructors
    public UserVoucherUsage() {}

    public UserVoucherUsage(User user, Voucher voucher) {
        this.user = user;
        this.voucher = voucher;
        this.usageCount = 0;
    }

    // Getters và setters
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

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public void incrementUsage() {
        this.usageCount++;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) {
            this.orderCode = order.getOrderCode(); // Lấy order_code từ đối tượng Order
        }
    }

    public String getOrderCode() {
        return order != null ? order.getOrderCode() : null;
    }

    // Getter và setter cho orderCode
    public String getOrderCodeField() {
        return this.orderCode;
    }

    public void setOrderCodeField(String orderCode) {
        this.orderCode = orderCode;
    }
    public List<UserVoucherOrder> getUserVoucherOrders() {
        return userVoucherOrders;
    }

    public void setUserVoucherOrders(List<UserVoucherOrder> userVoucherOrders) {
        this.userVoucherOrders = userVoucherOrders;
    }

    public void addOrder(Order order) {
        // Lấy thông tin về voucher và người dùng hiện tại
        String orderCode = order.getOrderCode();
        LocalDateTime orderDate = order.getOrderDate();
        
        // Lấy giá trị voucher (Integer từ Order)
        Integer voucherValueInteger = order.getVoucherValue();
        
        // Ép kiểu từ Integer sang Double
        Double voucherValue = (voucherValueInteger != null) ? voucherValueInteger.doubleValue() : 0.0;

        // Khởi tạo UserVoucherOrder với các tham số cần thiết
        UserVoucherOrder userVoucherOrder = new UserVoucherOrder(
            this.user,  // Người dùng đã sử dụng voucher
            this.voucher, // Voucher đã sử dụng
            order, // Đơn hàng
            orderCode, // Mã đơn hàng
            orderDate, // Ngày sử dụng voucher
            voucherValue // Giá trị voucher
        );

        // Thiết lập quan hệ với UserVoucherUsage
        userVoucherOrder.setUserVoucherUsage(this); // Liên kết với UserVoucherUsage hiện tại
        this.userVoucherOrders.add(userVoucherOrder); // Thêm đơn hàng vào danh sách UserVoucherOrders

        // Lưu hoặc xử lý thêm nếu cần (ví dụ, lưu vào DB)
    }



    public void removeOrder(UserVoucherOrder userVoucherOrder) {
        this.userVoucherOrders.remove(userVoucherOrder);
    }
}

