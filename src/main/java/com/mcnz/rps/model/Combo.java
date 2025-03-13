package com.mcnz.rps.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "combos")
public class Combo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "combo_id")
    private Long comboId;

    @Column(name = "combo_name", nullable = false)
    private String comboName;

    @ManyToMany
    @JoinTable(
        name = "combo_menu_items",
        joinColumns = @JoinColumn(name = "combo_id"),
        inverseJoinColumns = @JoinColumn(name = "item_id")
    )
    private List<MenuItems> menuItems;  // Danh sách các món ăn trong combo

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "description")
    private String description;


    @Column(name = "image_url")  // Thêm thuộc tính lưu URL hình ảnh
    private String imageUrl;  // Thuộc tính mới để lưu đường dẫn hình ảnh
    @Column(unique = true, nullable = false)
    private String token = UUID.randomUUID().toString();

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
    // Getters và Setters
    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

    public String getComboName() {
        return comboName;
    }

    public void setComboName(String comboName) {
        this.comboName = comboName;
    }

    public List<MenuItems> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItems> menuItems) {
        this.menuItems = menuItems;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public String getImageUrl() {  // Getter cho imageUrl
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {  // Setter cho imageUrl
        this.imageUrl = imageUrl;
    }
}
