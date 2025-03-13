package com.mcnz.rps.service;

import com.mcnz.rps.model.Notification;
import com.mcnz.rps.model.User;
import com.mcnz.rps.repository.NotificationRepository;
import com.mcnz.rps.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;  // Inject UserService
    // Lấy thông báo cho người dùng
    public List<Notification> getUserNotifications(String username) {
        return notificationRepository.findByUsername(username);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();  // Trả về danh sách người dùng từ cơ sở dữ liệu
    }
    

    public void createNotification(String username, String message, String link) {
        Notification notification = new Notification();
        notification.setUsername(username);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setRead(false);
        notification.setTimestamp(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    


    // Lấy tất cả thông báo
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    // Gửi thông báo toàn hệ thống
    public void sendGlobalNotification(String message, String link, boolean isPopup, LocalDateTime scheduledTime) {
        List<User> users = userRepository.findAll();  // Lấy tất cả người dùng
        for (User user : users) {
            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setLink(link);
            notification.setUsername(user.getUsername());
            notification.setRead(false);  // Chưa đọc
            notification.setPopup(isPopup);  // Set popup flag
            notification.setTimestamp(scheduledTime);
            notificationRepository.save(notification);  // Lưu thông báo
        }
    }

    // Gửi thông báo cho một nhóm người dùng
    public void sendGroupNotification(String message, String link, boolean isPopup, LocalDateTime scheduledTime, String group) {
        // Gửi thông báo cho nhóm người dùng
        List<String> usersInGroup = getUsersInGroup(group);
        for (String user : usersInGroup) {
            Notification notification = new Notification();
            notification.setUsername(user);
            notification.setMessage(message);
            notification.setLink(link);
            notification.setRead(false);
            notification.setTimestamp(scheduledTime);
            notificationRepository.save(notification);
        }
    }

  

    // Lấy danh sách người dùng theo nhóm
    private List<String> getUsersInGroup(String group) {
        // Giả sử bạn có phương thức lấy người dùng theo nhóm
        return List.of("user1", "user2");  // Thay bằng danh sách người dùng thực tế
    }

    // Đánh dấu tất cả thông báo là đã đọc
    public void markAllAsRead(String username) {
        List<Notification> notifications = notificationRepository.findByUsername(username);

        for (Notification notification : notifications) {
            notification.setRead(true);  // Đánh dấu là đã đọc
            notificationRepository.save(notification);  // Cập nhật thông báo
        }
    }

    // Xóa tất cả thông báo của người dùng
    public void deleteAllNotifications(String username) {
        List<Notification> notifications = notificationRepository.findByUsername(username);
        notificationRepository.deleteAll(notifications);  // Dùng phương thức deleteAll() có sẵn trong JpaRepository
    }
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAsUnread(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        notification.setRead(false);
        notificationRepository.save(notification);
    }


    // Phương thức gửi thông báo cho một số người dùng cụ thể
    public void sendNotificationToUsers(String message, String link, boolean isPopup, LocalDateTime scheduledTime, List<String> usernames) {
        // Lấy danh sách người dùng từ UserService
        List<User> users = userService.getAllUsers();

        // Lọc những người dùng trong danh sách đã chọn
        for (User user : users) {
            if (usernames.contains(user.getUsername())) {  // Kiểm tra nếu người dùng trong danh sách đã chọn
                Notification notification = new Notification();
                notification.setUsername(user.getUsername());
                notification.setMessage(message);
                notification.setLink(link);
                notification.setRead(false);  // Đánh dấu là chưa đọc
                notification.setPopup(isPopup);  // Set popup flag
                notification.setTimestamp(scheduledTime);
                notificationRepository.save(notification);  // Lưu thông báo vào cơ sở dữ liệu
            }
        }
    }
    public void createNotification(Notification notification) {
        notificationRepository.save(notification);
    }
 // Phương thức lấy thông báo theo ID
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo với ID: " + id));
    }

    // Phương thức lưu thông báo
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }
    private Map<Long, Notification> tempDeletedNotifications = new HashMap<>();

    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));
        tempDeletedNotifications.put(id, notification);
        notificationRepository.delete(notification);
    }
    public void undoDeleteNotification(Long id) {
        Notification notification = tempDeletedNotifications.remove(id);
        if (notification != null) {
            notificationRepository.save(notification);
        }
    }

    // Lấy tất cả thông báo của người dùng
    public List<Notification> getNotificationsByUsername(String username) {
        // Giả sử bạn có một phương thức trong repository để lấy thông báo theo tên người dùng
        return notificationRepository.findByUsername(username);
    }

  
}

