package com.mcnz.rps.repository;

import com.mcnz.rps.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPromotionIdOrderByCreatedAtAsc(Integer promotionId);
    Optional<Comment> findById(Long id);
    void delete(Comment comment);
    List<Comment> findByParentCommentId(Long parentCommentId);

}
