package com.airline.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Home Controller Handles requests for the home page
 */
@Controller
public class HomeController {

    /**
     * Home page - Display index
     */
    @GetMapping("/")
    public String home(Model model) {
        return "user/landing";
    }

    /**
     * Test page
     */
    @GetMapping("/test")
    public String test(Model model) {
        model.addAttribute("title", "Test Page");
        model.addAttribute("content", "Configuration is successful!");
        return "test";
    }

}
