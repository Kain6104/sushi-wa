package com.mcnz.rps.controller;

import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserVoucher;
import com.mcnz.rps.model.Voucher;
import com.mcnz.rps.service.UserService;
import com.mcnz.rps.service.UserVoucherService;
import com.mcnz.rps.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user/vouchers")
public class UserVoucherController {

    @Autowired
    private UserVoucherService userVoucherService;

    @Autowired
    private UserService userService;

    @Autowired
    private VoucherService voucherService;

    // Hiển thị danh sách voucher của người dùng
    @GetMapping
    public String userVouchers(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        User user = userService.getUserById(userId);
        List<UserVoucher> userVouchers = userVoucherService.getUserVouchers(user);
        model.addAttribute("userVouchers", userVouchers);
        return "voucher-wallet"; // Hiển thị trang "Ví Voucher"
    }

    // Lưu voucher vào ví
    @PostMapping("/save/{voucherCode}")
    public String saveVoucher(@PathVariable String voucherCode, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }

        User user = userService.getUserById(userId);
        Voucher voucher = voucherService.findVoucherByCode(voucherCode);

        if (voucher == null) {
            model.addAttribute("errorMessage", "Voucher không tồn tại!");
            return "redirect:/vouchers";
        }

        boolean saved = userVoucherService.saveVoucherForUser(user, voucher);
        if (!saved) {
            model.addAttribute("errorMessage", "Bạn đã lưu voucher này rồi.");
        } else {
            model.addAttribute("successMessage", "Lưu voucher thành công!");
        }

        return "redirect:/user/vouchers";
    }
}
