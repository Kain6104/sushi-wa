package com.mcnz.rps.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.mcnz.rps.model.CartItem;
import com.mcnz.rps.service.CartService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@ControllerAdvice
public class CartItemCountControllerAdvice {

    @Autowired
    private CartService cartService; // Dịch vụ giỏ hàng của bạn

    @ModelAttribute
    public void addCartItemCountToModel(Model model, HttpSession session) {
        // Lấy giỏ hàng từ session
        List<CartItem> cartItems = cartService.getCart(session);

        // Tính tổng số lượng món trong giỏ hàng
        int cartItemCount = cartItems.stream()
                                     .mapToInt(CartItem::getQuantity)  // Tổng số lượng từ từng món
                                     .sum();

        // Thêm số lượng giỏ hàng vào mô hình cho tất cả các view
        model.addAttribute("cartItemCount", cartItemCount);
    }
}
