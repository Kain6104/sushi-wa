package com.mcnz.rps.controller;

import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserVoucherUsage;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.model.VoucherEditLog;
import com.mcnz.rps.repository.MenuItemsRepository;
import com.mcnz.rps.repository.UserVoucherUsageRepository;
import com.mcnz.rps.repository.VoucherEditLogRepository;
import com.mcnz.rps.service.MenuItemsService;
import com.mcnz.rps.service.OrderService;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.service.UserVoucherUsageService;
import com.mcnz.rps.service.VoucherService;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {
	 @Autowired
	private UserVoucherUsageRepository userVoucherUsageRepository;
	 @Autowired
		private VoucherEditLogRepository voucherEditLogRepository;
	 @Autowired
	    private UserVoucherUsageService userVoucherUsageService;
	 @Autowired
    private VoucherService voucherService;
	 @Autowired
	    private OrderService orderService;
	 @Autowired
	    private UserService userService;
		
	 @Autowired
	 private MenuItemsService menuItemsService;

	 @Autowired
	 private MenuItemsRepository menuItemsRepository;
	 

    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "admin".equalsIgnoreCase(role);
    }
	    
    // Hiển thị danh sách voucher
    @GetMapping
    public String listVouchers(Model model) {
        List<Voucher> allVouchers = voucherService.getAllVouchers();

        List<Voucher> vouchers = voucherService.getAllVouchers();
     // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Voucher đang diễn ra
        List<Voucher> ongoingVouchers = allVouchers.stream()
                .filter(voucher -> !voucher.getStartDate().isAfter(now) && !voucher.getEndDate().isBefore(now))
                .toList();

        // Voucher đã hết hạn
        List<Voucher> expiredVouchers = allVouchers.stream()
                .filter(voucher -> voucher.getEndDate().isBefore(now))
                .toList();

        // Voucher sắp diễn ra
        List<Voucher> upcomingVouchers = allVouchers.stream()
                .filter(voucher -> voucher.getStartDate().isAfter(now))
                .toList();
        
        
        // Đưa danh sách voucher vào Model
        model.addAttribute("ongoingVouchers", ongoingVouchers);
        model.addAttribute("expiredVouchers", expiredVouchers);
        model.addAttribute("upcomingVouchers", upcomingVouchers);

        model.addAttribute("vouchers", vouchers);
        return "admin/vouchers/voucher-list";  // Trả về trang hiển thị danh sách voucher
    }

    // Hiển thị form thêm voucher
    @GetMapping("/create")
    public String createVoucherForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("foodItems", voucherService.getAllFoodItems()); // ✅ Load danh sách món ăn
        return "admin/vouchers/voucher-create";
    }

    @PostMapping("/create")
    public String createVoucher(@ModelAttribute Voucher voucher,
                                @RequestParam(value = "menuItemIds", required = false) List<Long> menuItemIds,
                                HttpSession session, // ✅ Lấy session
                                Model model, RedirectAttributes redirectAttributes) {

        // ✅ Kiểm tra quyền admin
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quyền của bạn không được phép truy cập trang web này");
            return "redirect:/error";
        }

        if (voucherService.existsByCode(voucher.getCode())) {
            model.addAttribute("errorMessage", "Mã Voucher đã tồn tại. Vui lòng nhập mã khác.");
            model.addAttribute("voucher", voucher);
            model.addAttribute("foodItems", voucherService.getAllFoodItems());
            return "admin/vouchers/voucher-create";
        }

        // ✅ Lấy userId từ session
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin người dùng.");
            return "redirect:/admin/vouchers";
        }

        // ✅ Lấy user từ database
        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng với ID: " + userId);
            return "redirect:/admin/vouchers";
        }

        if (!voucher.isApplicableToAll() && menuItemIds != null) {
            List<Integer> menuItemIdsInt = menuItemIds.stream().map(Long::intValue).toList();
            List<MenuItems> applicableItems = menuItemsRepository.findAllById(menuItemIdsInt);
            voucher.setApplicableItems(applicableItems);
        } else {
            voucher.setApplicableItems(null);
        }

        // ✅ Gán người tạo & thời gian tạo
        voucher.setCreatedBy(user);
        voucher.setCreatedAt(LocalDateTime.now());

        voucherService.saveVoucher(voucher);
        redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được thêm thành công.");
        return "redirect:/admin/vouchers";
    }

    // Hiển thị form chỉnh sửa voucher
    @GetMapping("/edit/{code}")
    public String editVoucherForm(@PathVariable("code") String code, Model model) {
        Voucher voucher = voucherService.findVoucherByCode(code);
        if (voucher == null) {
            model.addAttribute("errorMessage", "Voucher không tồn tại.");
            return "redirect:/admin/vouchers";
        }

        List<MenuItems> foodItems = voucherService.getAllFoodItems();

        // Đếm số lượng món ăn trong từng danh mục
        Map<String, Long> categoryCounts = foodItems.stream()
            .collect(Collectors.groupingBy(MenuItems::getCategory, Collectors.counting()));

        // Truy vấn lịch sử chỉnh sửa
        List<VoucherEditLog> editLogs = voucherEditLogRepository.findByVoucherOrderByEditTimeDesc(voucher);

        model.addAttribute("voucher", voucher);
        model.addAttribute("foodItems", foodItems);
        model.addAttribute("categoryCounts", categoryCounts);
        model.addAttribute("editLogs", editLogs); // Thêm lịch sử chỉnh sửa vào model

        return "admin/vouchers/edit-voucher";
    }
 // Hiển thị lịch sử chỉnh sửa và sử dụng voucher
    @GetMapping("/history/{code}")
    public String showHistoryVoucher(@PathVariable("code") String code, Model model ) {
        // Tìm voucher theo mã
        Voucher voucher = voucherService.findVoucherByCode(code);
        if (voucher == null) {
            model.addAttribute("errorMessage", "Voucher không tồn tại.");
            return "redirect:/admin/vouchers";
        }
        

        // Lấy danh sách tất cả món ăn
        List<MenuItems> foodItems = voucherService.getAllFoodItems();

        // Đếm số lượng món ăn theo từng danh mục
        Map<String, Long> categoryCounts = foodItems.stream()
            .collect(Collectors.groupingBy(MenuItems::getCategory, Collectors.counting()));

        // Lấy lịch sử chỉnh sửa voucher
        List<VoucherEditLog> editLogs = voucherEditLogRepository.findByVoucherOrderByEditTimeDesc(voucher);

        // Lấy lịch sử sử dụng voucher theo mã
        List<UserVoucherUsage> usageHistory = userVoucherUsageRepository.findByVoucherCode(code);

        // Lấy danh sách tất cả voucher
        List<Voucher> vouchers = voucherService.getAllVouchers();
        
        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();

        // Phân loại voucher
        List<Voucher> ongoingVouchers = vouchers.stream()
                .filter(v -> !v.getStartDate().isAfter(now) && !v.getEndDate().isBefore(now))
                .toList();

        List<Voucher> expiredVouchers = vouchers.stream()
                .filter(v -> v.getEndDate().isBefore(now))
                .toList();

        List<Voucher> upcomingVouchers = vouchers.stream()
                .filter(v -> v.getStartDate().isAfter(now))
                .toList();

     
     // Lấy lịch sử sử dụng voucher theo mã cụ thể
        List<UserVoucherUsage> usedVouchers = userVoucherUsageService.getUsedVouchersByCode(code);
        model.addAttribute("usedVouchers", usedVouchers);

        // Lấy danh sách tất cả đơn hàng có sử dụng voucher này
        List<Order> ordersWithVoucher = orderService.getOrdersByVoucherCode(code);
        model.addAttribute("ordersWithVoucher", ordersWithVoucher);

        // Sắp xếp danh sách theo ngày giảm dần (mới nhất trước)
        ordersWithVoucher.sort(Comparator.comparing(Order::getOrderDate).reversed());
        
        // Tính tổng giảm giá
        double totalVoucherDiscount = ordersWithVoucher.stream()
                .mapToDouble(Order::getVoucherDiscount) // Giữ nguyên kiểu Double
                .sum();


        model.addAttribute("ordersWithVoucher", ordersWithVoucher);
        model.addAttribute("totalVoucherDiscount", totalVoucherDiscount);
        
        // Đưa dữ liệu vào Model
        model.addAttribute("voucher", voucher);
        model.addAttribute("foodItems", foodItems);
        model.addAttribute("categoryCounts", categoryCounts);
        model.addAttribute("editLogs", editLogs);
        model.addAttribute("usageHistory", usageHistory);
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("ongoingVouchers", ongoingVouchers);
        model.addAttribute("expiredVouchers", expiredVouchers);
        model.addAttribute("upcomingVouchers", upcomingVouchers);
        model.addAttribute("now", LocalDateTime.now()); // Thêm biến now vào Thymeleaf

        return "admin/vouchers/voucher-history";
    }

    // Cập nhật voucher
    @PostMapping("/edit/{code}")
    public String updateVoucher(@PathVariable("code") String code, 
                                @ModelAttribute Voucher updatedVoucher, 
                                @RequestParam(value = "menuItemIds", required = false) List<Long> menuItemIds,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        boolean hasChanges = false;

        Voucher existingVoucher = voucherService.findVoucherByCode(code);
        if (existingVoucher == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy voucher.");
            return "redirect:/admin/vouchers";
        }

        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getUserById(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng.");
            return "redirect:/admin/vouchers/edit/" + code;
        }

        // Tạo log thay đổi
        StringBuilder changeLog = new StringBuilder();

        if (existingVoucher.getDiscountAmount() != updatedVoucher.getDiscountAmount()) {
            changeLog.append("Số tiền giảm giá: ").append(existingVoucher.getDiscountAmount())
                     .append(" ➝ ").append(updatedVoucher.getDiscountAmount()).append("\n");
            existingVoucher.setDiscountAmount(updatedVoucher.getDiscountAmount());
            hasChanges = true;
        }

        if (!existingVoucher.getDiscountType().equals(updatedVoucher.getDiscountType())) {
            changeLog.append("Loại giảm giá: ").append(existingVoucher.getDiscountType())
                     .append(" ➝ ").append(updatedVoucher.getDiscountType()).append("\n");
            existingVoucher.setDiscountType(updatedVoucher.getDiscountType());
            hasChanges = true;
        }

        if (!existingVoucher.getStartDate().equals(updatedVoucher.getStartDate())) {
            changeLog.append("Ngày bắt đầu: ").append(existingVoucher.getStartDate())
                     .append(" ➝ ").append(updatedVoucher.getStartDate()).append("\n");
            existingVoucher.setStartDate(updatedVoucher.getStartDate());
            hasChanges = true;
        }

        if (!existingVoucher.getEndDate().equals(updatedVoucher.getEndDate())) {
            changeLog.append("Ngày kết thúc: ").append(existingVoucher.getEndDate())
                     .append(" ➝ ").append(updatedVoucher.getEndDate()).append("\n");
            existingVoucher.setEndDate(updatedVoucher.getEndDate());
            hasChanges = true;
        }

        // Chỉ lưu lại phần bị thay đổi của điều kiện sử dụng
        String oldCondition = existingVoucher.getUsageCondition();
        String newCondition = updatedVoucher.getUsageCondition();

        if (!Objects.equals(oldCondition, newCondition)) {
            // Tách các dòng điều kiện thành danh sách
            List<String> oldLines = Arrays.asList(oldCondition.split("\n"));
            List<String> newLines = Arrays.asList(newCondition.split("\n"));

            StringBuilder conditionChanges = new StringBuilder("Điều kiện sử dụng thay đổi:\n");

            // So sánh từng dòng
            int minSize = Math.min(oldLines.size(), newLines.size());
            for (int i = 0; i < minSize; i++) {
                if (!oldLines.get(i).equals(newLines.get(i))) {
                    conditionChanges.append("- ").append(oldLines.get(i)).append(" ➝ ").append(newLines.get(i)).append("\n");
                }
            }

            // Nếu có dòng mới được thêm vào
            if (newLines.size() > oldLines.size()) {
                for (int i = minSize; i < newLines.size(); i++) {
                    conditionChanges.append("+ ").append(newLines.get(i)).append("\n");
                }
            }

            // Nếu có dòng bị xóa đi
            if (oldLines.size() > newLines.size()) {
                for (int i = minSize; i < oldLines.size(); i++) {
                    conditionChanges.append("").append(oldLines.get(i)).append("\n");
                }
            }

            // Chỉ lưu nếu có thay đổi
            if (conditionChanges.length() > 0) {
                changeLog.append(conditionChanges);
                existingVoucher.setUsageCondition(newCondition);
            }
            hasChanges = true;
        }


        if (existingVoucher.getMinOrderAmount() != updatedVoucher.getMinOrderAmount()) {
            changeLog.append("Số tiền đơn hàng tối thiểu: ").append(existingVoucher.getMinOrderAmount())
                     .append(" ➝ ").append(updatedVoucher.getMinOrderAmount()).append("\n");
            existingVoucher.setMinOrderAmount(updatedVoucher.getMinOrderAmount());
            hasChanges = true;
        }

        if (existingVoucher.getMaxDiscount() != updatedVoucher.getMaxDiscount()) {
            changeLog.append("Mức giảm tối đa: ").append(existingVoucher.getMaxDiscount())
                     .append(" ➝ ").append(updatedVoucher.getMaxDiscount()).append("\n");
            existingVoucher.setMaxDiscount(updatedVoucher.getMaxDiscount());
            hasChanges = true;
        }

        // Lưu lịch sử chỉnh sửa món ăn
        if (!updatedVoucher.isApplicableToAll() && menuItemIds != null) {
            List<MenuItems> oldApplicableItems = existingVoucher.getApplicableItems();

            // Ép kiểu menuItemIds từ List<Long> -> List<Integer> nếu cần
            List<Integer> menuItemIdsInt = menuItemIds.stream().map(Long::intValue).toList();

            // Lấy danh sách món ăn mới từ DB
            List<MenuItems> newApplicableItems = menuItemsRepository.findAllById(menuItemIdsInt);

            // Lấy danh sách ID món ăn cũ và mới
            Set<Integer> oldItemIds = oldApplicableItems.stream().map(MenuItems::getItemId).collect(Collectors.toSet());
            Set<Integer> newItemIds = newApplicableItems.stream().map(MenuItems::getItemId).collect(Collectors.toSet());

            // Xác định món ăn bị thêm vào
            Set<Integer> addedItems = new HashSet<>(newItemIds);
            addedItems.removeAll(oldItemIds);

            // Xác định món ăn bị xóa khỏi danh sách
            Set<Integer> removedItems = new HashSet<>(oldItemIds);
            removedItems.removeAll(newItemIds);

            // Ghi log các thay đổi về món ăn
            if (!addedItems.isEmpty()) {
                List<String> addedItemNames = newApplicableItems.stream()
                    .filter(item -> addedItems.contains(item.getItemId()))
                    .map(MenuItems::getName)
                    .toList();
                changeLog.append("Món ăn được thêm vào voucher: ").append(String.join(", ", addedItemNames)).append("\n");
            }

            if (!removedItems.isEmpty()) {
                List<String> removedItemNames = oldApplicableItems.stream()
                    .filter(item -> removedItems.contains(item.getItemId()))
                    .map(MenuItems::getName)
                    .toList();
                changeLog.append("Món ăn bị xóa khỏi voucher: ").append(String.join(", ", removedItemNames)).append("\n");
            }

            existingVoucher.setApplicableItems(newApplicableItems);
            hasChanges = true;
        } else {
            existingVoucher.setApplicableItems(null);
        }
        if (existingVoucher.getMaxUsage() != updatedVoucher.getMaxUsage()) {
            changeLog.append("Số lần sử dụng tối đa: ").append(existingVoucher.getMaxUsage())
                     .append(" ➝ ").append(updatedVoucher.getMaxUsage()).append("\n");
            existingVoucher.setMaxUsage(updatedVoucher.getMaxUsage());
            hasChanges = true;
        }

        if (existingVoucher.getMaxUsagePerUser() != updatedVoucher.getMaxUsagePerUser()) {
            changeLog.append("Số lần sử dụng tối đa mỗi người dùng: ").append(existingVoucher.getMaxUsagePerUser())
                     .append(" ➝ ").append(updatedVoucher.getMaxUsagePerUser()).append("\n");
            existingVoucher.setMaxUsagePerUser(updatedVoucher.getMaxUsagePerUser());
            hasChanges = true;
        }

        existingVoucher.setApplicableToAll(updatedVoucher.isApplicableToAll());


        // Nếu có thay đổi, lưu vào DB
        if (!changeLog.toString().isEmpty()) {
            voucherService.saveVoucher(existingVoucher);

            // Lưu log chỉnh sửa
            VoucherEditLog editLog = new VoucherEditLog();
            editLog.setVoucher(existingVoucher);
            editLog.setEditedBy(user);
            editLog.setEditTime(LocalDateTime.now());
            editLog.setChanges(changeLog.toString());
            voucherEditLogRepository.save(editLog);
        }
        if (hasChanges) {
            voucherService.saveVoucher(existingVoucher);

            // Cập nhật lại voucher trong session nếu voucher bị thay đổi
            session.removeAttribute("voucher");
            session.removeAttribute("voucherDiscount");

            redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được cập nhật thành công và sẽ được kiểm tra lại trong giỏ hàng.");
        }
        redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được cập nhật thành công.");
        return "redirect:/admin/vouchers";
    }


    // Xóa voucher
    @GetMapping("/delete/{code}")
    public String deleteVoucher(@PathVariable("code") String code, RedirectAttributes redirectAttributes) {
        voucherService.deleteVoucher(code);
        redirectAttributes.addFlashAttribute("successMessage", "Voucher đã được xóa thành công.");
        return "redirect:/admin/vouchers";
    }
    @GetMapping("/voucherUsageHistory/{voucherCode}")
    public String getVoucherUsageHistory(@PathVariable String voucherCode, Model model) {
        // Tìm tất cả UserVoucherUsage theo voucher code
        List<UserVoucherUsage> usageHistory = userVoucherUsageRepository.findByVoucherCode(voucherCode);

        // Trả về dữ liệu cho view
        model.addAttribute("usageHistory", usageHistory);
        return "admin/vouchers/voucher-usage-history";
    }
}
