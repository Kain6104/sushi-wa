package com.mcnz.rps.controller;

import com.mcnz.rps.model.Promotion;
import com.mcnz.rps.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.time.format.DateTimeFormatter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;
import com.mcnz.rps.model.Comment;
import com.mcnz.rps.model.Notification;
import com.mcnz.rps.service.CommentService;
import com.mcnz.rps.service.NotificationService;
import com.mcnz.rps.model.User;
import com.mcnz.rps.service.UserService;
import java.time.LocalDateTime;
import com.mcnz.rps.repository.CommentRepository;

@Controller
@RequestMapping
public class PromotionController {

    @Autowired
    private PromotionService promotionService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    // Hiển thị danh sách khuyến mãi cho người dùng
    @GetMapping("/promotions")
    public String showPromotions(Model model, HttpSession session) {
        List<Promotion> allPromotions = promotionService.getAllPromotions();
        LocalDate today = LocalDate.now();

        // Định dạng ngày tháng
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String username = (String) session.getAttribute("loggedInUser");
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) { // Kiểm tra xem người dùng đã đăng nhập hay chưa
            User user = userService.getUserById(userId);

            if (user.isForceRelogin()) { // Kiểm tra trạng thái buộc đăng nhập lại
                model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
                return "error"; // Chuyển hướng đến trang lỗi
            }

            // Lấy thông báo cho người dùng đã đăng nhập
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();

            // Sắp xếp thông báo theo thời gian (mới nhất lên đầu)
            notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

            // Thêm thông báo và số lượng chưa đọc vào model
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadNotificationsCount", unreadCount);
        }
        // Phân loại và định dạng các khuyến mãi
        // Khuyến mãi đang diễn ra
        List<Promotion> activePromotions = allPromotions.stream()
                .filter(promotion -> 
                    !promotion.getStartDate().isAfter(today) && 
                    !promotion.getEndDate().isBefore(today))
                .peek(promotion -> {
                    promotion.setFormattedStartDate(promotion.getStartDate().format(formatter));
                    promotion.setFormattedEndDate(promotion.getEndDate().format(formatter));
                })
                .toList();

        // Khuyến mãi đã kết thúc
        List<Promotion> expiredPromotions = allPromotions.stream()
                .filter(promotion -> promotion.getEndDate().isBefore(today))
                .peek(promotion -> {
                    promotion.setFormattedStartDate(promotion.getStartDate().format(formatter));
                    promotion.setFormattedEndDate(promotion.getEndDate().format(formatter));
                })
                .toList();

        // Khuyến mãi sắp diễn ra
        List<Promotion> upcomingPromotions = allPromotions.stream()
                .filter(promotion -> promotion.getStartDate().isAfter(today))
                .peek(promotion -> {
                    promotion.setFormattedStartDate(promotion.getStartDate().format(formatter));
                    promotion.setFormattedEndDate(promotion.getEndDate().format(formatter));
                })
                .toList();

        // Thêm danh sách khuyến mãi vào model
        model.addAttribute("activePromotions", activePromotions);
        model.addAttribute("expiredPromotions", expiredPromotions);
        model.addAttribute("upcomingPromotions", upcomingPromotions);
        model.addAttribute("page", "promotions");

        
        return "promotions";
    }

    @PostMapping("/promotion/react")
    public String reactToPromotion(@RequestParam Integer promotionId,
                                    @RequestParam String reaction,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }

        String result = promotionService.handleReaction(userId, promotionId, reaction);

        if ("reacted".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "Cảm xúc đã được ghi nhận.");
        } else if ("unreacted".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "Bạn đã hủy cảm xúc.");
        } else if ("updated".equals(result)) {
            redirectAttributes.addFlashAttribute("successMessage", "Cảm xúc đã được cập nhật.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi. Vui lòng thử lại.");
        }

        return "redirect:/promotions"; // Thay đổi thành route hợp lệ
    }
    @GetMapping("/promotion/{token}")
    public String showPromotionDetails(@PathVariable String token, Model model, HttpSession session) {
        Promotion promotion = promotionService.getPromotionByToken(token);
        List<Comment> comments = commentService.getCommentsByPromotionId(promotion.getId());
        model.addAttribute("promotion", promotion);
        model.addAttribute("comments", comments);
        String username = (String) session.getAttribute("loggedInUser");
        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) { // Kiểm tra xem người dùng đã đăng nhập hay chưa
            User user = userService.getUserById(userId);

            if (user.isForceRelogin()) { // Kiểm tra trạng thái buộc đăng nhập lại
                model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
                return "error"; // Chuyển hướng đến trang lỗi
            }

            // Lấy thông báo cho người dùng đã đăng nhập
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();

            // Sắp xếp thông báo theo thời gian (mới nhất lên đầu)
            notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));

            // Thêm thông báo và số lượng chưa đọc vào model
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadNotificationsCount", unreadCount);
        }
        model.addAttribute("page", "promotions");

        return "promotion-details"; // Trả về view promotion-details.html
    }
   
    @PostMapping("/promotion/{token}/comment")
    public String addComment(@PathVariable String token, 
                             @RequestParam String content,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để bình luận.");
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        Promotion promotion = promotionService.getPromotionByToken(token); // Sử dụng token để lấy promotion

        if (promotion != null) {
            commentService.addComment(content, user, promotion);
        }

        return "redirect:/promotion/" + token; // Quay lại trang promotion sử dụng token
    }

    // Sửa bình luận
    @PostMapping("/promotion/comment/{commentId}/edit")
    public String editComment(@PathVariable Long commentId,
                              @RequestParam String newContent,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để chỉnh sửa bình luận.");
            return "redirect:/login";
        }

        try {
            commentService.editComment(commentId, userId, newContent);
            redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được chỉnh sửa.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chỉ có thể chỉnh sửa bình luận của mình.");
        }

        return "redirect:/promotions";
    }

    @PostMapping("/promotion/{token}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable String token,
                                @PathVariable Long commentId,
                                @RequestParam Integer promotionId, // Promotion ID từ hidden field trong form
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để xóa bình luận.");
            return "redirect:/login";
        }

        try {
            // Xóa bình luận
            commentService.deleteComment(commentId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được xóa.");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn chỉ có thể xóa bình luận của mình.");
        }

        // Quay lại trang promotion theo token
        return "redirect:/promotion/" + token;
    }

    @PostMapping("/promotion/{token}/comment/{commentId}/reply")
    public String replyToComment(@PathVariable String token, 
                                 @PathVariable Long commentId,
                                 @RequestParam String content,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để trả lời bình luận.");
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/login";
        }

        // Tìm promotion theo token
        Promotion promotion = promotionService.getPromotionByToken(token);

        // Tìm bình luận cha
        Optional<Comment> parentCommentOpt = commentRepository.findById(commentId);
        if (!parentCommentOpt.isPresent()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bình luận.");
            return "redirect:/promotion/" + token;
        }
        Comment parentComment = parentCommentOpt.get();

        // Tạo phản hồi dưới bình luận cha
        Comment reply = new Comment();
        reply.setContent(content);
        reply.setParentComment(parentComment); // Liên kết phản hồi với bình luận cha
        reply.setUser(user);  // Liên kết người dùng
        reply.setPromotion(promotion); // Liên kết với promotion
        reply.setCreatedAt(LocalDateTime.now());

        try {
            commentRepository.save(reply); // Lưu phản hồi

            // Thêm phản hồi vào danh sách replies của bình luận cha
            parentComment.getReplies().add(reply);
            commentRepository.save(parentComment); // Lưu lại bình luận cha với replies đã cập nhật

            redirectAttributes.addFlashAttribute("successMessage", "Trả lời bình luận thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi trả lời bình luận.");
        }

        // Quay lại trang chi tiết promotion
        return "redirect:/promotion/" + token;
    }



}