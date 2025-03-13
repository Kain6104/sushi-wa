package com.mcnz.rps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.District;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findByProvinceId(Integer provinceId);
}
