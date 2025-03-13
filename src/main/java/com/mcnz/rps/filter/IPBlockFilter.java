package com.mcnz.rps.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mcnz.rps.service.BlockedIPService;

import java.io.IOException;

@Component
public class IPBlockFilter extends OncePerRequestFilter {

    @Autowired
    private BlockedIPService blockedIPService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIP = request.getRemoteAddr();

        if (blockedIPService.isBlocked(clientIP)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "IP của bạn đã bị chặn.");
            return;
        }

        filterChain.doFilter(request, response);
    }
    
}
