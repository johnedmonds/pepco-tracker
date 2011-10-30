package com.pocketcookies.pepco.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for the outage-maps page.
 * @author jack
 */
@Controller
@RequestMapping(value="/outage-map")
public class OutageMapController {

    @RequestMapping(value = "")
    public ModelAndView index() {
        return new ModelAndView("pepco.outage.map");
    }
}
