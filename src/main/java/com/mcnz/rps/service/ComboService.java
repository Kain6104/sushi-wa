package com.mcnz.rps.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mcnz.rps.model.Combo;
import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.repository.ComboRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ComboService {

    @Autowired
    private ComboRepository comboRepository;

    public List<Combo> getAllCombos() {
        return comboRepository.findAll();
    }

    public Combo saveCombo(Combo combo) {
        return comboRepository.save(combo);
    }

    public void deleteCombo(Long comboId) {
        comboRepository.deleteById(comboId);
    }
    // Thay đổi kiểu id sang Long
    public Combo getComboById(Long id) {
        return comboRepository.findById(id).orElse(null);  // Tìm combo theo ID hoặc trả về null nếu không tìm thấy
    }
    public Combo updateCombo(Long comboId, Combo updatedCombo) {
        Combo existingCombo = comboRepository.findById(comboId).orElse(null);
        if (existingCombo != null) {
            // Cập nhật thông tin của Combo
            existingCombo.setComboName(updatedCombo.getComboName());
            existingCombo.setPrice(updatedCombo.getPrice());
            existingCombo.setDescription(updatedCombo.getDescription());
            existingCombo.setImageUrl(updatedCombo.getImageUrl());

            // Tạo danh sách mới cho món ăn
            List<MenuItems> updatedMenuItems = updatedCombo.getMenuItems();
            existingCombo.setMenuItems(new ArrayList<>(updatedMenuItems)); // Sử dụng danh sách mới

            // Lưu combo đã chỉnh sửa
            return comboRepository.save(existingCombo);
        }
        throw new RuntimeException("Combo không tồn tại");
    }
    public Combo getComboByToken(String token) {
        return comboRepository.findByToken(token);
    }

}
