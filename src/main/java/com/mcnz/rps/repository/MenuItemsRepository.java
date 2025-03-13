package com.mcnz.rps.repository;

import com.mcnz.rps.model.MenuItems;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemsRepository extends JpaRepository<MenuItems, Integer> {
	  MenuItems findByItemCode(String itemCode);
	  void deleteByItemCode(String itemCode);
	   // Tìm kiếm món ăn theo tên
	    List<MenuItems> findByNameContainingIgnoreCase(String query);
	    MenuItems findByToken(String token);
	    List<MenuItems> findByCategory(String category);
}
