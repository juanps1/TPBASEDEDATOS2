package com.tp.persistencia.persistencia_poliglota.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthWebController {

    @GetMapping("/")
    public String home() {
        return "redirect:/app/dashboard";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas");
        }
        
        if (logout != null) {
            model.addAttribute("message", "Sesión cerrada correctamente");
        }
        
        return "auth/login";
    }
}
