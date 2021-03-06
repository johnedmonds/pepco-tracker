package com.pocketcookies.pepco.web;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.dao.OutageDAO;
import com.pocketcookies.pepco.web.util.ResourceNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * This is the home page for the outages section.
     * 
     * Retrieves the list of open (not repaired) outages as of @asof.
     * @param asof The time in history at which to check for open outages.
     * @return 
     */
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

    /**
     * Retrieves the page showing the history of a particular outage.
     * @param outageId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{outageId}")
    public ModelAndView outage(@PathVariable(value = "outageId") final int outageId) {
        final ModelAndView mav = new ModelAndView("pepco.outages.history");
        final Outage o = outageDao.getOutage(outageId);
        if (o == null) {
            throw new ResourceNotFoundException();
        }
        mav.getModel().put("outage", o);
        //We need to reference outages in essentially random access to determine which attributes have changed.
        //Thus, we need a list with fast random access.
        //That is why we store outageRevisions as a separate ArrayList rather than allowing the jsp to rely on ${outage.revisions}.
        //${outage.revisions} is a SortedSet which doesn't allow random access.
        mav.getModel().put("outageRevisions", new ArrayList<AbstractOutageRevision>(o.getRevisions()));
        return mav;
    }
}
