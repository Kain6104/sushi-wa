package com.mcnz.rps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcnz.rps.model.BlockedIP;
import com.mcnz.rps.repository.BlockedIPRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BlockedIPService {

    @Autowired
    private BlockedIPRepository blockedIPRepository;

    /**
     * Kiểm tra xem IP có bị chặn hay không.
     *
     * @param ipAddress Địa chỉ IP cần kiểm tra.
     * @return true nếu IP bị chặn, false nếu không.
     */
    public boolean isBlocked(String ipAddress) {
        return blockedIPRepository.existsByIpAddress(ipAddress);
    }

    /**
     * Chặn một địa chỉ IP.
     *
     * @param ipAddress Địa chỉ IP cần chặn.
     */
    public void blockIP(String ipAddress) {
        if (!isBlocked(ipAddress)) {
            BlockedIP blockedIP = new BlockedIP();
            blockedIP.setIpAddress(ipAddress);
            blockedIP.setBlockedAt(LocalDateTime.now()); // Gán thời gian chặn
            blockedIPRepository.save(blockedIP);
        }
    }


    /**
     * Gỡ chặn một địa chỉ IP.
     *
     * @param id ID của bản ghi IP trong cơ sở dữ liệu.
     */
    public void unblockIP(Long id) {
        blockedIPRepository.deleteById(id);
    }

    /**
     * Lấy danh sách toàn bộ các IP bị chặn.
     *
     * @return Danh sách các đối tượng BlockedIP.
     */
    public List<BlockedIP> getAllBlockedIPs() {
        return blockedIPRepository.findAll();
    }
  
}
