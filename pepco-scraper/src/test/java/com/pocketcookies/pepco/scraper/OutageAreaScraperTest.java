package com.pocketcookies.pepco.scraper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.ImmutableList;
import com.pocketcookies.pepco.model.OutageAreaRevision;
import com.pocketcookies.pepco.model.ParserRun;

public class OutageAreaScraperTest {
    @Test
    public void testParseArea() throws SAXException, IOException,
            ParserConfigurationException {
        final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(1));
        final NodeList areasNodeList = TestUtil.loadXml(
                getClass().getResourceAsStream("/testxml/thematic.xml"))
                .getElementsByTagName("item");
        final ImmutableList.Builder<OutageAreaRevision> builder = ImmutableList
                .builder();
        for (int i = 0; i < areasNodeList.getLength(); i++) {
            builder.add(OutageAreaScraper.parseOutageArea(
                    areasNodeList.item(i), run));
        }
        final List<OutageAreaRevision> areas = builder.build();
        assertEquals(2, areas.size());
        assertEquals("00000,00001", areas.get(0).getArea().getId());
        assertEquals(10, areas.get(0).getCustomersOut());
        assertEquals("00002", areas.get(1).getArea().getId());
        assertEquals(0, areas.get(1).getCustomersOut());
        assertEquals(run, areas.get(0).getParserRun());
        assertEquals(run, areas.get(1).getParserRun());
    }
}
