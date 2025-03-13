package com.mcnz.rps.service;

import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionManager {
    private Map<String, HttpSession> activeSessions = new HashMap<>();  // Lưu các session của người dùng
    private final Map<String, HttpSession> userSessions = new ConcurrentHashMap<>();  // Lưu session người dùng trong ConcurrentHashMap để đồng bộ hóa
    
    // Tập hợp để lưu trữ trạng thái người dùng đang online
    private final Set<String> onlineUsers = new HashSet<>();
    
    // Gọi khi người dùng đăng nhập
    public void loginUser(String username) {
        onlineUsers.add(username);
    }

    // Kiểm tra trạng thái online
    public boolean isUserOnline(String username) {
        return onlineUsers.contains(username);
    }

    // Hủy session theo username
    public void invalidateSessionsByUsername(String username) {
        HttpSession session = userSessions.get(username);
        if (session != null) {
            session.invalidate();
            userSessions.remove(username);
        }
    }

    // Đăng nhập người dùng và lưu vào activeSessions
    public void loginUser(String username, HttpSession session) {
        activeSessions.put(username, session);
    }

    // Đăng xuất người dùng
    public void logoutUser(String username) {
        HttpSession session = activeSessions.get(username);
        if (session != null) {
            session.invalidate();
            activeSessions.remove(username);
        }
    }

    // Đăng ký session của người dùng vào userSessions
    public void registerSession(String username, HttpSession session) {
        userSessions.put(username, session);
    }

    // Xóa session của người dùng
    public void logoutUser(String username, HttpSession session) {
        userSessions.remove(username);
        if (session != null) {
            session.invalidate();
        }
    }
}
