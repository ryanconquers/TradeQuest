package controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class MarketController {

    @GetMapping("/market")
    @ResponseBody
    public String getMarket(){
        return "Market Data";
    }
}