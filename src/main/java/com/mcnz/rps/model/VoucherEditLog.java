package com.mcnz.rps.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voucher_edit_logs")
public class VoucherEditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @ManyToOne
    @JoinColumn(name = "edited_by", nullable = false)
    private User editedBy;

    @Column(name = "edit_time", nullable = false)
    private LocalDateTime editTime;

    @Column(name = "changes", columnDefinition = "TEXT")
    private String changes;

 // ✅ Sửa lỗi so sánh kiểu int bằng "!=" thay vì ".equals()"
    public void setChanges(Voucher oldVoucher, Voucher newVoucher) {
        StringBuilder sb = new StringBuilder();

        if (oldVoucher.getDiscountAmount() != newVoucher.getDiscountAmount()) {
            sb.append("Số tiền giảm giá: ").append(oldVoucher.getDiscountAmount())
              .append(" ➝ ").append(newVoucher.getDiscountAmount()).append("\n");
        }

        if (!oldVoucher.getDiscountType().equals(newVoucher.getDiscountType())) {
            sb.append("Loại giảm giá: ").append(oldVoucher.getDiscountType())
              .append(" ➝ ").append(newVoucher.getDiscountType()).append("\n");
        }

        if (!oldVoucher.getStartDate().equals(newVoucher.getStartDate())) {
            sb.append("Ngày bắt đầu: ").append(oldVoucher.getStartDate())
              .append(" ➝ ").append(newVoucher.getStartDate()).append("\n");
        }

        if (!oldVoucher.getEndDate().equals(newVoucher.getEndDate())) {
            sb.append("Ngày kết thúc: ").append(oldVoucher.getEndDate())
              .append(" ➝ ").append(newVoucher.getEndDate()).append("\n");
        }

        if (!oldVoucher.getUsageCondition().equals(newVoucher.getUsageCondition())) {
            sb.append("Điều kiện sử dụng: ").append(oldVoucher.getUsageCondition())
              .append(" ➝ ").append(newVoucher.getUsageCondition()).append("\n");
        }

        this.changes = sb.toString();
    }


    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Voucher getVoucher() { return voucher; }
    public void setVoucher(Voucher voucher) { this.voucher = voucher; }

    public User getEditedBy() { return editedBy; }
    public void setEditedBy(User editedBy) { this.editedBy = editedBy; }

    public LocalDateTime getEditTime() { return editTime; }
    public void setEditTime(LocalDateTime editTime) { this.editTime = editTime; }

    public String getChanges() { return changes; }
    public void setChanges(String changes) { this.changes = changes; }

    @Override
    public String toString() {
        return "VoucherEditLog{" +
               "id=" + id +
               ", voucher=" + (voucher != null ? voucher.getCode() : "null") +
               ", editedBy=" + (editedBy != null ? editedBy.getUsername() : "null") +
               ", editTime=" + editTime +
               ", changes='" + changes + '\'' +
               '}';
    }
}
