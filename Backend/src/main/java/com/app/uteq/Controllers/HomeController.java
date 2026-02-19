package com.app.uteq.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    private static final String VERSION = "1.0.0";

    @GetMapping
    public String home() {
        return "La API está funcionando con v" + VERSION;
    }
}
