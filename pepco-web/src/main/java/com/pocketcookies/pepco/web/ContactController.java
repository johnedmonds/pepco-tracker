package com.pocketcookies.pepco.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ContactController {
	@RequestMapping(value = "/contact")
	public ModelAndView index() {
		return new ModelAndView("pepco.contact");
	}
}
