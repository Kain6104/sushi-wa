package com.mcnz.rps.repository;

import com.mcnz.rps.model.ChatHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findTop10ByOrderByTimestampDesc(); // Lấy 10 tin nhắn gần nhất
}
