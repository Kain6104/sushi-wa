package com.mcnz.rps.repository;

import com.mcnz.rps.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, String> {
    Optional<Voucher> findByCode(String code);
    boolean existsByCode(String code);

}
