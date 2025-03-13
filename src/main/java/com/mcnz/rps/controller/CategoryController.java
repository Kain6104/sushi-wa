package com.mcnz.rps.controller;

import com.mcnz.rps.model.Category;
import com.mcnz.rps.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories";
    }

    @GetMapping("/add")
    public String addCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @PostMapping("/add")
    public String saveCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryService.findById(id);
        if (category != null) {
            model.addAttribute("category", category);
            return "admin/category-form";
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/edit")
    public String updateCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteById(id);
        return "redirect:/admin/categories";
    }
    @PostMapping
    public String saveOrUpdateCategory(@ModelAttribute Category category) {
        categoryService.save(category);
        return "redirect:/admin/categories";
    }

}
