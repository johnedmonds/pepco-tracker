package com.pocketcookies.pepco.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value="/bot")
public class BotController{
    @RequestMapping(value="")
    public ModelAndView index(){
        return new ModelAndView("pepco.bot");
    }
}