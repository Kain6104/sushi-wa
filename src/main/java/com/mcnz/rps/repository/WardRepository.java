package com.mcnz.rps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.Ward;

@Repository
public interface WardRepository extends JpaRepository<Ward, Integer> {
    List<Ward> findByDistrictId(Integer districtId);
}
