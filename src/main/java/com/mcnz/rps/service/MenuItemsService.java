package com.mcnz.rps.service;

import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.repository.MenuItemsRepository;
import com.mcnz.rps.repository.OrderDetailRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MenuItemsService {

    @Autowired
    private MenuItemsRepository menuItemsRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    // Phương thức để lấy tất cả các món ăn
    public List<MenuItems> getAllItems() {
        return menuItemsRepository.findAll();
    }

    // Phương thức để lấy món ăn dựa trên ID
    public MenuItems getItemById(Integer id) { // Thay đổi kiểu dữ liệu từ Long thành Integer
        Optional<MenuItems> menuItem = menuItemsRepository.findById(id);
        return menuItem.orElse(null); // Trả về null nếu không tìm thấy món ăn
    }
    public List<MenuItems> getAllMenuItems() {
        return menuItemsRepository.findAll();  // Lấy tất cả các món ăn
    }

    public MenuItems getMenuItemByCode(String itemCode) {
        return menuItemsRepository.findByItemCode(itemCode);  // Tìm món ăn theo itemCode
    }

    public MenuItems saveMenuItem(MenuItems menuItems) {
        return menuItemsRepository.save(menuItems);  // Lưu hoặc cập nhật món ăn
    }

   
    // Phương thức chỉnh sửa món ăn
    public MenuItems updateMenuItem(MenuItems updatedMenuItem) {
        Optional<MenuItems> existingMenuItemOpt = menuItemsRepository.findById(updatedMenuItem.getItemId());
        if (existingMenuItemOpt.isPresent()) {
            MenuItems existingMenuItem = existingMenuItemOpt.get();
            // Cập nhật các thuộc tính
            existingMenuItem.setName(updatedMenuItem.getName());
            existingMenuItem.setDescription(updatedMenuItem.getDescription());
            existingMenuItem.setPrice(updatedMenuItem.getPrice());
            existingMenuItem.setCategory(updatedMenuItem.getCategory());
            existingMenuItem.setIngredients(updatedMenuItem.getIngredients());
            existingMenuItem.setImageUrl(updatedMenuItem.getImageUrl());
            existingMenuItem.setPromotion(updatedMenuItem.getPromotion());
            existingMenuItem.setItemCode(updatedMenuItem.getItemCode());
            return menuItemsRepository.save(existingMenuItem); // Lưu món ăn đã cập nhật
        }
        return null; // Trả về null nếu không tìm thấy món ăn cần chỉnh sửa
    }

    public void deleteMenuItemById(Integer itemId, RedirectAttributes redirectAttributes) {
        try {
            menuItemsRepository.deleteById(itemId); // Xóa món ăn theo itemId
        } catch (DataIntegrityViolationException e) {
            // Xử lý lỗi ràng buộc khóa ngoại
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Vui lòng xóa món trong combo trước khi xóa sản phẩm.");
            throw new RuntimeException("Không thể xóa món ăn: " + e.getMessage());
        }
    }

    // Phương thức tìm kiếm món ăn
    public List<MenuItems> searchItems(String query) {
        return menuItemsRepository.findByNameContainingIgnoreCase(query); // Tìm kiếm món ăn theo tên
    }
    public MenuItems getMenuItemByToken(String token) {
        return menuItemsRepository.findByToken(token);
    }
    public List<MenuItems> getSimilarItems(String category) {
        return menuItemsRepository.findByCategory(category);
    }
    public List<MenuItems> getAllMenuItemsWithSoldQuantity() {
        List<MenuItems> menuItems = menuItemsRepository.findAll();
        List<Object[]> menuItemSales = orderDetailRepository.findMenuItemSales(LocalDateTime.now().minusMonths(1000), LocalDateTime.now());

        // Tạo map lưu số lượng đã bán theo tên món ăn
        Map<String, Integer> soldQuantityMap = new HashMap<>();
        for (Object[] row : menuItemSales) {
            if (row.length >= 2) {
                soldQuantityMap.put((String) row[0], ((Number) row[1]).intValue());
            }
        }

        // Gán số lượng đã bán vào từng món ăn
        for (MenuItems item : menuItems) {
            item.setSoldQuantity(soldQuantityMap.getOrDefault(item.getName(), 0));
        }

        return menuItems;
    }
}
