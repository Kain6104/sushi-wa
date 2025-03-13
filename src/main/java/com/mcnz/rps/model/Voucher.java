package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Vouchers")
public class Voucher {

    @Id
    @Column(name = "code", unique = true, nullable = false)
    private String code;

    @Column(name = "discountAmount", nullable = false)
    private int discountAmount;

    @Column(name = "usageCount", nullable = false)
    private int usageCount;

    @Column(name = "remaining_uses", nullable = false)
    private int remainingUses = 0;

    @Column(name = "maxUsage", nullable = false)
    private int maxUsage;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "max_usage_per_user", nullable = false)
    private int maxUsagePerUser;

    @Column(name = "order_code", nullable = true) // Đảm bảo nullable = true nếu không bắt buộc
    private String orderCode;

    @Column(name = "applicable_to_all", nullable = false)
    private boolean applicableToAll;  // Nếu true thì áp dụng cho tất cả sản phẩm

    @Column(name = "applicable_item_id", nullable = true)
    private Long applicableItemId; // ID món ăn nếu voucher chỉ áp dụng cho món đó

    @Column(name = "usage_condition", columnDefinition = "TEXT")
    private String usageCondition; // Mô tả điều kiện sử dụng voucher
    
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL)
    private List<UserVoucher> userVouchers;

    @ManyToMany
    @JoinTable(
        name = "voucher_menu_items",
        joinColumns = @JoinColumn(name = "voucher_code"),
        inverseJoinColumns = @JoinColumn(name = "menu_item_id")
    )
    private List<MenuItems> applicableItems; // Thay vì chỉ 1 món ăn
    
    public boolean isApplicableToAll() {
        return applicableToAll;
    }

    public void setApplicableToAll(boolean applicableToAll) {
        this.applicableToAll = applicableToAll;
    }

    public Long getApplicableItemId() {
        return applicableItemId;
    }

    public void setApplicableItemId(Long applicableItemId) {
        this.applicableItemId = applicableItemId;
    }

    public String getUsageCondition() {
        return usageCondition;
    }

    public void setUsageCondition(String usageCondition) {
        this.usageCondition = usageCondition;
    }
    // Constructors
    public Voucher() {}
    
    public Voucher(String code, int discountAmount, int maxUsage, int maxUsagePerUser, LocalDateTime startDate, LocalDateTime endDate, boolean applicableToAll, Long applicableItemId, String usageCondition) {
        this.code = code;
        this.discountAmount = discountAmount;
        this.maxUsage = maxUsage;
        this.maxUsagePerUser = maxUsagePerUser;
        this.usageCount = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.applicableToAll = applicableToAll;
        this.applicableItemId = applicableItemId;
        this.usageCondition = usageCondition;
    }


    // Getters và setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(int discountAmount) {
        this.discountAmount = discountAmount;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public int getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(int maxUsage) {
        this.maxUsage = maxUsage;
    }

    public int getMaxUsagePerUser() {
        return maxUsagePerUser;
    }

    public void setMaxUsagePerUser(int maxUsagePerUser) {
        this.maxUsagePerUser = maxUsagePerUser;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    // Kiểm tra nếu voucher còn có thể sử dụng
    public boolean canUse() {
        return usageCount < maxUsage && isValidDate();
    }

    // Kiểm tra nếu voucher trong khoảng thời gian hợp lệ
    public boolean isValidDate() {
        LocalDateTime today = LocalDateTime.now();
        return (startDate == null || !today.isBefore(startDate)) &&
               (endDate == null || !today.isAfter(endDate));
    }
    // Tăng số lượng sử dụng lên 1 lần nếu voucher có thể sử dụng
    public void incrementUsage() {
        if (canUse()) {
            this.usageCount++;
        }
    }
    public List<MenuItems> getApplicableItems() {
        return applicableItems;
    }

    public void setApplicableItems(List<MenuItems> applicableItems) {
        this.applicableItems = applicableItems;
    }
    @Column(name = "min_order_amount", nullable = false)
    private int minOrderAmount; // Đơn hàng tối thiểu để áp dụng voucher

    @Column(name = "discount_type", nullable = false)
    private String discountType; // "PERCENT" hoặc "AMOUNT"

    @Column(name = "max_discount", nullable = true)
    private Integer maxDiscount; // Giảm tối đa nếu giảm theo %
    public int getMinOrderAmount() {
        return minOrderAmount;
    }

    public void setMinOrderAmount(int minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Integer getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(Integer maxDiscount) {
        this.maxDiscount = maxDiscount;
    }
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    
}
