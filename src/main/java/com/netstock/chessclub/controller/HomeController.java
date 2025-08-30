package com.netstock.chessclub.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for the home page and leaderboard.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    
    /**
     * Display the home page with leaderboard
     * @param model Model for view
     * @return Home page view
     */
    @GetMapping("/")
    public String home(Model model) {
        return "index";
    }
    
}