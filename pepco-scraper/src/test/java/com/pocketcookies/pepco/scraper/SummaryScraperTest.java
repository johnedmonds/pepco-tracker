package com.pocketcookies.pepco.scraper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.pocketcookies.pepco.model.ParserRun;
import com.pocketcookies.pepco.model.Summary;

public class SummaryScraperTest {
    @Test
    public void testParseSummary() throws SAXException, IOException,
            ParserConfigurationException {
        final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(1));
        final Summary summary = SummaryScraper.parseSummary(
                TestUtil.loadXml(getClass().getResourceAsStream(
                        "/testxml/summary.xml")), run);
        assertEquals(4, summary.getTotalOutages());
        assertEquals(1, summary.getDcAffectedCustomers());
        assertEquals(10, summary.getDcTotalCustomers());
        assertEquals(2, summary.getPgAffectedCustomers());
        assertEquals(20, summary.getPgTotalCustomers());
        assertEquals(3, summary.getMontAffectedCustomers());
        assertEquals(30, summary.getMontTotalCustomers());
        assertEquals(run, summary.getRun());
        assertEquals(new DateTime().getYear(), new DateTime(summary
                .getWhenGenerated().getTime()).getYear());
    }
}
