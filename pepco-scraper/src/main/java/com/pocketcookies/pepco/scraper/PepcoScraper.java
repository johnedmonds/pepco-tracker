package com.pocketcookies.pepco.scraper;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.OutageArea;
import com.pocketcookies.pepco.model.OutageAreaRevision;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.ParserRun;
import com.pocketcookies.pepco.model.Summary;

/**
 * Scrapes Pepco's outages.
 * 
 * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
 */
public class PepcoScraper {
    private static final String DATA_HTML_PREFIX = "http://www.pepco.com/home/emergency/maps/stormcenter/data/";
    private static final String THEMATIC_SUFFIX = "thematic/current/thematic_areas.xml";
    private static final String SUMMARY_SUFFIX = "data.xml";
    private static final String DIRECTORY_SUFFIX = "/outages/metadata.xml";
    private static final int MAX_ZOOM = 15;
    private static final Logger logger = Logger.getLogger("PepcoScraper");

    private static final DateTimeFormatter pepcoDateFormatter = DateTimeFormat
            .forPattern("MMM d, h:mm a");

    private static String getTextFromOnlyElement(final Node element,
            final String tagName) {
        assert element instanceof Element || element instanceof Document;
        final NodeList elements;
        if (element instanceof Element) {
            elements = ((Element) element).getElementsByTagName(tagName);
        } else {
            elements = ((Document) element).getElementsByTagName(tagName);
        }
        assert elements.getLength() == 1;
        return elements.item(0).getTextContent();
    }

    /**
     * Parse one of Pepco's times. Normally, this would be as easy as just
     * calling pepcoDateFormatter.parseDateTime, but there's some special case
     * stuff that needs to happen on the first scrape of a new year. Since Pepco
     * doesn't include the year, on the first day of the year, we might
     * accidentally assume we are scraping data from January. So we have to do
     * some stuff to adjust for that. See the comment inside this method for
     * what exactly we do.
     * 
     * @param date
     *            The date to parse.
     * @param now
     *            What time it is right now.
     */
    private static DateTime parsePepcoDateTime(final String date,
            final DateTime now) {
        final DateTime ret = pepcoDateFormatter.parseDateTime(date);
        /*
         * Check whether it is January and we are parsing a date in December.
         * 
         * Pepco's dates don't include the year. So at the turn of the year (Jan
         * 1), we'll probably read something like "Dec 31 12:58 PM". It is very
         * unlikely that this actually represents December 31 of this year. More
         * likely, this is December 31 of last year (either that or some poor
         * group of customers is going to have to wait more than a year for
         * their power to get fixed).
         */
        if (now.getMonthOfYear() == 1 && now.getDayOfMonth() == 1
                && ret.getMonthOfYear() == 12 && ret.getDayOfMonth() == 31) {
            return ret.withYear(now.getYear() - 1);
        } else {
            return ret.withYear(now.getYear());
        }
    }

    private static DateTime parsePepcoDateTime(final String date) {
        return parsePepcoDateTime(date, new DateTime());
    }

    /**
     * Shorthand for pulling out the text content of the directory element.
     */
    private static String getOutagesFolderName(final Document doc) {
        return doc.getElementsByTagName("directory").item(0).getTextContent();
    }

    private static Summary parseSummary(final Document doc, final ParserRun run) {
        final NodeList areas = doc.getElementsByTagName("area");
        final Element dc = (Element) areas.item(0);
        final Element pg = (Element) areas.item(1);
        final Element mont = (Element) areas.item(2);
        return new Summary(Integer.parseInt(getTextFromOnlyElement(doc,
                "total_outages")), Integer.parseInt(getTextFromOnlyElement(dc,
                "custs_out")), Integer.parseInt(getTextFromOnlyElement(dc,
                "total_custs")), Integer.parseInt(getTextFromOnlyElement(pg,
                "custs_out")), Integer.parseInt(getTextFromOnlyElement(pg,
                "total_custs")), Integer.parseInt(getTextFromOnlyElement(mont,
                "custs_out")), Integer.parseInt(getTextFromOnlyElement(mont,
                "total_custs")), new Timestamp(parsePepcoDateTime(
                getTextFromOnlyElement(doc, "date_generated"), new DateTime())
                .getMillis()), run);
    }

    /**
     * Parses the given XML document and returns an Outage. The returned Outage
     * will have an OutageRevision as its only revision. The Outage and
     * OutageRevision are neither loaded from nor persisted to the database.
     */
    private static AbstractOutageRevision parseOutage(final Element item,
            final ParserRun run) {
        final org.jsoup.nodes.Document doc = Jsoup
                .parseBodyFragment(getTextFromOnlyElement(item, "description"));
        final String sCustomersAffected = ((TextNode) doc
                .select(":containsOwn(Customers Affected)").first()
                .nextSibling()).text().trim();
        final int customersAffected = sCustomersAffected.equals("Less than 5") ? 0
                : Integer.parseInt(sCustomersAffected);
        final DateTime earliestReport = parsePepcoDateTime(((TextNode) doc
                .select(":containsOwn(Report)").first().nextSibling()).text()
                .trim());
        Timestamp estimatedRestoration = null;
        try {
            final String sEstimatedRestoration = ((TextNode) doc
                    .select(":containsOwn(Restoration)").first().nextSibling())
                    .text().trim();
            estimatedRestoration = sEstimatedRestoration.equals("Pending") ? null
                    : new Timestamp(parsePepcoDateTime(sEstimatedRestoration)
                            .getMillis());
        } catch (IllegalArgumentException e) {
            logger.warn("Error parsing estimated restoration.", e);
        }

        final String sPoint = getTextFromOnlyElement(item, "point");
        if (item.getElementsByTagName("point").getLength() != 0) {
            final String sPoints[] = sPoint.split(" ");
            final PointDouble latLng = new PointDouble(
                    Double.parseDouble(sPoints[0]),
                    Double.parseDouble(sPoints[1]));
            final int numOutages = Integer.parseInt(((TextNode) doc
                    .select(":containsOwn(Number of Outage Orders)").first()
                    .nextSibling()).text().trim());
            final Outage outage = new Outage(latLng.lat, latLng.lon,
                    new Timestamp(earliestReport.getMillis()), null);
            final OutageClusterRevision outageRevision = new OutageClusterRevision(
                    customersAffected, estimatedRestoration, outage, run,
                    numOutages);
            outage.getRevisions().add(outageRevision);
            return outageRevision;
        } else {
            final PointDouble latLon = new Polygon(getTextFromOnlyElement(item,
                    "polygon")).getCenter();
            final String cause = ((TextNode) doc.select(":containsOwn(Cause)")
                    .first().nextSibling()).text().trim();
            final CrewStatus status = CrewStatus.valueOf(((TextNode) doc
                    .select(":containsOwn(Crew Status)").first().nextSibling())
                    .text().trim().replace(' ', '_').toUpperCase());
            final Outage outage = new Outage(latLon.lat, latLon.lon,
                    new Timestamp(earliestReport.getMillis()), null);
            final OutageRevision outageRevision = new OutageRevision(
                    customersAffected, estimatedRestoration, outage, run,
                    cause, status);
            outage.getRevisions().add(outageRevision);
            return outageRevision;
        }
    }

    /**
     * Parses a single thematic area into an OutageAreaRevision.
     */
    private static OutageAreaRevision parseThematicArea(final Node area,
            final ParserRun run) {
        final String sCustomersOut = ((TextNode) Jsoup
                .parseBodyFragment(getTextFromOnlyElement(area, "description"))
                .select(":containsOwn(Customers Affected)").first()
                .nextSibling()).text().trim();
        final int customersOut = sCustomersOut.equals("Less than 5") ? 0
                : Integer.parseInt(sCustomersOut);
        return new OutageAreaRevision(new OutageArea(getTextFromOnlyElement(
                area, "title")), customersOut, run);
    }
    
    
}