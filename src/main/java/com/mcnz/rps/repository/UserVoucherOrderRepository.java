package com.mcnz.rps.repository;

import com.mcnz.rps.model.UserVoucherOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVoucherOrderRepository extends JpaRepository<UserVoucherOrder, Long> {
    List<UserVoucherOrder> findByUserVoucherUsageId(Long userVoucherUsageId);
}
