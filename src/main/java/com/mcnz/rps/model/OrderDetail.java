package com.mcnz.rps.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_details")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long orderDetailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private MenuItems menuItems;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "original_price", nullable = false)
    private double originalPrice; // Lưu giá trị của món ăn tại thời điểm thanh toán

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")
    
    private Combo combo;  // Liên kết với Combo nếu có
    public Combo getCombo() {
        return combo;
    }

    public void setCombo(Combo combo) {
        this.combo = combo;
    }
    // Getters and Setters
    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(Long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public MenuItems getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(MenuItems menuItems) {
        this.menuItems = menuItems;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }
}
