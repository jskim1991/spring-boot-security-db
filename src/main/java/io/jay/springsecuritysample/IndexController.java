package io.jay.springsecuritysample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @GetMapping
    public String hello() {
        return "Hello user";
    }
}
