package com.pocketcookies.pepco.web;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.pocketcookies.pepco.model.Summary;
import com.pocketcookies.pepco.model.dao.SummaryDAO;

@Controller
public class PepcoController {
	private final SummaryDAO summaryDao;

	@Autowired
	public PepcoController(@Qualifier("summaryDao") final SummaryDAO summaryDao) {
		this.summaryDao = summaryDao;
	}

	@RequestMapping(value = "/")
	public ModelAndView index() {
		final Summary summary = this.summaryDao.getMostRecentSummary();
		// final Summary previousSummary = summaries.get(1);
		final ModelAndView mav = new ModelAndView("pepco.homepage");
		mav.addObject("summary", summary);
		return mav;
	}

	@RequestMapping(value = "/summary-data", method = RequestMethod.GET)
	@ResponseBody
	public String summary() throws JSONException {
		final JSONArray dcdata = new JSONArray(), pgdata = new JSONArray(), montdata = new JSONArray();
		final JSONObject dc = new JSONObject().put("label", "DC")
				.put("color", "#0157AB").put("data", dcdata);
		final JSONObject pg = new JSONObject().put("label", "Prince George")
				.put("color", "#888888").put("data", pgdata);
		final JSONObject mont = new JSONObject().put("label", "Montgomery")
				.put("color", "#21941B").put("data", montdata);

		for (final Summary s : this.summaryDao.getSummaries(null, null, false,
				0)) {
			dcdata.put(new JSONArray().put(s.getWhenGenerated().getTime()).put(
					s.getDcAffectedCustomers()));
			pgdata.put(new JSONArray().put(s.getWhenGenerated().getTime()).put(
					s.getPgAffectedCustomers()));
			montdata.put(new JSONArray().put(s.getWhenGenerated().getTime())
					.put(s.getMontAffectedCustomers()));
		}

		return new JSONArray().put(dc).put(pg).put(mont).toString();
	}

	@RequestMapping(value = "/summary-csv", method = RequestMethod.GET)
	public void summaryCsv(final HttpServletResponse response)
			throws IOException {
		response.setContentType("text/csv");
		final StringBuilder sb = new StringBuilder(
				"Observation Timestamp,DC Affected,PG Affected,Montgomery Affected,DC Total,PG Total,Montgomery Total\r\n");
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (final Summary s : this.summaryDao.getSummaries(null, null, false,
				0)) {
			sb.append(sdf.format(s.getWhenGenerated())).append(",")
					.append(s.getDcAffectedCustomers()).append(",")
					.append(s.getPgAffectedCustomers()).append(",")
					.append(s.getMontAffectedCustomers()).append(",")
					.append(s.getDcTotalCustomers()).append(",")
					.append(s.getPgTotalCustomers()).append(",")
					.append(s.getMontTotalCustomers()).append("\r\n");
		}
		response.setContentLength(sb.length());
		response.getOutputStream().write(sb.toString().getBytes());
		response.getOutputStream().close();
	}
}
