package com.mcnz.rps.controller;

import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.User;
import com.mcnz.rps.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.mcnz.rps.service.UserService; // Import dịch vụ UserService



@Controller
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService; // Tiêm UserService vào controller
    @GetMapping("/my_order")
    public String showMyOrdersPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            session.setAttribute("redirectUrl", "/checkout");
            return "redirect:/login";
        }

        List<Order> orders = orderService.getOrdersByCustomerId(userId);
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));   
        
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##");

        // Duyệt qua từng đơn hàng để định dạng các giá trị
        for (Order order : orders) {
            // Định dạng totalPrice và totalAmount
            order.setFormattedTotalPrice(decimalFormat.format(order.getTotalPrice()));
            order.setFormattedTotalAmount(decimalFormat.format(order.getTotalAmount()));
            
            // Định dạng voucherDiscount nếu có
            if (order.getVoucherDiscount() != null) {
                order.setFormattedVoucherDiscount(decimalFormat.format(order.getVoucherDiscount()));
            } else {
                order.setFormattedVoucherDiscount("0đ"); // Nếu voucherDiscount là null, hiển thị 0đ
            }
        }
        // Lấy thông tin người dùng
        User user = userService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
            return "error"; // Chuyển đến trang lỗi
        }

        model.addAttribute("page", "my_order");

        model.addAttribute("orders", orders);
        model.addAttribute("lastOrder", orders.isEmpty() ? null : orders.get(0));

        return "my_order";
    }
    @PostMapping("/orders/cancel/{orderCode}")
    public String cancelOrder(@PathVariable String orderCode,
                              @RequestParam String cancelReason,
                              @RequestParam(required = false) String customReason,
                              HttpSession session,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        Long userId = (Long) session.getAttribute("userId");
        boolean isAdmin = session.getAttribute("isAdmin") != null && (boolean) session.getAttribute("isAdmin");

        if (userId == null) {
            session.setAttribute("redirectUrl", "/my_order");
            return "redirect:/login";
        }

        // Lấy thông tin đơn hàng
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return isAdmin ? "redirect:/admin/orders" : "redirect:/my_order";
        }

        // Kiểm tra khả năng hủy đơn hàng
        String cancelReasonMessage = orderService.canCancelOrder(order, userId, isAdmin);
        if (cancelReasonMessage != null) {
            redirectAttributes.addFlashAttribute("error", cancelReasonMessage);
            return isAdmin ? "redirect:/admin/orders/details/" + orderCode : "redirect:/my_order";
        }

        // Xử lý lý do hủy
        String finalReason = "Khác".equals(cancelReason) ? customReason : cancelReason;
        if (finalReason == null || finalReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng cung cấp lý do hợp lệ để hủy đơn hàng.");
            return isAdmin ? "redirect:/admin/orders/details/" + orderCode : "redirect:/my_order";
        }

        // Lấy thông tin người dùng
        User user = order.getUser();
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng liên quan đến đơn hàng.");
            return isAdmin ? "redirect:/admin/orders/details/" + orderCode : "redirect:/my_order";
        }

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
            return "error"; // Chuyển đến trang lỗi
        }

        // Hoàn điểm cho người dùng (nếu có sử dụng điểm)
        int pointsReturned = 0;
        if (order.getPointsUsed() > 0) {
            pointsReturned = order.getPointsUsed();  // Lấy số điểm đã sử dụng
            user.setPoints(user.getPoints() + pointsReturned);  // Hoàn lại điểm cho người dùng
            userService.updateUser(user); // Cập nhật thông tin người dùng

            // Lưu điểm hoàn lại vào đơn hàng
            order.setPointsReturned(pointsReturned); // Lưu số điểm hoàn lại vào đơn hàng
        }

        // Cập nhật trạng thái đơn hàng
        order.setStatus("CANCELED");
        order.setCancelReason(finalReason);
        order.setCanceledBy(isAdmin ? "ADMIN" : "USER");
        order.setCanceledAt(LocalDateTime.now()); // Lưu thời gian hủy

        orderService.updateOrder(order);

        // Định dạng ngày giờ hủy
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        String formattedCanceledAt = order.getCanceledAt().format(formatter);

        // Hiển thị thông báo hủy đơn hàng cùng số điểm hoàn lại (nếu có)
        String successMessage = "Đơn hàng " + orderCode + " đã được hủy thành công vào lúc " + formattedCanceledAt + ".";
        if (pointsReturned > 0) {
            successMessage += "<br>" + pointsReturned + " điểm đã được hoàn lại.";
        }

        redirectAttributes.addFlashAttribute("success", successMessage);
        return isAdmin ? "redirect:/admin/orders/details/" + orderCode : "redirect:/my_order";
    }


}
