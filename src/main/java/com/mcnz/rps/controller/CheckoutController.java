package com.mcnz.rps.controller;

import com.mcnz.rps.model.CartItem;
import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.Notification;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserVoucher;
import com.mcnz.rps.model.UserVoucherUsage;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.service.CartService;
import com.mcnz.rps.service.EmailService;
import com.mcnz.rps.service.NotificationService;
import com.mcnz.rps.service.OrderService;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.service.UserVoucherService;
import com.mcnz.rps.service.UserVoucherUsageService;
import com.mcnz.rps.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.OrderDetail;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.mcnz.rps.repository.MenuItemsRepository;
import com.mcnz.rps.repository.UserRepository;
import com.mcnz.rps.repository.UserVoucherRepository;
import com.mcnz.rps.repository.OrderDetailRepository;
import org.springframework.transaction.annotation.Transactional;
import java.text.DecimalFormat;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private OrderService orderService;

    
    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserVoucherService userVoucherService;
    
    @Autowired
    private UserVoucherUsageService userVoucherUsageService;
    
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository; 
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Hiển thị trang checkout với các thông tin đã tính toán trước
    @GetMapping("/checkout")
    public String showCheckoutPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            session.setAttribute("redirectUrl", "/checkout");
            return "redirect:/login";
        }
        

        User user = userService.getUserById(userId);
        if (user == null) {
            return "error"; // Xử lý khi người dùng không tồn tại
        }
        
        // Lấy danh sách voucher đã lưu của user
        List<UserVoucher> allUserVouchers = userVoucherService.getUserVouchers(user);
        
     // Lọc danh sách voucher đủ điều kiện sử dụng
        List<UserVoucher> validVouchers = allUserVouchers.stream()
            .filter(uv -> {
                Voucher voucher = uv.getVoucher();
                LocalDateTime now = LocalDateTime.now();

                // Kiểm tra thời gian hiệu lực của voucher
                boolean isValidTime = (voucher.getStartDate() == null || !now.isBefore(voucher.getStartDate())) &&
                                      (voucher.getEndDate() == null || !now.isAfter(voucher.getEndDate()));

                // Kiểm tra voucher có thể sử dụng không
                boolean isValidUsage = voucher.canUse();

                // Kiểm tra số lần sử dụng của user
                boolean isNotMaxUsage = userVoucherUsageService.findByUserAndVoucher(user, voucher)
                    .map(usage -> usage.getUsageCount() < voucher.getMaxUsagePerUser())
                    .orElse(true);

                // Kiểm tra giá trị đơn hàng có đạt điều kiện tối thiểu
                double totalOrderAmount = cartService.calculateTotal(cartService.getCart(session));
                boolean meetsMinOrder = totalOrderAmount >= voucher.getMinOrderAmount();

                // Kiểm tra nếu voucher chỉ áp dụng cho một số món ăn
                boolean hasValidItem = true; // Mặc định nếu voucher áp dụng cho tất cả
                if (!voucher.isApplicableToAll()) {
                    List<MenuItems> applicableItems = voucher.getApplicableItems();

                    List<Long> cartItemIds = cartService.getCart(session).stream()
                        .map(cartItem -> cartItem.getMenuItem().getItemId().longValue())
                        .collect(Collectors.toList());

                    List<Long> applicableItemIds = applicableItems.stream()
                        .map(menuItem -> menuItem.getItemId().longValue())
                        .collect(Collectors.toList());

                    hasValidItem = cartItemIds.stream().anyMatch(applicableItemIds::contains);
                }

                return isValidTime && isValidUsage && isNotMaxUsage && meetsMinOrder && hasValidItem;
            })
            .collect(Collectors.toList());

        model.addAttribute("savedVouchers", validVouchers);


        // Lấy thông tin giỏ hàng và tính toán chi phí
        List<CartItem> cartItems = cartService.getCart(session);
        double cartTotal = cartService.calculateTotal(cartItems);
        double discountFromPoints = session.getAttribute("discountFromPoints") != null 
                                    ? (double) session.getAttribute("discountFromPoints") 
                                    : 0.0;
        double voucherDiscount = session.getAttribute("voucherDiscount") != null 
                                  ? (double) session.getAttribute("voucherDiscount") 
                                  : 0.0;
        double totalPayable = cartTotal - discountFromPoints - voucherDiscount;


        // Tạo mã đơn hàng ngẫu nhiên chỉ khi chưa có mã đơn hàng trong session
        String orderCode = (String) session.getAttribute("orderCode");
        if (orderCode == null) {
            Random random = new Random();
            int randomSuffix = random.nextInt(900) + 100; // Số ngẫu nhiên từ 100 đến 999
            orderCode = "SSW-" + (System.currentTimeMillis() % 100000000L) + randomSuffix;
            session.setAttribute("orderCode", orderCode);  // Lưu mã đơn hàng vào session
        }


        // Tính toán thời gian hết hạn thanh toán (30 phút từ thời điểm tạo đơn hàng)
        LocalDateTime paymentDeadline = LocalDateTime.now().plusMinutes(30);
        session.setAttribute("paymentDeadline", paymentDeadline);

        // Chuẩn bị định dạng số tiền
        DecimalFormat formatter = new DecimalFormat("#,###.##");
        String formattedCartTotal = formatter.format(cartTotal);
        String formattedDiscountFromPoints = formatter.format(discountFromPoints);
        String formattedVoucherDiscount = formatter.format(voucherDiscount);
        String formattedTotalPayable = formatter.format(Math.max(totalPayable, 0));

        // Thêm dữ liệu vào Model
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotalFormatted", formattedCartTotal);
        model.addAttribute("discountFromPointsFormatted", formattedDiscountFromPoints);
        model.addAttribute("voucherDiscountFormatted", formattedVoucherDiscount);
        model.addAttribute("totalPayableFormatted", formattedTotalPayable);

        model.addAttribute("orderCode", orderCode);

        // Gửi cả số thực để tránh lỗi tính toán nếu cần
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("discountFromPoints", discountFromPoints);
        model.addAttribute("voucherDiscount", voucherDiscount);
        model.addAttribute("totalPayable", Math.max(totalPayable, 0));

        model.addAttribute("availablePoints", user.getPoints());
        model.addAttribute("userName", user.getName());
        model.addAttribute("phoneNumber", user.getPhone());
        model.addAttribute("address", user.getAddress());

        return "checkout";
    }


    // Áp dụng điểm
    // Áp dụng điểm để giảm giá và xem trước mà không trừ điểm thực tế
    @PostMapping("/applyPoints")
    public String applyPoints(
        HttpSession session,
        @RequestParam int pointsToUse,
        @RequestParam(required = false) String voucherCode,
        RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);

        if (user == null || pointsToUse < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid points usage.");
            return "redirect:/checkout";
        }

        // Lấy giỏ hàng và tính tổng giá trị
        List<CartItem> cartItems = cartService.getCart(session);
        double cartTotal = cartService.calculateTotal(cartItems);

        // Kiểm tra điểm khả dụng
        int availablePoints = user.getPoints();
        if (pointsToUse > availablePoints) {
            redirectAttributes.addFlashAttribute("errorMessage", "You do not have enough points.");
            return "redirect:/checkout";
        }

        // Kiểm tra điểm dùng không vượt quá giá trị đơn hàng
        double maxPointsUsable = Math.floor(cartTotal / 500); // Số điểm tối đa có thể dùng dựa trên giá trị đơn hàng
        if (pointsToUse > maxPointsUsable) {
            pointsToUse = (int) maxPointsUsable;
        }

        // Tính giá trị giảm từ điểm
        double discountFromPoints = pointsToUse * 500;

        // Tính giảm giá từ voucher
        double voucherDiscount = 0.0;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            Voucher voucher = voucherService.findVoucherByCode(voucherCode);
            if (voucher != null && voucher.canUse()) {
                voucherDiscount = Math.min(voucher.getDiscountAmount(), cartTotal - discountFromPoints);
                session.setAttribute("voucher", voucher);
            }
        }

        double totalPayable = Math.max(cartTotal - discountFromPoints - voucherDiscount, 0);

        // Lưu thông tin vào session
        session.setAttribute("pointsToUse", pointsToUse);
        session.setAttribute("discountFromPoints", discountFromPoints);
        session.setAttribute("totalPayable", totalPayable);

        return "redirect:/checkout";
    }



    @PostMapping("/applyVoucher")
    public String applyVoucher(HttpSession session, @RequestParam String voucherCode, RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng đăng nhập để áp dụng phiếu giảm giá.");
            return "redirect:/login";
        }

        User user = userService.findById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin người dùng.");
            return "redirect:/checkout";
        }

        Voucher voucher = voucherService.findVoucherByCode(voucherCode);
      
        // Kiểm tra thời gian sử dụng của voucher
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Voucher chưa đến thời gian sử dụng.");
            return "redirect:/checkout";
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Voucher đã hết hạn sử dụng.");
            return "redirect:/checkout";
        }
        if (voucher == null || !voucher.canUse()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiếu giảm giá không hợp lệ hoặc đã hết lượt sử dụng.");
            return "redirect:/checkout";
        }

        Optional<UserVoucherUsage> usageOpt = userVoucherUsageService.findByUserAndVoucher(user, voucher);
        if (usageOpt.isPresent() && usageOpt.get().getUsageCount() >= voucher.getMaxUsagePerUser()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn đã sử dụng phiếu giảm giá này tối đa số lần cho phép.");
            return "redirect:/checkout";
        }

        // Lấy giỏ hàng từ session
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/checkout";
        }

        // Kiểm tra điều kiện đơn hàng tối thiểu
        double totalOrderAmount = cartService.calculateTotal(cartItems);
        if (totalOrderAmount < voucher.getMinOrderAmount()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Đơn hàng của bạn không đạt mức tối thiểu để áp dụng voucher này. Vui lòng mua thêm sản phẩm.");
            return "redirect:/checkout";
        }

        // Kiểm tra nếu voucher chỉ áp dụng cho một số món ăn cụ thể
        if (!voucher.isApplicableToAll()) {
            List<MenuItems> applicableItems = voucher.getApplicableItems();

            List<Long> cartItemIds = cartItems.stream()
                .map(cartItem -> cartItem.getMenuItem().getItemId().longValue())
                .collect(Collectors.toList());

            List<Long> applicableItemIds = applicableItems.stream()
                .map(menuItem -> menuItem.getItemId().longValue())
                .collect(Collectors.toList());

            boolean hasValidItem = cartItemIds.stream().anyMatch(applicableItemIds::contains);
            if (!hasValidItem) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                        "Voucher này không áp dụng cho một số sản phẩm.");
                return "redirect:/checkout";
            }
        }

        // Áp dụng giảm giá dựa trên loại voucher
        double discount = 0.0;
        if (voucher.getDiscountType().equals("PERCENT")) {
            discount = totalOrderAmount * (voucher.getDiscountAmount() / 100.0);
            if (voucher.getMaxDiscount() != null) {
                discount = Math.min(discount, voucher.getMaxDiscount());
            }
        } else if (voucher.getDiscountType().equals("AMOUNT")) {
            discount = voucher.getDiscountAmount();
        }

        session.setAttribute("voucher", voucher);
        session.setAttribute("voucherDiscount", discount);

        redirectAttributes.addFlashAttribute("successMessage", "Phiếu giảm giá đã được áp dụng thành công!");
        return "redirect:/checkout";
    }

    @PostMapping("/removeVoucher")
    public String removeVoucher(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("voucher");
        session.removeAttribute("voucherDiscount");
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy voucher.");
        return "redirect:/checkout";
    }

    @Transactional
    @PostMapping("/processCheckout")
    public String processCheckout(
            @RequestParam("fullname") String fullname,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("deliveryDate") String deliveryDate,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "note", required = false) String note,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để tiếp tục.");
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Người dùng không tồn tại.");
            return "redirect:/login";
        }

        // Lấy thông tin giỏ hàng từ session
        List<CartItem> cartItems = cartService.getCart(session);
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giỏ hàng của bạn đang trống.");
            return "redirect:/checkout";
        }
  
        // Lấy mã đơn hàng từ session
        String orderCode = (String) session.getAttribute("orderCode");


        // Tính toán tổng tiền và giảm giá
        double cartTotal = cartService.calculateTotal(cartItems);
        double discountFromPoints = session.getAttribute("discountFromPoints") != null
                                    ? (double) session.getAttribute("discountFromPoints")
                                    : 0.0;
        double voucherDiscount = session.getAttribute("voucherDiscount") != null
                                 ? (double) session.getAttribute("voucherDiscount")
                                 : 0.0;
        double totalPayable = Math.max(cartTotal - discountFromPoints - voucherDiscount, 0);

        // Ensure totalPayable is valid and not null
        if (totalPayable < 0) {
            totalPayable = 0.0;  // Default value if the calculation goes wrong
        }

        int pointsUsed = (int) (discountFromPoints / 500);
        if (pointsUsed > 0) {
            user.setPoints(user.getPoints() - pointsUsed);
            userService.saveUser(user);
        }

        Voucher voucher = (Voucher) session.getAttribute("voucher");

        // Tạo đơn hàng và lưu vào cơ sở dữ liệu
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setDeliveryTime(LocalDateTime.parse(deliveryDate)); // Chuyển chuỗi thành LocalDateTime
        order.setStatus("PENDING");
        order.setTotalPrice(cartTotal);
        order.setAddress(address);
        order.setPaymentMethod(paymentMethod);
        order.setPhone(phoneNumber);
        order.setPointsUsed(pointsUsed);
        order.setTotalAmount(totalPayable);
        order.setVoucherCode(voucher != null ? voucher.getCode() : null);
        order.setVoucherDiscount(voucherDiscount); // Lưu giá trị voucher vào đơn hàng
        order.setName(fullname);
        order.setUsername(user.getUsername());
        order.setNote(note);
        order.setOrderCode(orderCode); // Sử dụng mã đơn hàng đã lưu trong session


        orderService.saveOrder(order);

        // Tính điểm tích lũy và lưu vào session
        int earnedPoints = 0; // Khai báo và khởi tạo mặc định
        if ("DELIVERED".equals(order.getStatus())) {
            earnedPoints = (int) (totalPayable / 10000); // 1 điểm = 10,000 VND
            user.setPoints(user.getPoints() + earnedPoints);
            userService.saveUser(user);
        }

        for (CartItem item : cartItems) {
            if (item.getMenuItem() == null) {
                throw new IllegalStateException("MenuItem cannot be null in CartItem");
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order); // Gán mối quan hệ với đơn hàng
            detail.setMenuItems(item.getMenuItem()); // Sử dụng đối tượng MenuItems đầy đủ
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPrice());
            detail.setOriginalPrice(item.getPrice()); // Lưu giá trị món ăn tại thời điểm thanh toán
            orderDetailRepository.save(detail); // Lưu từng chi tiết đơn hàng vào cơ sở dữ liệu
        }

        // Lưu thông tin vào bảng user_voucher_usage nếu có voucher
        if (voucher != null) {
            Optional<UserVoucherUsage> usageOpt = userVoucherUsageService.findByUserAndVoucher(user, voucher);
            UserVoucherUsage usage;

            if (usageOpt.isPresent()) {
                usage = usageOpt.get();
                usage.incrementUsage(); // Tăng số lần sử dụng
            } else {
                usage = new UserVoucherUsage();
                usage.setUser(user);
                usage.setVoucher(voucher);
                usage.setUsageCount(1); // Lần sử dụng đầu tiên
            }
            Optional<UserVoucher> userVoucherOpt = userVoucherRepository.findByUserAndVoucher(user, voucher);
            
            if (userVoucherOpt.isPresent()) {
                UserVoucher userVoucher = userVoucherOpt.get();
                userVoucher.setIsUsed(true); // Đánh dấu đã dùng
                userVoucherRepository.save(userVoucher); // Lưu vào database
            }

            // Kiểm tra rằng order đã có orderId trước khi liên kết
            if (order.getOrderId() != null) {
                usage.setOrder(order); // Liên kết với đơn hàng
                userVoucherUsageService.saveUserVoucherUsage(usage);
            } else {
                throw new IllegalStateException("Order ID is missing for this order.");
            }

            voucherService.updateVoucherUsage(voucher); // Cập nhật số lượng voucher còn lại
        }


        // Xóa thông tin giỏ hàng khỏi session
        session.removeAttribute("cartItems");
        session.removeAttribute("discountFromPoints");
        session.removeAttribute("voucherDiscount");
        session.removeAttribute("totalPayable");
        session.removeAttribute("voucher");
        session.removeAttribute("orderCode");


     // Xóa giỏ hàng khỏi service (nếu có)
        cartService.clearCart(session);
        // Lưu thông tin vào session
        session.setAttribute("orderId", order.getOrderId());
        session.setAttribute("orderCode2", order.getOrderCode());
        session.setAttribute("totalPayable", totalPayable);
        session.setAttribute("availablePoints", user.getPoints());
        session.setAttribute("earnedPoints", earnedPoints);
        session.setAttribute("customerName", fullname); // Tên khách hàng
        session.setAttribute("customerPhone", phoneNumber); // Số điện thoại
        session.setAttribute("customerAddress", address); // Địa chỉ
        session.setAttribute("orderDate", order.getOrderDate().toString()); // Ngày đặt hàng
        session.setAttribute("orderedItems", cartItems); // Lưu danh sách món ăn vào session
     // Xóa mã đơn hàng khỏi session sau khi thanh toán thành công

        // Lưu thêm thông tin vào session
        session.setAttribute("pointsUsed", pointsUsed); // Điểm đã sử dụng
        session.setAttribute("voucherUsed", voucher != null ? voucher.getCode() : null); // Voucher đã sử dụng
        session.setAttribute("voucherValue", voucherDiscount); // Giá trị voucher đã sử dụng
        session.setAttribute("deliveryDate", deliveryDate); // Ngày giao hàng
        session.setAttribute("note", note); // Ghi chú
        session.setAttribute("paymentMethod", paymentMethod); // Phương thức thanh toán
        // Định dạng lại totalPayable
        DecimalFormat df = new DecimalFormat("#,###");
        String formattedTotalPayable = df.format(totalPayable); // Format as currency

        StringBuilder orderDetailsBuilder = new StringBuilder();
        orderDetailsBuilder.append("<h3>Thông tin Món Ăn:</h3>");
        orderDetailsBuilder.append("<table border='1' style='width: 100%; border-collapse: collapse; text-align: center;'>");
        orderDetailsBuilder.append("<tr><th>Hình ảnh</th><th>Tên Món</th><th>SL</th><th>Đơn Giá</th><th>Tổng Giá</th></tr>");

        for (CartItem item : cartItems) {
            orderDetailsBuilder.append("<tr>");
            orderDetailsBuilder.append("<td><img src='" + item.getMenuItem().getImageUrl() + "' width='50' height='50'></td>"); // Thêm hình ảnh món ăn
            orderDetailsBuilder.append("<td>" + item.getMenuItem().getName() + "</td>");
            orderDetailsBuilder.append("<td>" + item.getQuantity() + "</td>");
            orderDetailsBuilder.append("<td>" + item.getPrice() + " VND</td>");
            orderDetailsBuilder.append("<td>" + (item.getPrice() * item.getQuantity()) + " VND</td>");
            orderDetailsBuilder.append("</tr>");
        }

        orderDetailsBuilder.append("</table>");

        String emailContent = "<!DOCTYPE html>" +
                "<html lang='vi'>" +
                "<head>" +
                "    <meta charset='UTF-8'>" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "    <title>Hóa Đơn " + order.getOrderCode() + "</title>" +
                "    <link rel='icon' href='https://gigamall.com.vn/data/2024/05/10/14290213_shushiwa.jpg' type='image/x-icon'>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 20px; color: #333; }" +
                "        .email-container { max-width: 800px; margin: 0 auto; background: #fff; border-radius: 10px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); padding: 20px; }" +
                "        .header { text-align: center; color: #d32f2f; padding: 20px; }" +
                "        .header h1 { font-size: 28px; }" +
                "        .table { width: 100%; margin-top: 20px; border-collapse: collapse; }" +
                "        .table th, .table td { border: 1px solid #ddd; padding: 10px; text-align: left; }" +
                "        .table th { background-color: #f4f4f4; }" +
                "        .footer { text-align: center; font-size: 12px; color: #666; margin-top: 20px; padding: 10px 0; background-color: #f1f1f1; }" +
                "        .total { margin-top: 30px; font-size: 18px; color: #333; background-color: #f5f5f5; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); text-align: right; }" +
                "        .total p { margin: 10px 0; }" +
                "        .total strong { color: #d32f2f; font-weight: bold; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='email-container'>" +
                "        <div class='header'>" +
                "            <h1>HÓA ĐƠN</h1>" +
                "            <div>Sushi Wa</div>" +
                "        </div>" +
                "        <div class='info'>" +
                "            <p><strong>Tên khách hàng:</strong> " + fullname + "</p>" +
                "            <p><strong>Số điện thoại:</strong> " + phoneNumber + "</p>" +
                "            <p><strong>Địa chỉ giao hàng:</strong> " + address + "</p>" +
                "            <p><strong>Mã đơn hàng:</strong> " + order.getOrderCode() + "</p>" +
                "            <p><strong>Ngày đặt hàng:</strong> " + order.getOrderDate() + "</p>" +
                "        </div>" +
                orderDetailsBuilder.toString() +  // Add order details here
                "        <div class='total'>" +
                "            <p><strong>Tổng tiền thanh toán:</strong> " + formattedTotalPayable + " VND</p>" +
                "        </div>" +
                "        <div class='footer'>" +
                "            <p>&copy; 2025 Cửa hàng của chúng tôi. All rights reserved.</p>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>";

        emailService.sendEmail(user.getEmail(), "Xác nhận Đơn Hàng", emailContent);
        redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderCode() +
                ". Thông tin chi tiết đã được gửi qua email.");

        // Tạo thông báo với liên kết chi tiết đơn hàng
        String orderDetails = "Đơn hàng #" + order.getOrderCode() + " của bạn đã được đặt thành công. Tổng tiền: " + formattedTotalPayable + " VND.";
        Notification notification = new Notification();
        notification.setUsername(user.getUsername());
        notification.setMessage(orderDetails);
        notification.setRead(false);
        notification.setTimestamp(LocalDateTime.now());

        // Tạo liên kết chi tiết đơn hàng và gán vào thông báo
        String orderDetailsLink = "http://localhost:8080/my_order?modal=orderDetailsModal_" + order.getOrderCode();
        notification.setLink(orderDetailsLink);

        notificationService.createNotification(notification);
        return "redirect:/checkoutSuccess";
    }




    @GetMapping("/checkoutSuccess")
    public String showCheckoutSuccess(HttpSession session, Model model) {
        Long orderId = (Long) session.getAttribute("orderId");
        String orderCode2 = (String) session.getAttribute("orderCode2");
        Double totalPayable = (Double) session.getAttribute("totalPayable");
        Integer availablePoints = (Integer) session.getAttribute("availablePoints");
        Integer earnedPoints = (Integer) session.getAttribute("earnedPoints");

        String customerName = (String) session.getAttribute("customerName");
        String customerPhone = (String) session.getAttribute("customerPhone");
        String customerAddress = (String) session.getAttribute("customerAddress");

        // Đọc orderDate từ session và chuyển từ chuỗi về LocalDateTime
        String orderDateString = (String) session.getAttribute("orderDate");
        LocalDateTime orderDate = orderDateString != null ? LocalDateTime.parse(orderDateString) : null;

        List<CartItem> orderedItems = (List<CartItem>) session.getAttribute("orderedItems");

        // Tính tổng giá trị các món ăn
        double totalPrice = orderedItems.stream()
                                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                                        .sum();
        // Các thông tin bổ sung
        Integer pointsUsed = (Integer) session.getAttribute("pointsUsed"); // Điểm đã sử dụng
        String voucherUsed = (String) session.getAttribute("voucherUsed"); // Voucher đã sử dụng
        Double voucherValue = (Double) session.getAttribute("voucherValue"); // Giá trị voucher đã sử dụng
        String deliveryDate = (String) session.getAttribute("deliveryDate"); // Ngày giao hàng
        String note = (String) session.getAttribute("note"); // Ghi chú
        String paymentMethod = (String) session.getAttribute("paymentMethod"); // Phương thức thanh toán

        // Đưa dữ liệu vào model để hiển thị trong view
        model.addAttribute("orderId", orderId);
        model.addAttribute("orderCode", orderCode2);
        model.addAttribute("totalPayable", totalPayable);
        model.addAttribute("availablePoints", availablePoints);
        model.addAttribute("earnedPoints", earnedPoints);

        model.addAttribute("customerName", customerName);
        model.addAttribute("customerPhone", customerPhone);
        model.addAttribute("customerAddress", customerAddress);
        model.addAttribute("orderDate", orderDate);
        model.addAttribute("orderedItems", orderedItems);

        // Các thông tin bổ sung cần hiển thị
        model.addAttribute("pointsUsed", pointsUsed); // Điểm đã sử dụng
        model.addAttribute("voucherUsed", voucherUsed); // Voucher đã sử dụng
        model.addAttribute("voucherValue", voucherValue); // Giá trị voucher
        model.addAttribute("deliveryDate", deliveryDate); // Ngày giao hàng
        model.addAttribute("note", note); // Ghi chú
        model.addAttribute("paymentMethod", paymentMethod); // Phương thức thanh toán
        model.addAttribute("totalPrice", totalPrice); // Thêm tổng giá trị vào model

        return "checkoutSuccess"; // Trả về view tên `checkoutSuccess`
    }
   
}
