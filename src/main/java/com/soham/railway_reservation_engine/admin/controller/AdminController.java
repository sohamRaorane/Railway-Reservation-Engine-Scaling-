package com.soham.railway_reservation_engine.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    @GetMapping("/api/v1/admin/test")
    public String adminTest(){
        return "Welcome Admin";
    }
}
