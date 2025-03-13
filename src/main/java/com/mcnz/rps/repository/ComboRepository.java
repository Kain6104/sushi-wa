package com.mcnz.rps.repository;

import com.mcnz.rps.model.Combo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComboRepository extends JpaRepository<Combo, Long> {
    Combo findByToken(String token);
}
