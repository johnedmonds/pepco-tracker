package com.pocketcookies.pepco.scraper;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableList;

public class PepcoUtil {
    public static final DateTimeFormatter PEPCO_DATE_FORMATTER = DateTimeFormat
            .forPattern("MMM d, h:mm a");
    
    /**
     * Taken from
     * http://www.pepco.com/home/emergency/maps/stormcenter/scripts/ifactor
     * /stormcenter.js
     * 
     * @param lat
     * @param lon
     * @param zoom
     * @return
     */
    public static List<String> getSpatialIndicesForPoint(double lat, double lon,
            int zoom) {
        Double minLat = -85.05112878;
        Double maxLat = 85.05112878;
        Double minLong = -180.0;
        Double maxLong = 180.0;
        String indexName = "";
        int A = 0;
        int curZoom = 0;
        int D = 0;
        List<String> indexNames = new LinkedList<String>();
        String[] zoomLevels = new String[] { "0", "1", "2", "3" };
        for (; curZoom < zoom; curZoom++) {
            A = 0;
            if (lat < ((maxLat + minLat) / 2)) {
                A = A + 2;
                maxLat = (maxLat + minLat) / 2;
            } else {
                minLat = (maxLat + minLat) / 2;
            }
            if (lon > ((maxLong + minLong) / 2)) {
                A = A + 1;
                minLong = (maxLong + minLong) / 2;
            } else {
                maxLong = (maxLong + minLong) / 2;
            }
            indexName = indexName + A;
        }
        if (indexName.length() > 1) {
            indexName = indexName.substring(0, indexName.length() - 2);
            for (curZoom = 0; curZoom < zoomLevels.length; curZoom++) {
                for (D = 0; D < zoomLevels.length; D++) {
                    indexNames.add(indexName + zoomLevels[curZoom]
                            + zoomLevels[D]);
                }
            }
        } else {
            indexNames = ImmutableList.<String> copyOf(zoomLevels);
        }
        return indexNames;
    }

    public static String getTextFromOnlyElement(final Node element,
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
    
    public static DateTime parsePepcoDateTime(final String data) {
        return parsePepcoDateTime(data, new DateTime());
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
    public static DateTime parsePepcoDateTime(final String date,
            final DateTime now) {
        final DateTime ret = PEPCO_DATE_FORMATTER.parseDateTime(date);
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
}