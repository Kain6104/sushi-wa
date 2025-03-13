package com.mcnz.rps.service;

import com.mcnz.rps.model.Comment;
import com.mcnz.rps.model.Promotion;
import com.mcnz.rps.model.User;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {
	@Autowired
	private UserService userService;


    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByPromotionId(Integer promotionId) {
        return commentRepository.findByPromotionIdOrderByCreatedAtAsc(promotionId);
    }

    public Comment addComment(String content, User user, Promotion promotion) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setUser(user);
        comment.setPromotion(promotion);
        return commentRepository.save(comment);
    }

    public Comment editComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // Check if the user is allowed to edit the comment
        if (!comment.getUser().getCustomerId().equals(userId)) {
            throw new SecurityException("You can only edit your own comments");
        }

        // Update the comment content
        comment.setContent(newContent);
        comment.setEdited(true); // Optional: Mark the comment as edited
        return commentRepository.save(comment); // Save the updated comment
    }

    public Comment replyToComment(Long parentCommentId, Long userId, String content) {
        // Tìm bình luận cha
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));

        // Lấy người dùng từ userId
        User user = userService.findById(userId);

        // Tạo phản hồi mới
        Comment reply = new Comment();
        reply.setContent(content);
        reply.setUser(user);
        reply.setParentComment(parentComment);  // Đây là điểm quan trọng, phản hồi này phải thuộc về bình luận cha
        reply.setPromotion(parentComment.getPromotion()); // Gán promotion từ bình luận cha
        reply.setCreatedAt(LocalDateTime.now());

        // Thêm phản hồi vào danh sách replies của bình luận cha
        parentComment.getReplies().add(reply);

        // Lưu phản hồi vào database (Lưu phản hồi và cũng lưu cập nhật cho bình luận cha)
        commentRepository.save(reply);
        commentRepository.save(parentComment);  // Lưu lại bình luận cha với danh sách replies đã được cập nhật

        return reply;
    }



    public void deleteComment(Long commentId, Long userId) {
        // Lấy thông tin bình luận
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // Kiểm tra quyền xóa bình luận
        if (!comment.getUser().getCustomerId().equals(userId)) {
            throw new SecurityException("You can only delete your own comments");
        }

        // Xóa bình luận
        commentRepository.delete(comment);
    }

    @Autowired
    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    // Add this method to find a comment by its ID
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }
}
