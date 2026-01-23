package ca.hanson.shiftflow_backend.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testController {



    @GetMapping("/")
    public String WelcomeMessage() {
        return "Hello to shiftflow backend!";
    }


}
