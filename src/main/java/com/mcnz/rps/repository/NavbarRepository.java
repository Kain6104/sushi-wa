package com.mcnz.rps.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.NavbarLink;

import java.util.List;

@Repository
public interface NavbarRepository extends JpaRepository<NavbarLink, Long> {
    List<NavbarLink> findAllByOrderByPosition();
}
