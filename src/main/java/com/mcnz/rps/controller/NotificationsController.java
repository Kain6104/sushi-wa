package com.mcnz.rps.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mcnz.rps.model.Notification;
import com.mcnz.rps.model.User;
import com.mcnz.rps.service.NotificationService;
import com.mcnz.rps.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class NotificationsController {

	@Autowired
	private UserService userService;
	
    @Autowired
    private NotificationService notificationService;

    // Bộ nhớ tạm thời lưu thông báo đã xóa
    private final Map<Long, Notification> deletedNotifications = new HashMap<>();
    
    @GetMapping("/notifications")
    public String getNotifications(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
        String username = (String) session.getAttribute("loggedInUser");

        if (username == null) {
            // Nếu người dùng chưa đăng nhập, chuyển hướng tới trang login
            return "redirect:/login";
        }

        // Lấy thông báo cho người dùng hiện tại
        List<Notification> notifications = notificationService.getUserNotifications(username);

        // Sắp xếp thông báo theo thời gian (mới nhất lên đầu)
        notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp())); // Sắp xếp giảm dần theo timestamp

        long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();

        Long userId = (Long) session.getAttribute("userId");

        if (userId != null) { // Kiểm tra xem người dùng đã đăng nhập hay chưa
            User user = userService.getUserById(userId);

            if (user.isForceRelogin()) { // Kiểm tra trạng thái buộc đăng nhập lại
                model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
                return "error"; // Chuyển hướng đến trang lỗi
            }
        }
        // Thêm thông báo và số lượng chưa đọc vào model
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationsCount", unreadCount);
        model.addAttribute("page", "notifications");

        return "notifications"; // Trả về trang danh sách thông báo
    }

    @GetMapping("/notifications/markAllRead")
    public String markAllRead(HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");

        if (username != null) {
            notificationService.markAllAsRead(username);
        }

        return "redirect:/notifications";
    }

    @GetMapping("/notifications/deleteAll")
    public String deleteAllNotifications(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("loggedInUser");

        if (username != null) {
            // Lấy tất cả thông báo của người dùng
            List<Notification> notifications = notificationService.getNotificationsByUsername(username);

            // Lưu trữ các thông báo đã xóa vào bộ nhớ tạm thời
            for (Notification notification : notifications) {
                deletedNotifications.put(notification.getId(), notification);
            }

            // Xóa tất cả thông báo
            notificationService.deleteAllNotifications(username);

            // Thêm thông báo vào model
         // Thêm thông báo và liên kết hoàn tác vào RedirectAttributes
            redirectAttributes.addFlashAttribute("undoMessage", "<strong style='color:red;'>Bạn đang xóa tất cả thông báo.</strong>");
            redirectAttributes.addFlashAttribute("undoLink", "/notifications/undoDeleteAll");  // Link hoàn tác
            redirectAttributes.addFlashAttribute("success", "Tất cả thông báo đã bị xóa. Bạn có thể hoàn tác việc xóa trong vòng 10 giây.");
        }

        return "redirect:/notifications";
    }

    // Phương thức hoàn tác xóa tất cả thông báo
    @GetMapping("/notifications/undoDeleteAll")
    public String undoDeleteAllNotifications(Model model) {
        // Khôi phục tất cả các thông báo đã xóa
        for (Notification notification : deletedNotifications.values()) {
            notificationService.saveNotification(notification);
        }

        // Xóa bộ nhớ tạm thời
        deletedNotifications.clear();

        model.addAttribute("success", "Hoàn tác xóa tất cả thông báo thành công.");
        return "redirect:/notifications";
    }
    @GetMapping("/notifications/markAsRead/{id}")
    public String markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return "redirect:/notifications";  // Sau khi xử lý xong, trở lại trang thông báo
    }

    @GetMapping("/notifications/delete/{id}")
    public String deleteNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Notification notification = notificationService.getNotificationById(id);

        if (notification != null) {
            // Lưu thông báo vào bộ nhớ tạm thời
            deletedNotifications.put(id, notification);

            // Đặt bộ đếm thời gian 10 giây để xóa hẳn
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (deletedNotifications.containsKey(id)) {
                        notificationService.deleteNotification(id);
                        deletedNotifications.remove(id);
                    }
                }
            }, 10000); // 10 giây

            // Thêm thông báo và liên kết hoàn tác vào flashAttributes
            redirectAttributes.addFlashAttribute("undoMessage", "Bạn đang xóa thông báo: " + notification.getMessage());
            redirectAttributes.addFlashAttribute("undoLink", "/notifications/undoDelete/" + id);
        }

        return "redirect:/notifications";
    }



    @GetMapping("/notifications/undoDelete/{id}")
    public String undoDeleteNotification(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (deletedNotifications.containsKey(id)) {
            Notification notification = deletedNotifications.remove(id);
            notificationService.saveNotification(notification); // Lưu lại vào cơ sở dữ liệu

            // Thêm thông báo thành công
            redirectAttributes.addFlashAttribute("successMessage", "Thông báo đã được hoàn tác thành công.");
        }

        return "redirect:/notifications";
    }

    @GetMapping("/notifications/markAsUnread/{id}")
    public String markAsUnread(@PathVariable Long id) {
        notificationService.markAsUnread(id);
        return "redirect:/notifications";  // Sau khi đánh dấu lại là chưa đọc, trở lại trang thông báo
    }
    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        String username = (String) session.getAttribute("loggedInUser");

        if (username != null) {
            List<Notification> notifications = notificationService.getUserNotifications(username);
            long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();
            System.out.println("Notifications count: " + unreadCount);  // In ra số lượng thông báo chưa đọc
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadNotificationsCount", unreadCount);
        }
    }

    @GetMapping("/fragments/header")
    public String getNotification(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
        String username = (String) session.getAttribute("loggedInUser");

        if (username == null) {
            // Nếu người dùng chưa đăng nhập, chuyển hướng tới trang login
            return "redirect:/login";
        }

        // Lấy thông báo cho người dùng hiện tại
        List<Notification> notifications = notificationService.getUserNotifications(username);

        // Sắp xếp thông báo theo thời gian (mới nhất lên đầu)
        notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp())); // Sắp xếp giảm dần theo timestamp

        long unreadCount = notifications.stream().filter(notification -> !notification.isRead()).count();

        // Thêm thông báo và số lượng chưa đọc vào model
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadNotificationsCount", unreadCount);

        return "fragments/header"; // Trả về trang danh sách thông báo
    }
}

