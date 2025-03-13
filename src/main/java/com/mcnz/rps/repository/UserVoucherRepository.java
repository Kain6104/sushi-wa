package com.mcnz.rps.repository;

import com.mcnz.rps.model.UserVoucher;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUser(User user);
    boolean existsByUserAndVoucher(User user, Voucher voucher);
    Optional<UserVoucher> findByUserAndVoucher(User user, Voucher voucher);

}
