package com.example.lab1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController {

    @GetMapping("/text")
    public String getText(){
        return "Hello world:)";
    }

    @GetMapping("/number")
    public int getNumber(){
        return 21;
    }
}
