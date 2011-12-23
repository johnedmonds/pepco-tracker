package com.pocketcookies.pepco.web;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.dao.OutageDAO;
import java.sql.Timestamp;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ModelAndView index(@RequestParam(value = "asof", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") final DateTime asof) {
        final ModelAndView mav = new ModelAndView("pepco.outages");
        if (asof == null) {
            mav.getModel().put("outages", outageDao.getOutagesAtZoomLevelAsOf(new Timestamp(new DateTime().getMillis()), null, AbstractOutageRevision.class));
        } else {
            mav.getModel().put("outages", outageDao.getOutagesAtZoomLevelAsOf(new Timestamp(asof.getMillis()), null, AbstractOutageRevision.class));
        }
        return mav;
    }
}
