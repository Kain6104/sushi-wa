package com.mcnz.rps.controller;

import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.Notification;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.OrderDetail;
import com.mcnz.rps.model.User;
import com.mcnz.rps.service.CartService;
import com.mcnz.rps.service.ComboService;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.service.MenuItemsService;
import com.mcnz.rps.service.NotificationService;
import com.mcnz.rps.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mcnz.rps.model.CartItem;
import com.mcnz.rps.model.Combo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.HashMap;


@Controller
public class MenuController {

    @Autowired
    private MenuItemsService menuItemsService;
    
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ComboService comboService;  // Dịch vụ lấy combo
    
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CartService cartService;

    @GetMapping("/menu")
    public String showMenuPage(Model model, HttpSession session) {
        List<MenuItems> allItems = menuItemsService.getAllItems();
        
        List<Combo> combos = comboService.getAllCombos();  // Lấy danh sách combo từ dịch vụ
        List<MenuItems> topPricedItems = allItems.stream()
                .sorted(Comparator.comparingDouble(MenuItems::getPrice).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<MenuItems> menuItems = menuItemsService.getAllMenuItemsWithSoldQuantity();
        model.addAttribute("menuItems", menuItems);

        List<String> categories = Arrays.asList("Sushi", "Sashimi", "Cơm Cuộn","Salad", "Món chính", "Lẩu", "Khai vị", "Nước uống", "Combo");
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

        // Lọc và thêm các khuyến mãi vào mô hình
        Map<String, List<MenuItems>> categorizedItems = allItems.stream()
                .collect(Collectors.groupingBy(MenuItems::getCategory));

        // Đếm số lượng món ăn trong mỗi danh mục
        Map<String, Long> categoryCounts = categorizedItems.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (long) entry.getValue().size()));
        
        // Thêm combo vào Map categorizedItems
        Map<String, List<Combo>> categorizedCombos = combos.stream()
                .collect(Collectors.groupingBy(combo -> "Combo"));  // Sử dụng "Combo" cho tất cả các combo
        model.addAttribute("categorizedCombos", categorizedCombos);  // Thêm dữ liệu combo vào Model

        
        int cartItemCount = cartService.getCartItemCount(session);
        model.addAttribute("combos", combos);  // Truyền dữ liệu combo vào Model

        model.addAttribute("menuItems", topPricedItems);
        model.addAttribute("categories", categories);
        model.addAttribute("categorizedItems", categorizedItems);
        model.addAttribute("categoryCounts", categoryCounts);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("page", "menu");

        return "menu";
    }

    @PostMapping("/add-to-cart")
    public String addToCart(@RequestParam("id") Integer id, HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Lấy sản phẩm từ id
        MenuItems menuItem = menuItemsService.getItemById(id);
        if (menuItem != null) {
            // Thêm sản phẩm vào giỏ hàng
            cartService.addItemToCart(menuItem, session);

            // Thêm thông báo thành công
            String successMessage = "Thêm \"" + menuItem.getName() + "\" vào giỏ hàng thành công!";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        }

        // Lấy URL trước đó từ header Referer
        String referer = request.getHeader("Referer");
        String redirectUrl;

     // Kiểm tra URL trước đó và xác định nơi redirect
        if (referer != null && referer.contains("/checkout")) {
            redirectUrl = "/checkout"; // Giữ lại trang checkout nếu đang ở checkout
        } else if (referer != null && referer.contains("/product-details")) {
            redirectUrl = referer; // Quay lại trang chi tiết sản phẩm
        } else if (referer != null && referer.contains("/menu")) {
            redirectUrl = "/menu"; // Quay lại trang menu
        } else {
            redirectUrl = "/"; // Trang mặc định nếu không xác định được
        }

        // Cập nhật lại voucher trong session nếu voucher bị thay đổi
        session.removeAttribute("voucher");
        session.removeAttribute("voucherDiscount");

        // Redirect đến URL xác định
        return "redirect:" + redirectUrl;

    }


    @PostMapping("/add-combo-to-cart")
    public String addComboToCart(@RequestParam("id") Long id, HttpSession session) { // Tham số id là Long vì comboId là Long
        Combo combo = comboService.getComboById(id); // Lấy Combo từ dịch vụ dựa trên id
        if (combo != null) {
            cartService.addComboToCart(combo, session); // Thêm combo vào giỏ hàng
        }
        return "redirect:/menu"; // Quay lại trang menu sau khi thêm vào giỏ hàng
    }

 // CartController.java
    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        // Lấy danh sách các món trong giỏ hàng
        List<CartItem> cartItems = cartService.getCart(session);
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        // Tính tổng số lượng món trong giỏ hàng
        int cartItemCount = cartItems.stream()
                                     .mapToInt(CartItem::getQuantity)  // Tính tổng số lượng từ từng món
                                     .sum();

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
        // Định dạng lại giá và tổng tiền của mỗi sản phẩm trong giỏ hàng
        List<Map<String, String>> formattedCartItems = cartItems.stream()
                .map(item -> {
                    Map<String, String> formattedItem = new HashMap<>();
                    formattedItem.put("id", String.valueOf(item.getId()));
                    formattedItem.put("imageUrl", item.getImageUrl());
                    formattedItem.put("name", item.getName());
                    formattedItem.put("price", formatter.format(item.getPrice()));
                    formattedItem.put("quantity", String.valueOf(item.getQuantity()));
                    formattedItem.put("total", formatter.format(item.getPrice() * item.getQuantity()));
                    return formattedItem;
                })
                .collect(Collectors.toList());

        // Tính tổng giá trị giỏ hàng và định dạng lại
        double totalAmount = cartItems.stream()
                                      .mapToDouble(item -> item.getPrice() * item.getQuantity())
                                      .sum();
        String formattedTotalAmount = formatter.format(totalAmount);
        // Xác định trang hiện tại
        model.addAttribute("page", "index");
        // Thêm số lượng món vào model
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("cartItems", formattedCartItems);
        model.addAttribute("totalAmount", formattedTotalAmount);

        return "cart";
    }


    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam("id") Integer id, 
                                 @RequestParam("quantity") int quantity, 
                                 HttpSession session, 
                                 Model model) {
        // Kiểm tra số lượng trong khoảng từ 1 đến 30
        if (quantity < 1 || quantity > 50) {
            model.addAttribute("quantityError", "Số lượng phải nằm trong khoảng từ 1 đến 30.");
            return viewCart(model, session); // Quay lại trang giỏ hàng với thông báo lỗi
        }
        
        // Nếu số lượng hợp lệ, tiến hành cập nhật
        cartService.updateItemQuantity(id, quantity, session);
        return "redirect:/cart"; // Quay lại trang giỏ hàng sau khi cập nhật thành công
    }


    @PostMapping("/cart/remove")
    public String removeCartItem(@RequestParam("id") Integer id, HttpSession session) {
        cartService.removeItemFromCart(id, session);
        return "redirect:/cart";
    }
    @GetMapping("/index")
    public String showIndex(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
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

        // Lấy tất cả các món ăn
        List<MenuItems> allItems = menuItemsService.getAllItems();

        // Lọc và nhóm các món ăn theo danh mục
        Map<String, List<MenuItems>> categorizedItems = allItems.stream()
                .collect(Collectors.groupingBy(MenuItems::getCategory));

        // Lấy số lượng sản phẩm trong giỏ hàng
        int cartItemCount = cartService.getCartItemCount(session);

        // Gửi dữ liệu vào model
        model.addAttribute("categorizedItems", categorizedItems);
        model.addAttribute("cartItemCount", cartItemCount);

        // Xác định trang hiện tại
        model.addAttribute("page", "index");

        return "index"; // Trả về trang index
    }

    @GetMapping("/product-details/{token}")
    public String showProductDetailsPage(
            @PathVariable("token") String token,  // Sử dụng token thay vì itemId
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        // Lấy thông tin món ăn dựa trên token
        MenuItems menuItem = menuItemsService.getMenuItemByToken(token);  // Phương thức lấy món ăn theo token
        if (menuItem == null) {
            // Nếu không tìm thấy món ăn, thông báo lỗi và chuyển hướng về trang danh sách món ăn
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy món ăn!");
            return "redirect:/menu"; // Trang menu hoặc danh sách món ăn
        }

        List<MenuItems> allItems = menuItemsService.getAllItems();
        List<Combo> combos = comboService.getAllCombos();  // Lấy danh sách combo từ dịch vụ
        List<MenuItems> topPricedItems = allItems.stream()
                .sorted(Comparator.comparingDouble(MenuItems::getPrice).reversed())
                .limit(3)
                .collect(Collectors.toList());

        List<String> categories = Arrays.asList("Sushi", "Sashimi", "Cơm Cuộn", "Món chính", "Lẩu", "Khai vị", "Nước uống", "Combo");

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
        // Lọc và thêm các khuyến mãi vào mô hình
        Map<String, List<MenuItems>> categorizedItems = allItems.stream()
                .collect(Collectors.groupingBy(MenuItems::getCategory));

        // Thêm combo vào Map categorizedItems
        Map<String, List<Combo>> categorizedCombos = combos.stream()
                .collect(Collectors.groupingBy(combo -> "Combo"));  // Sử dụng "Combo" cho tất cả các combo
        model.addAttribute("categorizedCombos", categorizedCombos);  // Thêm dữ liệu combo vào Model

        // Lấy các món ăn tương tự, có thể dựa trên danh mục hoặc các tiêu chí khác
        List<MenuItems> similarItems = menuItemsService.getSimilarItems(menuItem.getCategory()); // Sử dụng service của MenuItems để lấy món ăn tương tự


        List<MenuItems> menuItems = menuItemsService.getAllMenuItemsWithSoldQuantity();
        model.addAttribute("menuItems", menuItems);
        int cartItemCount = cartService.getCartItemCount(session);
        model.addAttribute("combos", combos);  // Truyền dữ liệu combo vào Model
        model.addAttribute("menuItems", topPricedItems);
        model.addAttribute("categories", categories);
        model.addAttribute("categorizedItems", categorizedItems);
        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("page", "menu");

        // Thêm món ăn vào model để hiển thị trên giao diện
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("similarItems", similarItems);

        return "product-details"; // Tên file giao diện chi tiết món ăn
    } 
}