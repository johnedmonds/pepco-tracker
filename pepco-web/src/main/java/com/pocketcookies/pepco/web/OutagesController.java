package com.pocketcookies.pepco.web;

import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.dao.OutageDAO;
import com.pocketcookies.pepco.web.util.ResourceNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for a list of outages.
 * This makes it similar to OutageMapController except that outages are displayed as a list rather than on a map.
 * Also handles requests for details about a specific outage.
 * 
 * @author John Edmonds
 */
@Controller
@RequestMapping(value = "/outages")
public class OutagesController {

    private final OutageDAO outageDao;

    @Autowired
    public OutagesController(
            @Qualifier("outageDao") final OutageDAO outageDao) {
        this.outageDao = outageDao;
    }

    @RequestMapping(method = RequestMethod.GET, value = "")
    public ModelAndView index(@RequestParam(value = "asof", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") DateTime asof) {
        final ModelAndView mav = new ModelAndView("pepco.outages");
        if (asof == null) {
            asof = new DateTime();
        }
        mav.getModel().put("outages", outageDao.getOutagesAtZoomLevelAsOf(new Timestamp(asof.getMillis()), null, OutageRevision.class));
        mav.getModel().put("asof", new java.util.Date(asof.getMillis()));
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{outageId}")
    public ModelAndView outage(@PathVariable(value = "outageId") final int outageId) throws IOException {
        final ModelAndView mav = new ModelAndView("pepco.outages.history");
        final Outage o = outageDao.getOutage(outageId);
        if (o == null) {
            throw new ResourceNotFoundException();
        }
        mav.getModel().put("outage", outageDao.getOutage(outageId));
        return mav;
    }
}
