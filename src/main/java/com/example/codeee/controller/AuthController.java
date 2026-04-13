package com.example.codeee.controller;

import com.example.codeee.model.LoginRequest;
import com.example.codeee.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    // ✅ LOGIN
    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        if (userService.validate(request.getUsername(), request.getPassword())) {
            return "Login Successful";
        } else {
            return "Invalid Credentials";
        }
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public String register(@RequestBody LoginRequest request) {
        return userService.register(
                request.getUsername(),
                request.getPassword()
        );
    }
}
