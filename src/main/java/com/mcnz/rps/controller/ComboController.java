package com.mcnz.rps.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mcnz.rps.model.Combo;
import com.mcnz.rps.model.User;

import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.Notification;
import com.mcnz.rps.service.ComboService;
import com.mcnz.rps.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.mcnz.rps.service.MenuItemsService;
import com.mcnz.rps.service.NotificationService;

import org.springframework.util.StringUtils;


@Controller
@RequestMapping
public class ComboController {
    @Autowired
    private MenuItemsService menuItemsService;
    @Autowired
    private ComboService comboService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    @GetMapping("/admin/combos")
    public String showCombos(Model model, HttpSession session) {
        model.addAttribute("combos", comboService.getAllCombos());
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
        return "admin/combo-list";
    }



    @GetMapping("/admin/combos/create")
    public String showCreateComboForm(Model model, HttpSession session) {
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
        model.addAttribute("combo", new Combo());
        model.addAttribute("menuItems", menuItemsService.getAllItems()); // Lấy danh sách món ăn
        return "admin/create-combo";
    }


    @PostMapping("/admin/combos/create")
    public String createCombo(@ModelAttribute Combo combo, 
                              @RequestParam("menuItemIds") List<Integer> menuItemIds, 
                              @RequestParam("imageUrl") String imageUrl) {

        // Chỉ lưu đường dẫn URL hình ảnh vào combo (không cần tải lên tệp nữa)
        combo.setImageUrl(imageUrl); // Lưu đường dẫn URL vào combo

        // Lấy danh sách món ăn đã chọn
        List<MenuItems> selectedItems = menuItemsService.getAllItems().stream()
                .filter(item -> menuItemIds.contains(item.getItemId()))
                .toList();
        combo.setMenuItems(selectedItems);

        // Lưu combo vào cơ sở dữ liệu
        comboService.saveCombo(combo);

        // Chuyển hướng về danh sách combo
        return "redirect:/admin/combos";
    }

    @GetMapping("/admin/combos/edit/{id}")
    public String showEditComboForm(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Combo combo = comboService.getComboById(id);
        if (combo != null) {
            model.addAttribute("combo", combo);
            model.addAttribute("menuItems", menuItemsService.getAllItems()); // Lấy danh sách món ăn

            // Lấy thông tin trang trước đó từ header "Referer"
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/admin/products")) {
                model.addAttribute("refererPage", referer);
            } else {
                model.addAttribute("refererPage", "/admin/combos"); // Đường dẫn mặc định nếu không có referer
            }

            return "admin/edit-combo"; // Trả về trang chỉnh sửa combo
        } else {
            return "redirect:/admin/combos"; // Nếu không tìm thấy, quay lại danh sách combo
        }
    }

    @PostMapping("/admin/combos/edit/{id}")
    public String updateCombo(@PathVariable("id") Long id, 
                              @ModelAttribute Combo updatedCombo, 
                              @RequestParam("menuItemIds") List<Integer> menuItemIds, 
                              @RequestParam("imageUrl") String imageUrl,
                              @RequestParam(value = "refererPage", required = false) String refererPage, 
                              RedirectAttributes redirectAttributes) {
        Combo existingCombo = comboService.getComboById(id);
        if (existingCombo != null) {
            // Danh sách món ăn cũ
            List<MenuItems> oldItems = existingCombo.getMenuItems();
            List<Integer> oldItemIds = oldItems.stream()
                                               .map(MenuItems::getItemId)
                                               .toList();

            // Cập nhật thông tin combo
            existingCombo.setComboName(updatedCombo.getComboName());
            existingCombo.setPrice(updatedCombo.getPrice());
            existingCombo.setDescription(updatedCombo.getDescription());
            existingCombo.setImageUrl(imageUrl);

            // Lấy danh sách món ăn mới
            List<MenuItems> selectedItems = menuItemsService.getAllItems().stream()
                    .filter(item -> menuItemIds.contains(item.getItemId()))
                    .toList();
            existingCombo.setMenuItems(selectedItems);

            // Lưu combo đã chỉnh sửa
            comboService.updateCombo(id, existingCombo);

            // So sánh món ăn đã thêm/xóa
            List<MenuItems> addedItems = selectedItems.stream()
                    .filter(item -> !oldItemIds.contains(item.getItemId()))
                    .toList();
            List<MenuItems> removedItems = oldItems.stream()
                    .filter(item -> !menuItemIds.contains(item.getItemId()))
                    .toList();

            // Tạo thông báo
            StringBuilder message = new StringBuilder();
            message.append("Combo \"")
                   .append(existingCombo.getComboName())
                   .append("\" với ID ")
                   .append(existingCombo.getComboId())
                   .append(" đã được cập nhật thành công.");
            
            if (!addedItems.isEmpty()) {
                message.append("<br>Món ăn được thêm vào: ");
                message.append(addedItems.stream()
                                         .map(MenuItems::getName)
                                         .collect(Collectors.joining(", ")));
            }
            if (!removedItems.isEmpty()) {
                message.append("<br>Món ăn được xóa khỏi combo: ");
                message.append(removedItems.stream()
                                           .map(MenuItems::getName)
                                           .collect(Collectors.joining(", ")));
            }

            redirectAttributes.addFlashAttribute("successMessage", message.toString());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy combo.");
        }

        // Chuyển hướng quay lại trang trước đó (nếu có), hoặc về danh sách combo
        if (refererPage != null && refererPage.contains("/admin/products")) {
            return "redirect:" + refererPage;
        }
        return "redirect:/admin/combos";
    }



    @GetMapping("/admin/combos/delete/{id}")
    public String deleteCombo(@PathVariable Long id) {
        comboService.deleteCombo(id);
        return "redirect:/admin/combos";
    }
    @GetMapping("/combo-details/{token}")
    public String getComboDetails(@PathVariable String token, Model model, HttpSession session) {
        Combo combo = comboService.getComboByToken(token);

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
        if (combo == null) {
            model.addAttribute("error", "Không tìm thấy combo.");
            return "error";
        }

      

        model.addAttribute("combo", combo);
        return "combo-details";
    }


}
