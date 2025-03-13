package com.mcnz.rps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Tìm các thông báo của người dùng
    List<Notification> findByUsername(String username);

}
