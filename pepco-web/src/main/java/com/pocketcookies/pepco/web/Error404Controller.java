package com.pocketcookies.pepco.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class Error404Controller {
	@RequestMapping(value = "/err/404")
	public String handle404() {
		return "pepco.404";
	}
}
