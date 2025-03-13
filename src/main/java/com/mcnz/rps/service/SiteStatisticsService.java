package com.mcnz.rps.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class SiteStatisticsService {

    private final AtomicInteger totalVisits = new AtomicInteger(0);
    private final AtomicInteger onlineUsers = new AtomicInteger(0);
    private final AtomicInteger visitsLastMinute = new AtomicInteger(0);

    public void incrementTotalVisits() {
        totalVisits.incrementAndGet();
    }

    public void incrementVisitsLastMinute() {
        visitsLastMinute.incrementAndGet();
    }

    public int getTotalVisits() {
        return totalVisits.get();
    }

    public int getVisitsLastMinute() {
        return visitsLastMinute.get();
    }

    public void resetVisitsLastMinute() {
        visitsLastMinute.set(0);
    }

    public void userLoggedIn() {
        onlineUsers.incrementAndGet();
    }

    public void userLoggedOut() {
        onlineUsers.decrementAndGet();
    }

    public int getOnlineUsers() {
        return onlineUsers.get();
    }
}
