package com.mcnz.rps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mcnz.rps.model.BlockedIP;

public interface BlockedIPRepository extends JpaRepository<BlockedIP, Long> {
    boolean existsByIpAddress(String ipAddress);
}

