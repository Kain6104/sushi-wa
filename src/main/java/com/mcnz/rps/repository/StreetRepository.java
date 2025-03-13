package com.mcnz.rps.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mcnz.rps.model.Street;

@Repository
public interface StreetRepository extends JpaRepository<Street, Integer> {
    List<Street> findByDistrictId(Integer districtId);
    List<Street> findByWardId(Integer wardId);

}
