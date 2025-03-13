package com.mcnz.rps.model;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "menu_items") // Ánh xạ với bảng menu_items
public class MenuItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id") // Ánh xạ với cột item_id
    private Integer itemId;

    @Column(name = "name", nullable = false) // Ánh xạ với cột name
    private String name;

    @Column(name = "description") // Ánh xạ với cột description
    private String description;

    @Column(name = "price", nullable = false) // Ánh xạ với cột price
    private Double price;

    @Column(name = "category", nullable = false) // Ánh xạ với cột category
    private String category;

    @Column(name = "ingredients") // Ánh xạ với cột ingredients
    private String ingredients;

    @Column(name = "image_url") // Ánh xạ với cột image_url
    private String imageUrl;

    @Column(name = "avg_rating", columnDefinition = "FLOAT DEFAULT 0") // Ánh xạ với cột avg_rating
    private Float avgRating;

    @Column(name = "promotion") // Ánh xạ với cột promotion
    private String promotion;

    @Column(name = "item_code", unique = true, nullable = false) // Ánh xạ với cột item_code
    private String itemCode;

    @Column(unique = true, nullable = false)
    private String token = UUID.randomUUID().toString();
    
    // Getter cho token
    public String getToken() {
        return token;
    }

    // Setter cho token
    public void setToken(String token) {
        this.token = token;
    }
    // Constructors
    public MenuItems() {
    }

    public MenuItems(String name, Double price, String category) {
        this.name = name;
        this.price = price;
        this.category = category;
    }

    // Getters and Setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Float getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Float avgRating) {
        this.avgRating = avgRating;
    }

    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }
    @ManyToMany(mappedBy = "menuItems")
    private List<Combo> combos; // Danh sách các combo mà món ăn này thuộc về

    // Getter và Setter cho combos
    public List<Combo> getCombos() {
        return combos;
    }

    public void setCombos(List<Combo> combos) {
        this.combos = combos;
    }
    private int soldQuantity;

    public int getSoldQuantity() {
        return soldQuantity;
    }

    public void setSoldQuantity(int soldQuantity) {
        this.soldQuantity = soldQuantity;
    }
}
