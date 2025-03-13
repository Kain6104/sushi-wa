package com.mcnz.rps.service;

import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.UserVoucherUsage;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.repository.MenuItemsRepository;
import com.mcnz.rps.repository.OrderRepository;
import com.mcnz.rps.repository.UserVoucherUsageRepository;
import com.mcnz.rps.repository.VoucherRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserVoucherUsageRepository userVoucherUsageRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MenuItemsRepository menuItemsRepository;


    /**
     * Tìm kiếm voucher theo mã.
     * 
     * @param code Mã voucher.
     * @return Voucher nếu tồn tại, ngược lại trả về null.
     */
    public Voucher findVoucherByCode(String code) {
        Optional<Voucher> voucher = voucherRepository.findByCode(code);
        return voucher.orElse(null);
    }

    /**
     * Cập nhật số lần sử dụng của voucher sau khi sử dụng.
     *
     * @param voucher Voucher cần cập nhật.
     */
    public void updateVoucherUsage(Voucher voucher) {
        if (voucher.canUse()) {
            voucher.incrementUsage(); // Tăng số lần sử dụng của voucher
            voucherRepository.save(voucher); // Lưu lại voucher vào database
        }
    }
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

  
    public void saveVoucher(Voucher voucher) {
        if (voucher.isApplicableToAll()) {
            voucher.setApplicableItems(null); // Xóa danh sách nếu áp dụng cho tất cả món ăn
        }
        voucherRepository.save(voucher);
    }

    public void deleteVoucherByCode(String code) {
        voucherRepository.deleteById(code);
    }
    @Transactional
    public void updateVoucherUsage() {
        // Lấy danh sách mã voucher, số lần sử dụng, mã đơn hàng, và ID người dùng
        List<Object[]> usageData = orderRepository.findVoucherUsage();

        for (Object[] record : usageData) {
            String voucherCode = (String) record[0];
            Long usageCount = (Long) record[1];
            Long userId = (Long) record[2];
            Long orderId = (Long) record[3];

            // Tìm kiếm voucher bằng Optional
            Optional<Voucher> optionalVoucher = voucherRepository.findByCode(voucherCode);
            if (optionalVoucher.isPresent()) {
                Voucher voucher = optionalVoucher.get();
                // Cập nhật usageCount trong bảng Voucher
                voucher.setUsageCount(voucher.getUsageCount() + usageCount.intValue());
                voucherRepository.save(voucher);
            } else {
                System.out.println("Không tìm thấy voucher với mã: " + voucherCode);
            }

            // Log thông tin mã đơn hàng và người dùng
            System.out.println("Voucher Code: " + voucherCode +
                               ", Usage Count: " + usageCount +
                               ", User ID: " + userId +
                               ", Order ID: " + orderId);
        }
    }
    // Cập nhật voucher
    public void updateVoucher(String code, Voucher updatedVoucher) {
        Voucher existingVoucher = findVoucherByCode(code);
        if (existingVoucher != null) {
            existingVoucher.setDiscountAmount(updatedVoucher.getDiscountAmount());
            existingVoucher.setMaxUsage(updatedVoucher.getMaxUsage());
            existingVoucher.setMaxUsagePerUser(updatedVoucher.getMaxUsagePerUser());
            existingVoucher.setStartDate(updatedVoucher.getStartDate());
            existingVoucher.setEndDate(updatedVoucher.getEndDate());
            existingVoucher.setApplicableToAll(updatedVoucher.isApplicableToAll());
            existingVoucher.setApplicableItemId(updatedVoucher.isApplicableToAll() ? null : updatedVoucher.getApplicableItemId());
            existingVoucher.setUsageCondition(updatedVoucher.getUsageCondition());

            voucherRepository.save(existingVoucher);
        }
    }


    // Xóa voucher
    public void deleteVoucher(String code) {
        voucherRepository.deleteById(code);
    }
    public List<UserVoucherUsage> getVoucherUsageHistory(String voucherCode) {
        return userVoucherUsageRepository.findByVoucherCode(voucherCode);
    }
    public boolean existsByCode(String code) {
        return voucherRepository.existsByCode(code);
    }
    public boolean isVoucherApplicable(Voucher voucher, Long itemId) {
        if (voucher.isApplicableToAll()) {
            return true; // Áp dụng cho tất cả món ăn
        }
        return voucher.getApplicableItemId() != null && voucher.getApplicableItemId().equals(itemId);
    }

    public List<MenuItems> getAllFoodItems() {
        return menuItemsRepository.findAll();
    }
    public List<UserVoucherUsage> getUsedVouchers(LocalDateTime startDate, LocalDateTime endDate) {
        return userVoucherUsageRepository.findUsedVouchers(startDate, endDate);
    }

}
