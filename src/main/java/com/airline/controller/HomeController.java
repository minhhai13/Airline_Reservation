package com.airline.controller;

import com.airline.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private RouteService routeService;

    @GetMapping("/")
    public String home(Model model) {
        try {
            List<String> origins = routeService.getAllOrigins();
            List<String> destinations = routeService.getAllDestinations();
            model.addAttribute("origins", origins != null ? origins : new ArrayList<>());
            model.addAttribute("destinations", destinations != null ? destinations : new ArrayList<>());
        } catch (Exception e) {
            // If database not connected, use empty lists
            System.out.println("Warning: Could not load routes: " + e.getMessage());
            model.addAttribute("origins", new ArrayList<>());
            model.addAttribute("destinations", new ArrayList<>());
        }
        return "index";
    }
}

