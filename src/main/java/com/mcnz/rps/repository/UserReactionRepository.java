package com.mcnz.rps.repository;

import com.mcnz.rps.model.UserReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserReactionRepository extends JpaRepository<UserReaction, Long> {
    Optional<UserReaction> findByUserIdAndPromotionId(Long userId, Integer promotionId);
    void deleteByPromotionId(Integer promotionId);

}
