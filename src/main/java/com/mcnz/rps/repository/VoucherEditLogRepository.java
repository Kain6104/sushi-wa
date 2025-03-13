package com.mcnz.rps.repository;

import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.model.VoucherEditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherEditLogRepository extends JpaRepository<VoucherEditLog, Long> {
    List<VoucherEditLog> findByVoucherOrderByEditTimeDesc(Voucher voucher);
}
