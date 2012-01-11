package com.pocketcookies.pepco.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Exists purely to handle requests to /contact and return the static contact page.
 * @author jack
 */
@Controller
public class ContactController {

    /**
     * Returns the static contact page.
     * @return 
     */
    @RequestMapping(value = "/contact")
    public ModelAndView index() {
        return new ModelAndView("pepco.contact");
    }
}
