package com.mcnz.rps.service;

import com.mcnz.rps.model.Promotion;
import com.mcnz.rps.repository.PromotionRepository;
import com.mcnz.rps.repository.UserReactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import com.mcnz.rps.model.UserReaction;

import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Sort;


@Service
public class PromotionService {

    @Autowired
    private PromotionRepository promotionRepository;

    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll(Sort.by(Sort.Direction.ASC, "pinned", "order"));
    }

    @Autowired
    private UserReactionRepository commentRepository; // Đảm bảo repository này tồn tại

    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }
    public void deletePromotionById(Integer id) {
        if (!promotionRepository.existsById(id)) {
            throw new IllegalArgumentException("Promotion with ID " + id + " does not exist.");
        }

        // Xóa tất cả comment liên quan trước
        commentRepository.deleteByPromotionId(id);  

        // Sau đó xóa promotion
        promotionRepository.deleteById(id);
    }

    public Promotion getPromotionById(Integer id) {
        return promotionRepository.findById(id).orElse(null);
    }

    public void updatePromotion(Integer id, Promotion promotion) {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại"));
        
        // Kiểm tra và gán giá trị cho privacy
        if (promotion.getPrivacy() == null || promotion.getPrivacy().isEmpty()) {
            promotion.setPrivacy(existingPromotion.getPrivacy());
        }

        // Cập nhật các trường khác
        existingPromotion.setTitle(promotion.getTitle());
        existingPromotion.setDescription(promotion.getDescription());
        existingPromotion.setStartDate(promotion.getStartDate());
        existingPromotion.setEndDate(promotion.getEndDate());
        existingPromotion.setPrivacy(promotion.getPrivacy());
        existingPromotion.setImageUrl(promotion.getImageUrl());

        // Cập nhật trạng thái pinned và order
        existingPromotion.setPinned(promotion.isPinned());
        existingPromotion.setOrder(promotion.getOrder());

        promotionRepository.save(existingPromotion);
    }


    public boolean addReaction(Integer promotionId, String reaction) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);

        if (promotionOpt.isPresent()) {
            Promotion promotion = promotionOpt.get();

            switch (reaction.toLowerCase()) {
                case "like":
                    promotion.setLikes(promotion.getLikes() + 1);
                    break;
                case "love":
                    promotion.setLoves(promotion.getLoves() + 1);
                    break;
                default:
                    return false; // Phản ứng không hợp lệ
            }

            promotionRepository.save(promotion);
            return true;
        }

        return false; // Không tìm thấy chương trình khuyến mãi
    }
    @Autowired
    private UserReactionRepository userReactionRepository;

    public String handleReaction(Long userId, Integer promotionId, String reaction) {
        Optional<UserReaction> existingReactionOpt = userReactionRepository.findByUserIdAndPromotionId(userId, promotionId);

        if (existingReactionOpt.isPresent()) {
            UserReaction existingReaction = existingReactionOpt.get();
            String currentReaction = existingReaction.getReaction();

            if (currentReaction.equals(reaction)) {
                removeReaction(existingReaction, reaction, promotionId);
                return "unreacted";
            } else {
                updateReaction(existingReaction, reaction, promotionId, currentReaction);
                return "updated";
            }
        } else {
            Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
            if (promotionOpt.isEmpty()) {
                throw new IllegalArgumentException("Promotion ID không hợp lệ: " + promotionId);
            }
            addNewReaction(userId, promotionId, reaction);
            return "reacted";
        }
    }


    private void addNewReaction(Long userId, Integer promotionId, String reaction) {
        // Lưu cảm xúc mới
        UserReaction newReaction = new UserReaction();
        newReaction.setUserId(userId);
        newReaction.setPromotionId(promotionId);
        newReaction.setReaction(reaction);
        userReactionRepository.save(newReaction);

        // Tăng số lượt cảm xúc
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow();
        if ("like".equalsIgnoreCase(reaction)) {
            promotion.setLikes(promotion.getLikes() + 1);
        } else if ("love".equalsIgnoreCase(reaction)) {
            promotion.setLoves(promotion.getLoves() + 1);
        }
        promotionRepository.save(promotion);
    }

    private void removeReaction(UserReaction existingReaction, String reaction, Integer promotionId) {
        // Xóa cảm xúc
        userReactionRepository.delete(existingReaction);

        // Giảm số lượt cảm xúc
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow();
        if ("like".equalsIgnoreCase(reaction)) {
            promotion.setLikes(promotion.getLikes() - 1);
        } else if ("love".equalsIgnoreCase(reaction)) {
            promotion.setLoves(promotion.getLoves() - 1);
        }
        promotionRepository.save(promotion);
    }

    private void updateReaction(UserReaction existingReaction, String newReaction, Integer promotionId, String currentReaction) {
        // Cập nhật cảm xúc
        existingReaction.setReaction(newReaction);
        userReactionRepository.save(existingReaction);

        // Cập nhật số lượt cảm xúc
        Promotion promotion = promotionRepository.findById(promotionId).orElseThrow();
        if ("like".equalsIgnoreCase(currentReaction)) {
            promotion.setLikes(promotion.getLikes() - 1);
        } else if ("love".equalsIgnoreCase(currentReaction)) {
            promotion.setLoves(promotion.getLoves() - 1);
        }

        if ("like".equalsIgnoreCase(newReaction)) {
            promotion.setLikes(promotion.getLikes() + 1);
        } else if ("love".equalsIgnoreCase(newReaction)) {
            promotion.setLoves(promotion.getLoves() + 1);
        }
        promotionRepository.save(promotion);
    }
    public List<Promotion> findByPrivacyPublic() {
        return promotionRepository.findByPrivacy("public");
    }

    public List<Promotion> findByPrivacyForLoggedIn() {
        return promotionRepository.findByPrivacyIn(Arrays.asList("public", "unlisted"));
    }
 // Phương thức tìm kiếm các khuyến mãi dựa trên title và description
    public List<Promotion> searchPromotions(String query) {
        return promotionRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }
    public Promotion getPromotionByToken(String token) {
        return promotionRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
    }

}
