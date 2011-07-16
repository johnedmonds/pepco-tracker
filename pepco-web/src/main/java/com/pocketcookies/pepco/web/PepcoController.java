package com.pocketcookies.pepco.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.pocketcookies.pepco.model.Summary;
import com.pocketcookies.pepco.model.dao.OutageDAO;

@Controller
public class PepcoController {
	private final OutageDAO outageDao;

	@Autowired
	public PepcoController(@Qualifier("outageDao") final OutageDAO outageDao) {
		this.outageDao = outageDao;
	}

	@RequestMapping(value = "/web")
	public ModelAndView index() {
		final List<Summary> summaries = this.outageDao.getSummaries(null, null,
				true, 2);
		final Summary currentSummary = summaries.get(0);
		// final Summary previousSummary = summaries.get(1);
		final ModelAndView mav = new ModelAndView("pepco.homepage");
		mav.addObject("summary", currentSummary);
		return mav;
	}
}