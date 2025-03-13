package com.mcnz.rps.repository;

import com.mcnz.rps.model.Promotion;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
	  List<Promotion> findByPrivacy(String privacy);
	    List<Promotion> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

	    List<Promotion> findByPrivacyIn(List<String> privacies);
	    Optional<Promotion> findByToken(String token);

}
