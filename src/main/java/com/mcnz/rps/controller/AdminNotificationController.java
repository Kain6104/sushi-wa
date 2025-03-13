package com.mcnz.rps.controller;

import com.mcnz.rps.model.Notification;
import com.mcnz.rps.model.User;
import com.mcnz.rps.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;  // Import Model

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AdminNotificationController {

    @Autowired
    private NotificationService notificationService;

    // Hiển thị trang danh sách thông báo
    @GetMapping("/admin/notifications")
    public String showNotifications(Model model) {
        List<Notification> notifications = notificationService.getAllNotifications();

        Map<String, List<Notification>> userNotifications = notifications.stream()
        	    .collect(Collectors.groupingBy(Notification::getUsername));

        // Thêm thông báo nhóm theo người dùng vào model
        model.addAttribute("userNotifications", userNotifications);
        return "admin/notifications";  // Trang hiển thị danh sách thông báo
    }
    // Hiển thị trang gửi thông báo
    @GetMapping("/admin/send-notification")
    public String showSendNotificationPage() {
        return "admin/send-notification";  // Trang hiển thị form gửi thông báo
    }
    

    // Gửi thông báo cho toàn hệ thống
    @PostMapping("/admin/send-global-notification")
    public String sendGlobalNotification(@RequestParam String message, 
                                         @RequestParam String link, 
                                         @RequestParam boolean isPopup, 
                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime scheduledTime) {
        notificationService.sendGlobalNotification(message, link, isPopup, scheduledTime);
        return "redirect:/admin/notifications";  // Redirect về trang danh sách thông báo
    }

    // Gửi thông báo cho một nhóm người dùng
    @PostMapping("/admin/send-group-notification")
    public String sendGroupNotification(@RequestParam String message, 
                                        @RequestParam String link, 
                                        @RequestParam boolean isPopup, 
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime scheduledTime, 
                                        @RequestParam String group) {
        notificationService.sendGroupNotification(message, link, isPopup, scheduledTime, group);
        return "redirect:/admin/notifications";  // Redirect về trang danh sách thông báo
    }

  

    // Hiển thị trang gửi thông báo cho người dùng cụ thể
    @GetMapping("/admin/send-user-notification")
    public String showSendUserNotificationPage(Model model) {
        // Lấy danh sách tất cả người dùng để hiển thị trong form
        List<User> users = notificationService.getAllUsers();  // Lấy danh sách người dùng
        model.addAttribute("users", users);
        return "admin/send-user-notification";  // Trang hiển thị form gửi thông báo
    }

    @PostMapping("/admin/send-user-notification")
    public String sendUserNotification(@RequestParam String message, 
                                       @RequestParam String link, 
                                       @RequestParam boolean isPopup, 
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime scheduledTime, 
                                       @RequestParam List<String> usernames) {
        // Gửi thông báo cho người dùng được chọn
        notificationService.sendNotificationToUsers(message, link, isPopup, scheduledTime, usernames);
        return "redirect:/admin/notifications";  // Redirect về trang danh sách thông báo
    }

}
