package com.mcnz.rps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Integer> {
}
