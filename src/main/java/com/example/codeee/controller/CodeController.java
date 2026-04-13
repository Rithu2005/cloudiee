package com.example.codeee.controller;

import com.example.codeee.model.CodeRequest;
import com.example.codeee.service.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class CodeController {

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/execute")
    public String execute(@RequestBody CodeRequest request) {
        return executionService.runCode(
                request.getLanguage(),
                request.getCode(),
                request.getInput()
        );
    }
}
