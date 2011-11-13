package com.pocketcookies.pepco.web;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.dao.OutageDAO;

@Controller
public class PepcoOutageController {

    final OutageDAO outageDao;

    @Autowired
    public PepcoOutageController(
            @Qualifier("outageDao") final OutageDAO outageDao) {
        this.outageDao = outageDao;
    }

    /**
     * Wraps a regular outage so that it can be serialized by Jackson.
     */
    public static class OutageRevisionWrapper {

        public final double lat, lon;
        public final long earliestReport;
        public final long observedEnd;
        public final int customersAffected;
        public final long estimatedRestoration;
        public final String cause;
        public final String status;

        public OutageRevisionWrapper(final OutageRevision revision) {
            super();
            this.lat = revision.getOutage().getLat();
            this.lon = revision.getOutage().getLon();
            this.earliestReport = revision.getOutage().getEarliestReport().getTime();
            this.observedEnd = revision.getOutage().getObservedEnd() == null ? -1
                    : revision.getOutage().getObservedEnd().getTime();
            this.customersAffected = revision.getNumCustomersAffected();
            this.estimatedRestoration = revision.getEstimatedRestoration() == null ? -1
                    : revision.getEstimatedRestoration().getTime();
            this.cause = revision.getCause();
            this.status = WordUtils.capitalize(revision.getStatus().toString().replace("_", " ").toLowerCase());
        }

        public OutageRevisionWrapper(double lat, double lon,
                long earliestReport, long observedEnd, int customersAffected,
                long estimatedRestoration, String cause, String status) {
            super();
            this.lat = lat;
            this.lon = lon;
            this.earliestReport = earliestReport;
            this.observedEnd = observedEnd;
            this.customersAffected = customersAffected;
            this.estimatedRestoration = estimatedRestoration;
            this.cause = cause;
            this.status = status;
        }

        @Override
        public boolean equals(final Object o) {
            final OutageRevisionWrapper w = (OutageRevisionWrapper) o;
            return w.lat == lat && w.lon == lon
                    && w.earliestReport == earliestReport
                    && w.observedEnd == observedEnd
                    && w.customersAffected == customersAffected
                    && w.estimatedRestoration == estimatedRestoration
                    && w.cause.equals(cause) && w.status.equals(status);
        }

        @Override
        public int hashCode() {
            return (int) (lat + lon + earliestReport + observedEnd
                    + customersAffected + estimatedRestoration
                    + cause.hashCode() + status.hashCode());
        }
    }

    @RequestMapping(value = "/outage-data", method = RequestMethod.GET)
    public @ResponseBody
    Collection<OutageRevisionWrapper> getOutagesAsOf(
            @RequestParam(value = "timestamp", defaultValue = "-1") final long timestamp) {
        final Collection<AbstractOutageRevision> revisions = this.outageDao.getOutagesAtZoomLevelAsOf(
                new Timestamp(timestamp < 0 ? new Date().getTime()
                : timestamp), null, OutageRevision.class);
        final Collection<OutageRevisionWrapper> wrapper = new ArrayList<OutageRevisionWrapper>(
                revisions.size());
        for (final AbstractOutageRevision r : revisions) {
            r.getOutage();
            wrapper.add(new OutageRevisionWrapper((OutageRevision) r));
        }
        return wrapper;
    }
}
