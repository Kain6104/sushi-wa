package com.mcnz.rps.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.model.UserVoucherUsage;
@Repository
public interface UserVoucherUsageRepository extends JpaRepository<UserVoucherUsage, Long> {
    
    // Tìm kiếm UserVoucherUsage dựa trên User và Voucher
    Optional<UserVoucherUsage> findByUserAndVoucher(User user, Voucher voucher);

    // Lấy danh sách tất cả các UserVoucherUsage dựa trên Voucher
    List<UserVoucherUsage> findByVoucher(Voucher voucher);

    // Tìm kiếm UserVoucherUsage dựa trên mã voucher (voucher.code)
    @Query("SELECT u FROM UserVoucherUsage u WHERE u.voucher.code = :voucherCode")
    List<UserVoucherUsage> findByVoucherCode(@Param("voucherCode") String voucherCode);

    @Query("SELECT u FROM UserVoucherUsage u WHERE u.order = :order")
    Optional<UserVoucherUsage> findByOrder(@Param("order") Order order);
    List<UserVoucherUsage> findByUser(User user); // Tìm tất cả user voucher usage của user
    @Query("SELECT u FROM UserVoucherUsage u " +
    	       "WHERE u.order.orderDate BETWEEN :startDateTime AND :endDateTime")
    	List<UserVoucherUsage> findUsedVouchers(@Param("startDateTime") LocalDateTime startDate, 
    	                                        @Param("endDateTime") LocalDateTime endDate);

}
