package com.mcnz.rps.config;

import com.mcnz.rps.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyResetScheduler {

    @Autowired
    private UserService userService;

    // Chạy lúc 00:00 mỗi ngày để đặt lại số lần gửi email
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetEmailCount() {
        userService.resetEmailCount();
        System.out.println("Đã đặt lại số lần gửi email trong ngày.");
    }
}
