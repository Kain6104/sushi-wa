package com.mcnz.rps.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcnz.rps.model.User;
import com.mcnz.rps.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    Optional<UserSession> findBySessionId(String sessionId);
    void deleteByUser(User user);
}
