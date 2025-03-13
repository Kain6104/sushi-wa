package com.mcnz.rps.controller;

import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.Promotion;
import com.mcnz.rps.model.Combo;
import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserVoucherUsage;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.service.OrderService;
import com.mcnz.rps.service.MenuItemsService;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.service.UserVoucherUsageService;
import com.mcnz.rps.service.VoucherService;
import com.mcnz.rps.service.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.mcnz.rps.service.PromotionService;
import com.mcnz.rps.service.SiteStatisticsService;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
@Controller
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private MenuItemsService menuItemsService;

    @Autowired
    private UserService UserService;
  
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private SessionManager sessionManager;
    
    @Autowired
    private SiteStatisticsService siteStatisticsService;

    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private UserVoucherUsageService userVoucherUsageService;

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "admin".equalsIgnoreCase(role);
    }

    @GetMapping("/admin")
    public String redirectToDashboard(HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
            return "redirect:/error";
        }
        return "redirect:/admin/dashboard"; // Điều hướng đến dashboard
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(
            @RequestParam(value = "view", defaultValue = "day") String view,
            @RequestParam(value = "specificDate", required = false) String specificDate,
            @RequestParam(name = "startDateTime", required = false) String startDateTimeStr,
            @RequestParam(name = "endDateTime", required = false) String endDateTimeStr,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // Check admin permissions
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
            return "redirect:/error";
        }

        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }
        // Add site statistics to the model
        try {
            model.addAttribute("totalVisits", siteStatisticsService.getTotalVisits());
            model.addAttribute("onlineUsers", siteStatisticsService.getOnlineUsers());
            model.addAttribute("visitsLastMinute", siteStatisticsService.getVisitsLastMinute());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to retrieve site statistics.");
            return "redirect:/error";
        }
       

     // Determine the time range
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

        try {
            // Nếu người dùng chọn khoảng thời gian cụ thể
            if (startDateTimeStr != null && endDateTimeStr != null && !startDateTimeStr.isEmpty() && !endDateTimeStr.isEmpty()) {
                startDateTime = LocalDateTime.parse(startDateTimeStr, formatter);
                endDateTime = LocalDateTime.parse(endDateTimeStr, formatter);
            } else {
                // Nếu không, xác định thời gian dựa trên chế độ xem
                switch (view.toLowerCase()) {
                    case "day":
                        LocalDate date = (specificDate != null && !specificDate.isEmpty())
                                ? LocalDate.parse(specificDate)
                                : LocalDate.now();
                        startDateTime = date.atStartOfDay();
                        endDateTime = date.atTime(23, 59, 59);
                        break;
                    case "yesterday":
                        startDateTime = LocalDate.now().minusDays(1).atStartOfDay();
                        endDateTime = LocalDate.now().minusDays(1).atTime(23, 59, 59);
                        break;
                    case "week":
                        startDateTime = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
                        endDateTime = LocalDate.now().atTime(23, 59, 59);
                        break;
                    case "month":
                        startDateTime = LocalDate.now().withDayOfMonth(1).atStartOfDay();
                        endDateTime = LocalDate.now().atTime(23, 59, 59);
                        break;
                    case "lastm":
                        startDateTime = LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay();
                        endDateTime = LocalDate.now().withDayOfMonth(1).minusDays(1).atTime(23, 59, 59);
                        break;
                    case "year":
                        startDateTime = LocalDate.now().withDayOfYear(1).atStartOfDay();
                        endDateTime = LocalDate.now().atTime(23, 59, 59);
                        break;
                    case "lasty":
                    	startDateTime = LocalDate.now().minusYears(1).withDayOfYear(1).atStartOfDay();
                        endDateTime = LocalDate.now().withDayOfYear(1).minusDays(1).atTime(23, 59, 59);
                        break;
                    case "all":
                        startDateTime = LocalDate.of(2024, 11, 20).atStartOfDay(); // All time
                        endDateTime = LocalDate.now().atTime(23, 59, 59);
                        break;
                    default:
                        model.addAttribute("error", "Invalid view mode.");
                        return "error";
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Chế độ xem hoặc ngày không hợp lệ.");
            return "redirect:/error";
        }

        List<UserVoucherUsage> usedVouchers = userVoucherUsageService.getUsedVouchers(startDateTime, endDateTime);
        model.addAttribute("usedVouchers", usedVouchers);
      
        Map<String, Double> voucherDiscounts = orderService.getTotalDiscountPerVoucher(startDateTime, endDateTime);
        
        // Debug: In dữ liệu ra console để kiểm tra
        System.out.println("=== DEBUG: Voucher Discounts ===");
        for (Map.Entry<String, Double> entry : voucherDiscounts.entrySet()) {
            System.out.println("Voucher: " + entry.getKey() + " - Tổng giảm: " + entry.getValue());
        }

        model.addAttribute("voucherDiscounts", voucherDiscounts);
        Map<String, Double> pointsUsedMap = orderService.getTotalPointsUsedPerUser(startDateTime, endDateTime);
        if (pointsUsedMap == null) {
            pointsUsedMap = new HashMap<>();
        }
        model.addAttribute("pointsUsedMap", pointsUsedMap);

        // Lấy tổng số điểm đã dùng theo từng người dùng
        Map<String, Double> pointsUsedPerUser = orderService.getTotalPointsUsedPerUser(startDateTime, endDateTime);

        // Debug: In dữ liệu ra console để kiểm tra
        System.out.println("=== DEBUG: Points Used ===");
        for (Map.Entry<String, Double> entry : pointsUsedPerUser.entrySet()) {
            System.out.println("User: " + entry.getKey() + " - Tổng điểm đã dùng: " + entry.getValue());
        }

        model.addAttribute("pointsUsedPerUser", pointsUsedPerUser);

     // Thêm thông tin vào model để hiển thị trên HTML
        String timeRangeMessage = "Bạn đang xem từ " + startDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) 
                                + " đến " + endDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        model.addAttribute("timeRangeMessage", timeRangeMessage);
     // Fetch sales and revenue data
        double totalRevenue;
        int totalSales;
        List<Object[]> menuItemSales;

        try {
            totalRevenue = orderService.getTotalRevenueForDay(startDateTime, endDateTime);
            totalSales = orderService.getTotalSalesForDay(startDateTime, endDateTime);
            menuItemSales = orderService.getMenuItemSales(startDateTime, endDateTime);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to retrieve sales data.");
            return "redirect:/error";
        }

        // Calculate total quantity and revenue from menu items
        long totalQuantity = menuItemSales.stream()
                .mapToLong(item -> ((Number) item[1]).longValue())
                .sum();

        double totalRevenueItem = menuItemSales.stream()
                .mapToDouble(item -> ((Number) item[2]).doubleValue())
                .sum();
     // Calculate average revenue per sale (totalRevenue / totalSales)
        double averageRevenuePerSale = (totalSales != 0) ? totalRevenue / totalSales : 0.0;
        
        // Unified Decimal Format for currency display
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.'); // Use '.' as the thousand separator
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##", symbols);
        model.addAttribute("menuItemSales", menuItemSales);

        // Format total revenues
        String formattedTotalRevenue = decimalFormat.format(totalRevenue);
        String formattedTotalRevenueItem = decimalFormat.format(totalRevenueItem);
        String formattedAverageRevenuePerSale = decimalFormat.format(averageRevenuePerSale);
        // Fetch canceled orders count
        int totalCanceledSales = orderService.getTotalCanceledOrders(startDateTime, endDateTime);


        // Fetch canceled orders data
        List<Object[]> canceledMenuItemSales;
        double totalCanceledRevenueItem; // Tổng doanh thu từng món trong đơn hàng đã hủy
        try {
            canceledMenuItemSales = orderService.getCanceledMenuItemSales(startDateTime, endDateTime);

            // Calculate total canceled quantity and revenue
            int totalCanceledQuantity = canceledMenuItemSales.stream()
                    .mapToInt(item -> item[1] instanceof Integer ? (Integer) item[1] : ((Long) item[1]).intValue())
                    .sum();

            double totalCanceledRevenue = canceledMenuItemSales.stream()
                    .mapToDouble(item -> item[2] instanceof Double ? (Double) item[2] : Double.parseDouble(item[2].toString()))
                    .sum();

            // Calculate total revenue of each item in canceled orders
            totalCanceledRevenueItem = canceledMenuItemSales.stream()
                    .mapToDouble(item -> item[2] instanceof Double ? (Double) item[2] : Double.parseDouble(item[2].toString()))
                    .sum();

            // Count total canceled sales

            
            // Format canceled revenue
            String formattedTotalCanceledRevenue = decimalFormat.format(totalCanceledRevenue);
            String formattedTotalCanceledRevenueItem = decimalFormat.format(totalCanceledRevenueItem);
         // Chuẩn bị dữ liệu cho biểu đồ
            List<String> itemNames = menuItemSales.stream()
                    .map(item -> (String) item[0])
                    .collect(Collectors.toList());

            List<Long> itemQuantities = menuItemSales.stream()
                    .map(item -> ((Number) item[1]).longValue())
                    .collect(Collectors.toList());

            List<Double> itemRevenues = menuItemSales.stream()
                    .map(item -> ((Number) item[2]).doubleValue())
                    .collect(Collectors.toList());

            model.addAttribute("itemNames", itemNames);
            model.addAttribute("itemQuantities", itemQuantities);
            model.addAttribute("itemRevenues", itemRevenues);
            try {
                // Lấy doanh thu theo phương thức thanh toán, không bao gồm đơn hàng hủy
                Map<String, Double> revenueByPaymentMethod = orderService.getRevenueByPaymentMethod(startDateTime, endDateTime);

                // Tính tổng doanh thu
                double totalRevenueByMethod = revenueByPaymentMethod.values().stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

                String formattedTotalRevenueByMethod = decimalFormat.format(totalRevenueByMethod);

                // Đưa vào model
                model.addAttribute("revenueByPaymentMethod", revenueByPaymentMethod);
                model.addAttribute("totalRevenueByPaymentMethod", formattedTotalRevenueByMethod);

            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Unable to retrieve payment method revenue data.");
                return "redirect:/error";
            }
       
            // Add formatted canceled data to model
            model.addAttribute("canceledMenuItemSales", canceledMenuItemSales);
            model.addAttribute("totalCanceledQuantity", totalCanceledQuantity);
            model.addAttribute("totalCanceledRevenue", formattedTotalCanceledRevenue);
            model.addAttribute("totalCanceledRevenueItem", formattedTotalCanceledRevenueItem);
            model.addAttribute("formattedAverageRevenuePerSale", formattedAverageRevenuePerSale);

            model.addAttribute("totalCanceledSales", totalCanceledSales);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to retrieve canceled sales data.");
            return "redirect:/error";
        }
        //doanh thu dự kiến
        double projectedRevenue; // Doanh thu dự kiến

        try {
            totalRevenue = orderService.getTotalRevenueForDay(startDateTime, endDateTime); // Doanh thu "DELIVERED"
            projectedRevenue = orderService.getProjectedRevenue(startDateTime, endDateTime); // Doanh thu dự kiến
            totalSales = orderService.getTotalSalesForDay(startDateTime, endDateTime); // Tổng số đơn hàng
            menuItemSales = orderService.getMenuItemSales(startDateTime, endDateTime); // Thống kê món theo trạng thái "DELIVERED"
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to retrieve sales data.");
            return "redirect:/error";
        }

     // Tính tổng điểm đã dùng và tổng giá trị giảm giá từ voucher
        double totalPointsUsed = 0.0;
        double totalVoucherDiscountUsed = 0.0;
        model.addAttribute("totalPointsUsed2", totalPointsUsed);

        try {
            totalPointsUsed = orderService.getTotalPointsUsed(startDateTime, endDateTime);
            totalVoucherDiscountUsed = orderService.getTotalVoucherDiscountUsed(startDateTime, endDateTime);
            
            // Nhân tổng điểm đã dùng với 500
            totalPointsUsed *= 500;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lấy dữ liệu điểm và giảm giá từ voucher.");
            return "redirect:/error";
        }


        // Tính tổng giảm giá
        double totalDiscount = totalVoucherDiscountUsed + totalPointsUsed;

        // Thêm thuộc tính vào model để gửi tới view
        model.addAttribute("totalPointsUsed", totalPointsUsed);
        model.addAttribute("totalVoucherDiscountUsed", totalVoucherDiscountUsed);
        model.addAttribute("totalDiscount", totalDiscount);
        // Định dạng các giá trị
        String formattedTotalDiscount= decimalFormat.format(totalDiscount);
        String formattedTotalPointsUsed = decimalFormat.format(totalPointsUsed);
        String formattedTotalVoucherDiscountUsed = decimalFormat.format(totalVoucherDiscountUsed);

        // Thêm vào model
        model.addAttribute("totalPointsUsed", formattedTotalPointsUsed);
        model.addAttribute("totalVoucherDiscountUsed", formattedTotalVoucherDiscountUsed);
        model.addAttribute("totalDiscount", formattedTotalDiscount);

        // Format doanh thu dự kiến
        String formattedProjectedRevenue = decimalFormat.format(projectedRevenue);
        model.addAttribute("projectedRevenue", formattedProjectedRevenue);


        // Add attributes to the model
        model.addAttribute("view", view);
        model.addAttribute("specificDate", specificDate != null ? specificDate : LocalDate.now().toString());
        model.addAttribute("totalRevenue", formattedTotalRevenue);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("menuItemSales", menuItemSales);
        model.addAttribute("totalQuantity", totalQuantity);
        model.addAttribute("totalRevenueItem", formattedTotalRevenueItem);
        
        // Lấy dữ liệu doanh thu trong 7 ngày qua (có thể chỉnh khoảng thời gian)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfPeriod = now.truncatedTo(ChronoUnit.DAYS).minusDays(6); // Lấy từ 6 ngày trước

        // Lấy dữ liệu từ service
        List<Object[]> revenueData = orderService.getRevenueByDayAndHour(startOfPeriod, now);

        // Dùng Map để lưu dữ liệu doanh thu theo ngày & giờ
        Map<String, Map<Integer, Double>> revenueMap = new LinkedHashMap<>();

        for (Object[] record : revenueData) {
            if (record[0] instanceof java.sql.Date && record[1] instanceof Number && record[2] instanceof Number) {
                String day = record[0].toString(); // YYYY-MM-DD
                int hour = ((Number) record[1]).intValue();
                double revenue = ((Number) record[2]).doubleValue();

                revenueMap.putIfAbsent(day, new HashMap<>());
                revenueMap.get(day).put(hour, revenue);
            }
        }

        
        // Chuẩn bị dữ liệu cho frontend
        List<String> days = new ArrayList<>(revenueMap.keySet()); // Danh sách ngày
        List<List<Double>> dailyHourlyRevenues = new ArrayList<>();

        for (String day : days) {
            List<Double> hourlyRevenues = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                hourlyRevenues.add(revenueMap.get(day).getOrDefault(i, 0.0)); // Mặc định 0 nếu không có dữ liệu
            }
            dailyHourlyRevenues.add(hourlyRevenues);
        }

        // Kiểm tra dữ liệu trước khi đưa vào model
        System.out.println("Days: " + days);
        System.out.println("Hourly Revenues: " + dailyHourlyRevenues);

        List<User> usersList = UserService.getAllUsers(); // Đổi tên biến để tránh trùng lặp
        Map<String, String> userTokens = new HashMap<>();

        for (User u : usersList) { // Đổi tên biến trong vòng lặp để tránh trùng lặp với biến `user` ở trên
            userTokens.put(u.getUsername(), u.getSecureToken());
        }

        model.addAttribute("userTokens", userTokens);


        model.addAttribute("userTokens", userTokens);


        // Đưa vào model
        model.addAttribute("days", days);
        model.addAttribute("dailyHourlyRevenues", dailyHourlyRevenues);


        
        return "admin/dashboard";
    }
    
    @GetMapping("/admin/products")
    public String showProductsPage(HttpSession session, Model model, 
            
            RedirectAttributes redirectAttributes) {
    	  if (!isAdmin(session)) {
              redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
              return "redirect:/error";
          }
    	  List<MenuItems> allItems = menuItemsService.getAllItems();
          
          List<MenuItems> topPricedItems = allItems.stream()
                  .sorted(Comparator.comparingDouble(MenuItems::getPrice).reversed())
                  .limit(3)
                  .collect(Collectors.toList());

          List<String> categories = Arrays.asList("Sushi", "Sashimi", "Cơm Cuộn","Salad", "Món chính", "Lẩu", "Khai vị", "Nước uống", "Combo");
          Long userId = (Long) session.getAttribute("userId");
          User user = UserService.getUserById(userId);

          if (user.isForceRelogin()) {
              model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
    
              return "error";
          }

          // Lọc và thêm các khuyến mãi vào mô hình
          Map<String, List<MenuItems>> categorizedItems = allItems.stream()
                  .collect(Collectors.groupingBy(MenuItems::getCategory));

          // Đếm số lượng món ăn trong mỗi danh mục
          Map<String, Long> categoryCounts = categorizedItems.entrySet().stream()
                  .collect(Collectors.toMap(Map.Entry::getKey, entry -> (long) entry.getValue().size()));
          
          // Thêm combo vào Map categorizedItems
                  model.addAttribute("menuItems", topPricedItems);
          model.addAttribute("categories", categories);
          model.addAttribute("categorizedItems", categorizedItems);
          model.addAttribute("categoryCounts", categoryCounts);
          model.addAttribute("page", "menu");


        List<MenuItems> menuItems = menuItemsService.getAllMenuItems();
        model.addAttribute("menuItems", menuItems);
        return "admin/products";
    }

    @PostMapping("/admin/products")
    public String addMenuItem(HttpSession session, @ModelAttribute MenuItems menuItems,  RedirectAttributes redirectAttributes, Model model) {
    	  if (!isAdmin(session)) {
              redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
              return "redirect:/error";
          }
    	  Long userId = (Long) session.getAttribute("userId");
          User user = UserService.getUserById(userId);

          if (user.isForceRelogin()) {
              model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
    
              return "error";
          }
        menuItemsService.saveMenuItem(menuItems);
        return "redirect:/admin/products";
    }
    @PostMapping("/admin/products/add")
    public String addMenuItem(@ModelAttribute MenuItems menuItem, RedirectAttributes redirectAttributes) {
        menuItemsService.saveMenuItem(menuItem);
        redirectAttributes.addFlashAttribute("successMessage", "Thêm món ăn thành công.");
        return "redirect:/admin/products";
    }
    @GetMapping("/admin/products/delete/{itemId}")
    public String deleteMenuItem(@PathVariable Integer itemId, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
        MenuItems menuItem = menuItemsService.getItemById(itemId);
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }
        if (menuItem != null) {
            String deletedItemName = menuItem.getName(); // Lấy tên món ăn
            List<Combo> combos = menuItem.getCombos(); // Lấy danh sách combo liên quan

            if (!combos.isEmpty()) {
                // Nếu món ăn thuộc combo, hiển thị thông báo với đường link đến combo
                StringBuilder message = new StringBuilder("\"" + deletedItemName + 
                    "\" còn thuộc các combo sau. Vui lòng chỉnh sửa:");
                for (Combo combo : combos) {
                	message.append("<br><a href='/admin/combos/edit/")
                    .append(combo.getComboId())
                    .append("' class='btn btn-warning'>Chỉnh sửa ")
                    .append(combo.getComboName())
                    .append(" (ID: ").append(combo.getComboId()).append(")")
                    .append("</a>");

                }
                redirectAttributes.addFlashAttribute("errorMessage", message.toString());
            } else {
                try {
                    menuItemsService.deleteMenuItemById(itemId, redirectAttributes); // Gọi phương thức xóa
                    redirectAttributes.addFlashAttribute("successMessage", 
                        "Món ăn \"" + deletedItemName + "\" đã được xóa thành công.");
                } catch (RuntimeException e) {
                    // Hiển thị thông báo lỗi không mong muốn
                    redirectAttributes.addFlashAttribute("errorMessage", 
                        "Đã xảy ra lỗi khi xóa món ăn. Vui lòng thử lại sau.");
                }
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Không tìm thấy món ăn với ID: " + itemId);
        }
        return "redirect:/admin/products";
    }


    // Cập nhật món ăn
    @PostMapping("/admin/products/edit/{itemId}")
    public String editMenuItem(@PathVariable Integer itemId, @ModelAttribute MenuItems menuItem, RedirectAttributes redirectAttributes) {
        menuItem.setItemId(itemId); // Gán lại ID để đảm bảo cập nhật đúng bản ghi
        menuItemsService.updateMenuItem(menuItem);
        redirectAttributes.addFlashAttribute("successMessage", "Món ăn đã được chỉnh sửa thành công!");
        return "redirect:/admin/products";
    }
    @GetMapping("/admin/orders")
    public String showOrders(@RequestParam(value = "statusFilter", defaultValue = "") String statusFilter, 
                              HttpSession session, Model model) {
        // Kiểm tra quyền truy cập của admin
        if (!isAdmin(session)) {
            model.addAttribute("error", "Quyền của bạn không được phép truy cập trang web này");
            return "error";
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        // Lấy danh sách đơn hàng từ service
        List<Order> orders;

        if (statusFilter.isEmpty()) {
            // Nếu không có bộ lọc trạng thái, lấy tất cả đơn hàng
            orders = orderService.findAllOrders();
        } else {
            // Nếu có bộ lọc trạng thái, lấy đơn hàng theo trạng thái
            orders = orderService.findOrdersByStatus(statusFilter);
        }

        // Sắp xếp danh sách đơn hàng theo ngày đặt hàng giảm dần
        orders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));

        // Định dạng tiền
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##"); // Định dạng với dấu chấm ngăn cách hàng nghìn
        for (Order order : orders) {
            order.setFormattedTotalPrice(decimalFormat.format(order.getTotalPrice())); // Định dạng totalPrice
            order.setFormattedTotalAmount(decimalFormat.format(order.getTotalAmount())); // Định dạng totalAmount
        }

        // Thêm danh sách vào model
        model.addAttribute("orders", orders);
        model.addAttribute("statusFilter", statusFilter); // Thêm statusFilter vào model để dễ dàng giữ trạng thái lọc

        return "admin/orders";
    }


    @GetMapping("/admin/orders/details/{orderCode}")
    public String viewOrderDetails(@PathVariable String orderCode, Model model, HttpSession session) {
        // Kiểm tra quyền admin
        if (!isAdmin(session)) {
            model.addAttribute("error", "Quyền của bạn không được phép truy cập trang web này");
            return "error";
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        // Lấy thông tin đơn hàng theo mã đơn hàng
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            model.addAttribute("error", "Không tìm thấy thông tin đơn hàng với mã: " + orderCode);
            return "error";
        }

        // Định dạng tiền
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##"); // Sử dụng định dạng với dấu chấm ngăn cách hàng nghìn
        if (order.getTotalPrice() != null) {
            order.setFormattedTotalPrice(decimalFormat.format(order.getTotalPrice())); // Định dạng totalPrice
        }
        if (order.getTotalAmount() != null) {
            order.setFormattedTotalAmount(decimalFormat.format(order.getTotalAmount())); // Định dạng totalAmount
        }
        // Định dạng voucherDiscount nếu có
        if (order.getVoucherDiscount() != null) {
            order.setFormattedVoucherDiscount(decimalFormat.format(order.getVoucherDiscount()));
        } else {
            order.setFormattedVoucherDiscount("0đ"); // Nếu voucherDiscount là null, hiển thị 0đ
        }
        // Đưa thông tin vào model để hiển thị trong file order-details.html
        model.addAttribute("order", order);

        return "admin/order-details"; // Trỏ tới file order-details.html
    }



    @GetMapping("/admin/orders/confirm/{orderCode}")
    public String confirmOrder(HttpSession session, @PathVariable String orderCode, HttpServletRequest request, RedirectAttributes redirectAttributes, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/error";
        }
              Order order = orderService.getOrderByCode(orderCode);
        if (order != null) {
            order.setStatus("DELIVERED");
            orderService.saveOrder(order);

            // Lấy thông tin người dùng và cộng điểm cho họ
            User user = order.getUser();
            if (user != null) {
                // Tính điểm thưởng (1 điểm = 10,000 VND)
                int earnedPoints = (int) (order.getTotalAmount() / 10000);
                user.setPoints(user.getPoints() + earnedPoints);
                UserService.saveUser(user);  // Lưu thông tin người dùng với điểm mới
                System.out.println("Cộng " + earnedPoints + " điểm cho người dùng: " + user.getUsername());
            }

            // Set a flash attribute to indicate successful confirmation
            redirectAttributes.addFlashAttribute("successMessage", "Đơn hàng đã được xác nhận hoàn tất thành công!");
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        // Determine the redirect URL based on the current request URL
        String referer = request.getHeader("Referer");
        String redirectUrl = referer.contains("/details") ? "/admin/orders/details/" + orderCode : "/admin/orders?statusFilter=DELIVERING";

        return "redirect:" + redirectUrl;
    }
    @GetMapping("/admin/orders/deliver/{orderCode}")
    public String deliverOrder(HttpSession session, @PathVariable String orderCode, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/error";
        }

        Order order = orderService.getOrderByCode(orderCode);
        if (order != null) {
            order.setStatus("DELIVERING");
            orderService.saveOrder(order);
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        return "redirect:/admin/orders?statusFilter=PENDING";
    }
    @GetMapping("/admin/orders/cancel/{orderCode}")
    public String showAdminCancelOrderPage(HttpSession session, @PathVariable String orderCode, Model model) {
        if (!isAdmin(session)) {
            model.addAttribute("error", "Bạn không có quyền truy cập trang này.");
            return "error";
        }

        Order order = orderService.getOrderByCode(orderCode);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            model.addAttribute("error", "Chỉ có thể hủy các đơn hàng đang chờ xử lý.");
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", order);
        return "admin_cancel_order";
    }

    @PostMapping("/admin/orders/cancel/{orderCode}")
    public String cancelOrderByAdmin(
            HttpSession session,
            @PathVariable String orderCode,
            @RequestParam String adminReason,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {
        // Kiểm tra quyền admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện thao tác này.");
            return "redirect:/login";
        }

        // Kiểm tra lý do hủy
        if (adminReason == null || adminReason.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng cung cấp lý do hủy đơn.");
            return "redirect:/admin/orders/details/" + orderCode;
        }

        // Lấy đơn hàng
        Order order = orderService.getOrderByCode(orderCode);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/admin/orders/details/" + orderCode;
        }

        // Lấy thông tin người dùng liên quan đến đơn hàng
        User user = order.getUser();
        if (user != null) {
            // Trừ điểm của người dùng nếu đơn hàng đã được xác nhận và có điểm thưởng
            if ("DELIVERED".equalsIgnoreCase(order.getStatus())) {
                int earnedPoints = (int) (order.getTotalAmount() / 10000);
                user.setPoints(user.getPoints() - earnedPoints); // Trừ điểm
                UserService.saveUser(user); // Lưu lại thông tin người dùng
                System.out.println("Trừ " + earnedPoints + " điểm cho người dùng: " + user.getUsername());
            }
        }

        // Cập nhật trạng thái và lý do hủy
        order.setStatus("CANCELED");
        order.setCancelReason(adminReason);
        order.setCanceledBy("ADMIN");
        orderService.updateOrder(order);

        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được hủy thành công!");

        // Xác định URL chuyển hướng dựa trên referer
        String referer = request.getHeader("Referer");
        String redirectUrl = referer.contains("/details") ? "/admin/orders/details/" + orderCode : "/admin/orders";

        return "redirect:" + redirectUrl;
    }

    @GetMapping("/admin/customers")
    public String customersPage(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/error";
        }

        // Lấy danh sách người dùng từ UserService
        List<User> users = UserService.getAllUsers();

        // Cập nhật trạng thái online và tính tổng chi tiêu cho từng User
     // Cập nhật thời gian tạo tài khoản chỉ khi có giá trị
        for (User user : users) {
            // Tính tổng chi tiêu
            double totalSpending = orderService.getTotalSpending(user.getCustomerId());
            user.setTotalSpending(totalSpending);

            // Định dạng tổng chi tiêu với dấu chấm ngăn cách hàng nghìn
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedTotalSpending = formatter.format(totalSpending);
            user.setFormattedTotalSpending(formattedTotalSpending);

            // Kiểm tra nếu createdAt không null
            if (user.getCreatedAt() != null) {
                // Định dạng thời gian tạo tài khoản
                DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedCreatedAt = user.getCreatedAt().format(formatterDate);
                user.setFormattedCreatedAt(formattedCreatedAt);
            } else {
                // Nếu createdAt là null, có thể gán giá trị mặc định hoặc thông báo nào đó
                user.setFormattedCreatedAt("N/A"); // Hoặc có thể để trống ""
            }

            // Cập nhật trạng thái online
            user.setOnlineStatus(checkOnlineStatus(user));
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
            return "error";
        }

        model.addAttribute("users", users);
        return "admin/customers";
    }
    @GetMapping("/admin/customers/logout/{customerId}")
    public String forceLogout(@PathVariable Long customerId, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/error";
        }

        // Kiểm tra nếu người dùng tồn tại
        User user = UserService.findById(customerId);
        if (user == null) {
            return "redirect:/admin/customers?error=UserNotFound";
        }

        // Buộc đăng xuất người dùng
        sessionManager.logoutUser(user.getUsername());

        // Trả về danh sách khách hàng
        return "redirect:/admin/customers?success=UserLoggedOut";
    }

    private boolean checkOnlineStatus(User user) {
        // Logic kiểm tra trạng thái online
        // Ví dụ: kiểm tra session hiện tại hoặc dữ liệu WebSocket
        return sessionManager.isUserOnline(user.getUsername());
    }
    @GetMapping("/admin/customers/details/{secureToken}")
    public String viewCustomerDetails(@PathVariable String secureToken, Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/error";
        }

        // Tìm người dùng bằng secureToken
        User user = UserService.findBySecureToken(secureToken);
        if (user == null) {
            model.addAttribute("errorMessage", "Không tìm thấy thông tin người dùng.");
            return "admin/customer";
        }

        // Tính tổng chi tiêu của người dùng
        double totalSpending = orderService.getTotalSpending(user.getCustomerId());

        // Lấy danh sách đơn hàng của người dùng
        List<Order> orders = orderService.getOrdersByCustomerId(user.getCustomerId());

        // Định dạng tổng chi tiêu
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedTotalSpending = formatter.format(totalSpending);

        // Định dạng tiền trong danh sách đơn hàng
        DecimalFormat decimalFormat = new DecimalFormat("#,###.##"); // Sử dụng định dạng với dấu chấm ngăn cách hàng nghìn
        for (Order order : orders) {
            if (order.getTotalPrice() != null) {
                order.setFormattedTotalPrice(decimalFormat.format(order.getTotalPrice()));
            }
            if (order.getTotalAmount() != null) {
                order.setFormattedTotalAmount(decimalFormat.format(order.getTotalAmount()));
            }
        }
        
        List<Map<String, Object>> top5MenuItems = orderService.getTop5MenuItemsByUserId(user.getCustomerId());
        model.addAttribute("top5MenuItems", top5MenuItems);

        List<Map<String, Object>> pointsHistory = orderService.getPointsHistoryByUserId(user.getCustomerId());
        model.addAttribute("pointsHistory", pointsHistory);


        // Thêm dữ liệu vào model
        model.addAttribute("user", user);
        model.addAttribute("totalSpending", formattedTotalSpending);
        model.addAttribute("orders", orders);

        return "admin/customer-details";
    }





    @PostMapping("/admin/customers/add")
    public String addCustomer(@ModelAttribute User newUser, RedirectAttributes redirectAttributes, HttpSession session) {
    	   if (!isAdmin(session)) {
               return "redirect:/error";
           }
        boolean isAdded = UserService.addNewUser(newUser);
        if (isAdded) {
            redirectAttributes.addFlashAttribute("successMessage", "Khách hàng mới đã được thêm thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm khách hàng thất bại! Username hoặc email đã tồn tại.");
        }
        return "redirect:/admin/customers";
    }


    @PostMapping("/admin/customers/edit/{username}")
    public String editCustomerByUsername(@PathVariable String username,
                                         @ModelAttribute User updatedUser,
                                         @RequestParam String isAccountLocked,
                                         RedirectAttributes redirectAttributes) {
        // Chuyển giá trị khóa tài khoản thành boolean
        updatedUser.setAccountLocked(Boolean.parseBoolean(isAccountLocked));
        
        updatedUser.setForceRelogin(true);

        boolean isUpdated = UserService.updateUserByUsername(username, updatedUser);
        if (isUpdated) {
            redirectAttributes.addFlashAttribute("successMessage", "Thông tin khách hàng đã được cập nhật!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng với username: " + username);
        }
        return "redirect:/admin/customers";
    }


    @GetMapping("/admin/customers/delete/{username}")
    public String deleteCustomerByUsername(@PathVariable String username, RedirectAttributes redirectAttributes, HttpSession session, Model model) {
    	  if (!isAdmin(session)) {
              redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
              return "redirect:/error";
          }
    	  Long userId = (Long) session.getAttribute("userId");
          User user = UserService.getUserById(userId);

          if (user.isForceRelogin()) {
              model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
    
              return "error";
          }

        String deletedCustomerName = UserService.deleteUserByUsername(username);
        if (deletedCustomerName != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Khách hàng '" + deletedCustomerName + "' đã được xóa!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy khách hàng với username: " + username);
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/admin/promotions")
    public String showPromotions(Model model,  RedirectAttributes redirectAttributes , HttpSession session) {
    	  if (!isAdmin(session)) {
              redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
              return "redirect:/error";
          }
    	  Long userId = (Long) session.getAttribute("userId");
          User user = UserService.getUserById(userId);

          if (user.isForceRelogin()) {
              model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
    
              return "error";
          }

        model.addAttribute("promotion", new Promotion()); // Đảm bảo Promotion đã được thêm
        model.addAttribute("promotions", promotionService.getAllPromotions());
        return "admin/promotions";
    }


    @PostMapping("/admin/promotions/save")
    public String savePromotion(@ModelAttribute("promotion") Promotion promotion,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ! Vui lòng kiểm tra lại các trường nhập.");
            return "redirect:/admin/promotions";
        }

        promotionService.savePromotion(promotion);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo bài viết mới thành công!");
        return "redirect:/admin/promotions";
    }


    @GetMapping("/admin/promotions/{id}")
    @ResponseBody
    public Promotion getPromotion(HttpSession session, RedirectAttributes redirectAttributes, @PathVariable("id") Integer id) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
            return null; // Hoặc xử lý trả về một thông báo lỗi hợp lệ nếu không đủ quyền
        }
        return promotionService.getPromotionById(id);
    }

    @PostMapping("/admin/promotions/edit/{id}")
    public String editPromotion(
            @PathVariable Integer id,
            @ModelAttribute Promotion promotion,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Đảm bảo privacy có giá trị hợp lệ
            if (promotion.getPrivacy() == null || promotion.getPrivacy().isEmpty()) {
                promotion.setPrivacy("PUBLIC"); // Gán giá trị mặc định nếu bị null
            }

            // Cập nhật khuyến mãi
            promotionService.updatePromotion(id, promotion);
            redirectAttributes.addFlashAttribute("successMessage", "Chỉnh sửa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        return "redirect:/admin/promotions";
    }


    @GetMapping("/admin/promotions/delete/{id}")
    public String deletePromotion(HttpSession session, RedirectAttributes redirectAttributes, @PathVariable("id") Integer id, Model model) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
            return "redirect:/error";
        }
        Long userId = (Long) session.getAttribute("userId");
        User user = UserService.getUserById(userId);

        if (user.isForceRelogin()) {
            model.addAttribute("error", "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.");
  
            return "error";
        }

        promotionService.deletePromotionById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa bài viết " + id + " thành công!");
        return "redirect:/admin/promotions";
    }

   
}