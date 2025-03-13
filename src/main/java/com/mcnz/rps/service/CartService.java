package com.mcnz.rps.service;

import com.mcnz.rps.model.CartItem;
import com.mcnz.rps.model.Combo;
import com.mcnz.rps.model.MenuItems;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "cart";

    public List<CartItem> getCart(HttpSession session) {
        List<CartItem> cart = (List<CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addItemToCart(MenuItems menuItem, HttpSession session) {
        List<CartItem> cart = getCart(session);
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getId().equals(menuItem.getItemId()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().incrementQuantity(); // Tăng số lượng nếu đã tồn tại trong giỏ
        } else {
            // Thêm đối tượng MenuItems đầy đủ vào CartItem
            cart.add(new CartItem(menuItem.getItemId(), menuItem.getName(), menuItem.getPrice(), 1, menuItem.getImageUrl(), menuItem));
        }
        session.setAttribute(CART_SESSION_KEY, cart); // Cập nhật giỏ hàng trong session
    }


    public int getCartItemCount(HttpSession session) {
        return getCart(session).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public void removeItemFromCart(Integer id, HttpSession session) {
        List<CartItem> cart = getCart(session);
        cart.removeIf(item -> item.getId().equals(id));
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateItemQuantity(Integer id, int quantity, HttpSession session) {
        List<CartItem> cart = getCart(session);
        for (CartItem item : cart) {
            if (item.getId().equals(id)) {
                item.setQuantity(quantity);
                break;
            }
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }
    public double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                        .mapToDouble(item -> item.getPrice() * item.getQuantity())
                        .sum();
    }
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY); // Xóa giỏ hàng
    }
    public void addComboToCart(Combo combo, HttpSession session) {
        List<CartItem> cart = getCart(session);  // Lấy giỏ hàng từ session

        // Kiểm tra xem combo đã có trong giỏ chưa
        Optional<CartItem> existingItem = cart.stream()
                .filter(item -> item.getId().equals(combo.getComboId()))  // Kiểm tra theo combo ID
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().incrementQuantity(); // Tăng số lượng nếu combo đã có trong giỏ
        } else {
            // Tạo CartItem từ combo và thêm vào giỏ
            CartItem comboItem = new CartItem(combo.getComboId(), combo.getComboName(), combo.getPrice(), 1, combo.getImageUrl(), null);
            cart.add(comboItem); // Thêm combo mới vào giỏ
        }

        // Cập nhật giỏ hàng trong session
        session.setAttribute(CART_SESSION_KEY, cart);
    }

}
