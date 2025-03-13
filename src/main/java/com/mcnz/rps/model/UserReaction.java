package com.mcnz.rps.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_reactions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "promotion_id"})) // Đảm bảo mỗi người dùng chỉ thả cảm xúc 1 lần
public class UserReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "promotion_id", nullable = false)
    private Integer promotionId;

    @Column(name = "reaction", nullable = false)
    private String reaction;

    // Constructors
    public UserReaction() {}

    public UserReaction(Long userId, Integer promotionId, String reaction) {
        this.userId = userId;
        this.promotionId = promotionId;
        this.reaction = reaction;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(Integer promotionId) {
        this.promotionId = promotionId;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }
}
