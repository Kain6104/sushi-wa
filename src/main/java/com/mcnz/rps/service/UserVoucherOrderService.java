package com.mcnz.rps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcnz.rps.model.UserVoucherOrder;
import com.mcnz.rps.repository.UserVoucherOrderRepository;

@Service
public class UserVoucherOrderService {

    @Autowired
    private UserVoucherOrderRepository userVoucherOrderRepository;

    public void save(UserVoucherOrder userVoucherOrder) {
        userVoucherOrderRepository.save(userVoucherOrder);
    }

    // Bạn có thể thêm các phương thức khác nếu cần, ví dụ: find, delete, update...
}
