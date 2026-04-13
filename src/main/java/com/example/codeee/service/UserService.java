package com.example.codeee.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private Map<String, String> users = new HashMap<>();

    public UserService() {
        users.put("admin", "1234"); // default user
    }

    // ✅ LOGIN
    public boolean validate(String username, String password) {
        return users.containsKey(username) &&
                users.get(username).equals(password);
    }

    // ✅ REGISTER
    public String register(String username, String password) {
        if (users.containsKey(username)) {
            return "User already exists";
        }

        users.put(username, password);
        return "Registration Successful";
    }
}
