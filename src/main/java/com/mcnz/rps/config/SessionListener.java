package com.mcnz.rps.config;


import com.mcnz.rps.service.SiteStatisticsService;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@WebListener
public class SessionListener implements HttpSessionListener {

    @Autowired
    private SiteStatisticsService siteStatisticsService;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        siteStatisticsService.incrementTotalVisits();
        siteStatisticsService.userLoggedIn();
        System.out.println("Session created: Total visits = " + siteStatisticsService.getTotalVisits()
                + ", Online users = " + siteStatisticsService.getOnlineUsers());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        siteStatisticsService.userLoggedOut();
        System.out.println("Session destroyed: Online users = " + siteStatisticsService.getOnlineUsers());
    }
}