package com.mcnz.rps.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserVoucherOrder;
import com.mcnz.rps.model.UserVoucherUsage;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.repository.UserRepository;
import com.mcnz.rps.repository.VoucherRepository;
import com.mcnz.rps.repository.OrderRepository;
import com.mcnz.rps.repository.UserVoucherUsageRepository;

@Service
public class UserVoucherUsageService {

    @Autowired
    private UserVoucherUsageRepository userVoucherUsageRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;
    // Inject VoucherService
    @Autowired
    private VoucherService voucherService;

    

    public List<UserVoucherUsage> findByVoucher(Voucher voucher) {
        return userVoucherUsageRepository.findByVoucher(voucher);
    }

    public void save(UserVoucherUsage usage) {
        userVoucherUsageRepository.save(usage);
    }


    public void updateVoucherUsage(String voucherCode, String userId, String orderId) {
        // Tìm voucher theo mã
        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));

        // Tìm user theo ID
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        // Tìm order nếu có orderId
        Order order = orderRepository.findById(Long.valueOf(orderId))
                .orElseThrow(() -> new IllegalArgumentException("Order không tồn tại"));

        // Tìm kiếm UserVoucherUsage nếu đã có cho user và voucher này
        Optional<UserVoucherUsage> usageOpt = findByUserAndVoucher(user, voucher);
        UserVoucherUsage usage;

        if (usageOpt.isPresent()) {
            usage = usageOpt.get();
            usage.incrementUsage(); // Tăng số lần sử dụng của voucher
        } else {
            usage = new UserVoucherUsage(user, voucher);
            usage.setUsageCount(1); // Đây là lần sử dụng đầu tiên
        }

        // Thêm mã đơn hàng vào UserVoucherUsage
        usage.addOrder(order); // Thêm đơn hàng vào danh sách

        // Lưu UserVoucherUsage với các đơn hàng liên kết
        saveUserVoucherUsage(usage); // Lưu UserVoucherUsage

        // Cập nhật số lượng voucher còn lại trong hệ thống
        voucherService.updateVoucherUsage(voucher);
    }


     
 // Tìm kiếm UserVoucherUsage theo User và Voucher
    public Optional<UserVoucherUsage> findByUserAndVoucher(User user, Voucher voucher) {
        return userVoucherUsageRepository.findByUserAndVoucher(user, voucher);
    }

    // Lưu UserVoucherUsage
    public void saveUserVoucherUsage(UserVoucherUsage userVoucherUsage) {
        userVoucherUsageRepository.save(userVoucherUsage);
    }
    public List<UserVoucherUsage> getVoucherUsageHistory(String voucherCode) {
        return userVoucherUsageRepository.findByVoucherCode(voucherCode);
    }


    public List<UserVoucherUsage> getUsedVouchers(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return userVoucherUsageRepository.findUsedVouchers(startDateTime, endDateTime);
    }
    public Map<String, Double> getTotalDiscountPerVoucher() {
        List<Order> deliveredOrders = orderRepository.findByStatus("DELIVERED"); // Chỉ lấy đơn hàng đã giao
        Map<String, Double> discountMap = new HashMap<>();

        for (Order order : deliveredOrders) { // Lặp qua đơn hàng đã giao
            String voucherCode = order.getVoucherCode();
            Double voucherDiscount = order.getVoucherDiscount(); // Lấy giá trị từ voucher_discount

            if (voucherCode != null && voucherDiscount != null) { // Đảm bảo không null
                discountMap.put(voucherCode, discountMap.getOrDefault(voucherCode, 0.0) + voucherDiscount);
            }
        }

        return discountMap;
    }

    // Thêm phương thức lấy toàn bộ lịch sử sử dụng voucher
    public List<UserVoucherUsage> getAllUsedVouchers() {
        return userVoucherUsageRepository.findAll();
    }
    public List<UserVoucherUsage> getUsedVouchersByCode(String voucherCode) {
        return userVoucherUsageRepository.findByVoucherCode(voucherCode);
    }


}

