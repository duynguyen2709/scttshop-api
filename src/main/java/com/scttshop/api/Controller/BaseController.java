package com.scttshop.api.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {

    @GetMapping("/")
    public String index(){
        return "SCTT-SHOP REST API HOMEPAGE";
    }
}

