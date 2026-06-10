package com.devsecops.demo.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/health")
    public String health() {
        return "UP";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "DevSecOps Demo Running";
    }
}
