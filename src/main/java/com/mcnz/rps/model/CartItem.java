package com.mcnz.rps.model;

public class CartItem {
    private Integer id;            // ID of the item
    private String name;           // Name of the item
    private double price;          // Price of the item
    private int quantity;          // Quantity of the item
    private String imageUrl;       // Image URL of the item
    private String promotion;      
    private MenuItems menuItem;    // Full MenuItems object for reference

    // Constructors
    // Constructor with basic information
 // Constructor Full với tất cả các thuộc tính
    public CartItem(Long id, String name, Double price, int quantity, String imageUrl, MenuItems menuItem) {
        this.id = id.intValue();  // Chuyển Long thành Integer
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.menuItem = menuItem;
    }


 // Constructor with MenuItems object
    public CartItem(MenuItems menuItem, int quantity) {
        this.id = menuItem.getItemId();
        this.name = menuItem.getName();
        this.price = menuItem.getPrice();
        this.quantity = quantity;
        this.imageUrl = menuItem.getImageUrl();
        this.menuItem = menuItem;
        this.promotion = menuItem.getPromotion(); // Áp dụng khuyến mãi từ MenuItems
    }
    public boolean hasPromotion() {
        return promotion != null && !promotion.isEmpty();
    }

    // Full constructor for all attributes
    public CartItem(Integer id, String name, double price, int quantity, String imageUrl, MenuItems menuItem) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.menuItem = menuItem;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public MenuItems getMenuItem() {
        return menuItem;
    }

    public void setMenuItem(MenuItems menuItem) {
        this.menuItem = menuItem;
    }

    // Increment quantity
    public void incrementQuantity() {
        this.quantity++;
    }

    // Decrement quantity (ensure it doesn't go below 1)
    public void decrementQuantity() {
        if (this.quantity > 1) {
            this.quantity--;
        }
    }

    // Calculate total price for this CartItem
    public double getTotalPrice() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", imageUrl='" + imageUrl + '\'' +
                ", menuItem=" + menuItem +
                '}';
    }

    // Getter and Setter for promotion
    public String getPromotion() {
        return promotion;
    }

    public void setPromotion(String promotion) {
        this.promotion = promotion;
    }
    public Double getDiscountedPrice() {
        if (promotion == null || promotion.isEmpty()) {
            return price;
        }
        // Giảm giá theo phần trăm
        if (promotion.contains("%")) {
            double discountPercent = Double.parseDouble(promotion.replace("%", "").trim());
            return price * (1 - discountPercent / 100);
        }
        // Giảm giá theo số tiền (VD: "-20000")
        if (promotion.startsWith("-")) {
            double discountAmount = Double.parseDouble(promotion.replace("-", "").trim());
            return Math.max(price - discountAmount, 0); // Giá không âm
        }
        // Mua 2 tặng 1
        if (promotion.equalsIgnoreCase("2T1") && quantity >= 3) {
            return price * (quantity - quantity / 3);
        }
        // Mua 5 giảm 10%
        if (promotion.equalsIgnoreCase("Mua 5 giảm 10%") && quantity >= 5) {
            return price * quantity * 0.9;
        }
        return price;
    }


    public Double getTotalPriceWithDiscount() {
        return getDiscountedPrice() * quantity;
    }
   

}
