package com.mcnz.rps.service;

import com.mcnz.rps.model.UserVoucher;
import com.mcnz.rps.model.User;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.repository.UserVoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserVoucherService {

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    public List<UserVoucher> getUserVouchers(User user) {
        return userVoucherRepository.findByUser(user);
    }

    public boolean saveVoucherForUser(User user, Voucher voucher) {
        if (userVoucherRepository.existsByUserAndVoucher(user, voucher)) {
            return false; // Người dùng đã lưu voucher này rồi
        }
        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucherRepository.save(userVoucher);
        return true;
    }
    public void markVoucherAsUsed(User user, Voucher voucher) {
        Optional<UserVoucher> userVoucherOpt = userVoucherRepository.findByUserAndVoucher(user, voucher);
        if (userVoucherOpt.isPresent()) {
            UserVoucher userVoucher = userVoucherOpt.get();
            userVoucher.setIsUsed(true);
            userVoucherRepository.save(userVoucher);
        }
    }
    
}
