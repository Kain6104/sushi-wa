package com.mcnz.rps.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.mcnz.rps.model.BlockedIP;
import com.mcnz.rps.service.BlockedIPService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class BlockIPController {

    @Autowired
    private BlockedIPService blockedIPService;

    @GetMapping("/manage-blocked-ips")
    public String manageBlockedIPs(Model model) {
        // Định dạng thời gian trước khi gửi vào model
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<BlockedIP> blockedIPs = blockedIPService.getAllBlockedIPs();
        
        // Định dạng thời gian của từng đối tượng BlockedIP
        blockedIPs.forEach(ip -> ip.setFormattedBlockedAt(ip.getBlockedAt().format(formatter)));
        
        model.addAttribute("blockedIPs", blockedIPs);
        return "admin/manage-blocked-ips"; // Giao diện quản lý
    }

    @PostMapping("/block-ip")
    public String blockIP(@RequestParam("ipAddress") String ipAddress, HttpServletRequest request, Model model) {
        String clientIP = request.getRemoteAddr(); // Lấy IP của client từ request

        if (blockedIPService.isBlocked(clientIP)) {
            model.addAttribute("errorMessage", "IP của bạn đã bị chặn.");
            return "admin/manage-blocked-ips";  // Trả về view với thông báo lỗi
        }

        blockedIPService.blockIP(ipAddress);
        return "redirect:/admin/manage-blocked-ips"; // Redirect sau khi chặn IP
    }


    @PostMapping("/unblock-ip/{id}")
    public String unblockIP(@PathVariable Long id) {
        blockedIPService.unblockIP(id);
        return "redirect:/admin/manage-blocked-ips"; // Redirect sau khi bỏ chặn
    }
}
