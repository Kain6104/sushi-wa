package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User user; // Liên kết với bảng `users`

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();


    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "item_name")
    @Column(name = "quantity")
    private Map<String, Integer> items; // Các món ăn và số lượng

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate; // Ngày đặt hàng

    @Column(name = "delivery_time")
    private LocalDateTime deliveryTime; // Thời gian giao hàng
 

    // Getter cho định dạng ngày giờ
    public String getFormattedOrderDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return orderDate.format(formatter); // Định dạng ngày giờ
    }

    public String getFormattedDeliveryTime() {
        if (deliveryTime == null) {
            return "Chưa xác định"; // Nếu chưa có thời gian giao hàng
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return deliveryTime.format(formatter);
    }

    @Column(name = "status", nullable = false)
    private String status; // Trạng thái đơn hàng

    @Column(name = "total_price", nullable = false)
    private Double totalPrice; // Tổng giá trị trước khi giảm giá

    @Column(name = "address", nullable = true)
    private String address = ""; // Địa chỉ giao hàng

    @Column(name = "phone", nullable = true)
    private String phone = ""; // Số điện thoại người nhận

    @Column(name = "points_used")
    private int pointsUsed; // Điểm đã sử dụng

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount; // Tổng tiền thanh toán sau giảm giá

    @Column(name = "voucher_code")
    private String voucherCode; // Mã voucher áp dụng

    @Column(name = "payment_method", nullable = true)
    private String paymentMethod = ""; // Phương thức thanh toán

    @Column(name = "order_code", unique = true, nullable = false)
    private String orderCode;

    
    @Column(name = "name", nullable = true)
    private String name = ""; // Tên người nhận hàng

    @Column(name = "username", nullable = true)
    private String username = ""; // Tên đăng nhập người đặt hàng
    @Column(name = "voucher_value")
    private Integer voucherValue; // Giá trị giảm giá từ voucher

    
    @Column(name = "note")
    private String note = ""; // Thời gian giao hàng
    
    // Getter và Setter
    public Integer getVoucherValue() {
        return voucherValue;
    }

    public void setVoucherValue(Integer voucherValue) {
        this.voucherValue = voucherValue;
    }

    private String formattedTotalAmount;

    // Getter và setter cho totalAmount và totalPrice
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public int getRewardPoints() {
        return (int) Math.floor(this.totalAmount / 10000);
    }

	public String getFormattedRewardPoints() {
	    DecimalFormat formatter = new DecimalFormat("#,###");
	    int rewardPoints = (int) Math.floor(this.totalAmount / 10000);
	    return formatter.format(rewardPoints).replace(",", ".");
	}
   
    public String getFormattedTotalAmount() {
        return formattedTotalAmount;
    }
    public void setFormattedTotalAmount(String formattedTotalAmount) {
        this.formattedTotalAmount = formattedTotalAmount;
    }
    // Getters và Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public void setItems(Map<String, Integer> items) {
        this.items = items;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(LocalDateTime deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatusDescription() {
        if ("DELIVERED".equalsIgnoreCase(this.status)) {
            return "Đã nhận hàng";
        }
        // Các trạng thái khác (nếu cần)
        switch (this.status.toUpperCase()) {
            case "PENDING":
                return "Đang chờ xử lý";
            case "DELIVERING":
                return "Đang giao hàng";
            case "CANCELED":
                return "Đã hủy";
            default:
                return "Trạng thái không xác định";
        }
    }


    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getPointsUsed() {
        return pointsUsed;
    }

    public void setPointsUsed(int pointsUsed) {
        this.pointsUsed = pointsUsed;
    }

    // Phương thức tính điểm
    public int getPoints() {
        return this.pointsUsed * 500; // Không cần Math.floor vì giá trị trả về đã là int
    }
    public String getFormattedPoints() {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(this.getPoints()).replace(",", "."); // Đổi dấu phân cách từ ',' thành '.'
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
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

    public String getItemsDescription() {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return "Không có món ăn";
        }

        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<table style='width: 100%; border-collapse: collapse;'>");
        htmlBuilder.append("<thead>");
        htmlBuilder.append("<tr>");
        htmlBuilder.append("<th style='padding: 8px; border: 1px solid #ddd;'>Hình ảnh</th>");
        htmlBuilder.append("<th style='padding: 8px; border: 1px solid #ddd;'>Tên món</th>");
        htmlBuilder.append("<th style='padding: 8px; border: 1px solid #ddd;'>Giá</th>");
        htmlBuilder.append("<th style='padding: 8px; border: 1px solid #ddd;'>Số lượng</th>");
        htmlBuilder.append("<th style='padding: 8px; border: 1px solid #ddd;'>Tổng tiền</th>");
        htmlBuilder.append("</tr>");
        htmlBuilder.append("</thead>");
        htmlBuilder.append("<tbody>");

        for (OrderDetail detail : orderDetails) {
            MenuItems menuItem = detail.getMenuItems();
            htmlBuilder.append("<tr>");
            
            // Cột hình ảnh
            htmlBuilder.append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>")
                .append("<img src='")
                .append(menuItem.getImageUrl())
                .append("' alt='")
                .append(menuItem.getName())
                .append("' style='width: 50px; height: 50px; object-fit: cover;'>")
                .append("</td>");
            
            // Cột tên món với liên kết chi tiết
            htmlBuilder.append("<td style='padding: 8px; border: 1px solid #ddd;'>")
                .append("<a href='/product-details/")
                .append(menuItem.getToken())
                .append("' style='text-decoration: none; color: #007bff;'>")
                .append(menuItem.getName())
                .append("</a>")
                .append("</td>");
            
            // Cột giá
            htmlBuilder.append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>")
                .append(String.format("%,.0f", detail.getOriginalPrice()))
                .append(" đ</td>");
            
            // Cột số lượng
            htmlBuilder.append("<td style='padding: 8px; border: 1px solid #ddd; text-align: center;'>")
                .append(detail.getQuantity())
                .append("</td>");
            
            // Cột tổng tiền
            htmlBuilder.append("<td style='padding: 8px; border: 1px solid #ddd; text-align: right;'>")
                .append(String.format("%,.0f", detail.getPrice() * detail.getQuantity()))
                .append(" đ</td>");
            
            htmlBuilder.append("</tr>");
        }

        htmlBuilder.append("</tbody>");
        htmlBuilder.append("</table>");

        return htmlBuilder.toString();
    }

    private String formattedTotalPrice; // Giá trị đã định dạng

    // Getter và Setter cho formattedTotalPrice
    public String getFormattedTotalPrice() {
        return formattedTotalPrice;
    }

    public void setFormattedTotalPrice(String formattedTotalPrice) {
        this.formattedTotalPrice = formattedTotalPrice;
    }
    private String cancelReason;

	 // Getter và Setter
	 public String getCancelReason() {
	     return cancelReason;
	 }
	
	 public void setCancelReason(String cancelReason) {
	     this.cancelReason = cancelReason;
	 }
	 private String canceledBy; // "USER" or "ADMIN"

	// Getter
	public String getCanceledBy() {
	    return canceledBy;
	}

	// Setter
	public void setCanceledBy(String canceledBy) {
	    this.canceledBy = canceledBy;
	}
	 private LocalDateTime canceledAt; // Ngày giờ hủy đơn
	 // Getter và Setter cho canceledAt
    public LocalDateTime getCanceledAt() {
        return canceledAt;
    }

    public void setCanceledAt(LocalDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    // Getter cho canceledAt với định dạng HH:mm:ss dd/MM/yyyy
    public String getCanceledAtFormatted() {
        if (canceledAt == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return canceledAt.format(formatter);
    }
    public void cancelOrder() {
        if ("CANCELED".equalsIgnoreCase(this.status)) {
            return; // Đơn hàng đã bị hủy trước đó
        }
        
        this.status = "CANCELED";
        
        if (this.pointsUsed > 0 && this.user != null) {
            this.user.setPoints(this.user.getPoints() + this.pointsUsed);
        }
    }
    @Column(name = "voucher_discount", nullable = true)
    private Double voucherDiscount;

    private String formattedVoucherDiscount; // Giá trị đã định dạng

    // Getter và Setter cho voucherDiscount
    public Double getVoucherDiscount() {
        return voucherDiscount;
    }

    public void setVoucherDiscount(Double voucherDiscount) {
        this.voucherDiscount = voucherDiscount;
    }

    // Getter và Setter cho formattedVoucherDiscount
    public String getFormattedVoucherDiscount() {
        return formattedVoucherDiscount;
    }

    public void setFormattedVoucherDiscount(String formattedVoucherDiscount) {
        this.formattedVoucherDiscount = formattedVoucherDiscount;
    }
    public void generateOrderCode() {
        this.orderCode = "ORD-" + this.orderId; // Tạo mã đơn hàng dạng ORD-<orderId>
    }

    private int pointsReturned;

    // Phương thức getter và setter
    public int getPointsReturned() {
        return pointsReturned;
    }

    public void setPointsReturned(int pointsReturned) {
        this.pointsReturned = pointsReturned;
    }
    public void setNote(String note) {
        this.note = note;
    }

public String getNote() {
    return note;
}
}
