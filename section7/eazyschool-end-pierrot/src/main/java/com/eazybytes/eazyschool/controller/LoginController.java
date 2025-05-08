package com.eazybytes.eazyschool.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
public class LoginController {

    @GetMapping("/login")
    public String displayLoginPage(@RequestParam(required = false) String error, Model model,
                                   @RequestParam(required = false) String logout) {
        if (error != null) {
            model.addAttribute("errorMessage",
                     "Invalid username or password. Please try again.");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage",
                    "You have been logged out successfully.");
        }

        return "login.html";
    }

}