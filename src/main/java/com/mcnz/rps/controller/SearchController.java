package com.mcnz.rps.controller;

import com.mcnz.rps.model.MenuItems;
import com.mcnz.rps.model.Order;
import com.mcnz.rps.model.Promotion;
import com.mcnz.rps.service.MenuItemsService;
import com.mcnz.rps.service.OrderService;
import com.mcnz.rps.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    @Autowired
    private MenuItemsService menuItemsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PromotionService promotionService;

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
    	   // Tìm kiếm các món ăn
        List<MenuItems> searchedMenuItems = menuItemsService.searchItems(query);

        // Tìm kiếm các khuyến mãi
        List<Promotion> searchedPromotions = promotionService.searchPromotions(query);

        // Tìm kiếm đơn hàng dựa trên mã đơn hàng hoặc tên khách hàng
        List<Order> searchedOrders = orderService.searchOrders(query);

        // Thêm kết quả tìm kiếm vào model
        model.addAttribute("searchedMenuItems", searchedMenuItems);
        model.addAttribute("searchedPromotions", searchedPromotions);
        model.addAttribute("searchedOrders", searchedOrders);
        model.addAttribute("query", query);
       
        return "search-results"; // Trang view sẽ hiển thị kết quả tìm kiếm
    }
}
