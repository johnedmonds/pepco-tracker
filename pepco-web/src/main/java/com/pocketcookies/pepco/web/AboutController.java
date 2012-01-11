package com.pocketcookies.pepco.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Exists purely to handle requests to /about and return the static about page.
 * @author jack
 */
@Controller
public class AboutController {
    
        /**
         * Required so we can serve the (basically static) about page.
         * @return 
         */
	@RequestMapping(value = "/about")
	public ModelAndView index() {
		return new ModelAndView("pepco.about");
	}
}
