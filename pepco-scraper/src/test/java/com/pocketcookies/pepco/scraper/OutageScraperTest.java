package com.pocketcookies.pepco.scraper;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.Collection;

import org.joda.time.DateTime;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.ParserRun;

public class OutageScraperTest {
    @Test
    public void testParseOutage() {
      final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
      final Collection<AbstractOutageRevision> revisions = OutageScraper.parseOutages(TestUtil.loadXml(getClass().getResourceAsStream("/testxml/outages_single.xml")).getElementsByTagName("item"),run);
      final AbstractOutageRevision abstractOutageRevision=Iterables.getOnlyElement(revisions);
      assertEquals(OutageRevision.class, abstractOutageRevision.getClass());
      final OutageRevision revision = (OutageRevision) abstractOutageRevision;
      assertEquals(10, revision.getNumCustomersAffected());
      assertEquals("Jan 1, 1:00 PM", PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision.getOutage().getEarliestReport().getTime())));
      assertEquals("Jan 1, 2:00 PM", PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision.getEstimatedRestoration().getTime())));
      assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear());
      assertEquals(new DateTime().getYear(),new DateTime(revision.getEstimatedRestoration().getTime()).getYear());
      assertEquals(2, revision.getOutage().getLat(), .0001);
      assertEquals(3, revision.getOutage().getLon(), .0001);
      assertEquals("Under Evaluation", revision.getCause());
      assertEquals(CrewStatus.PENDING, revision.getStatus());
      assertEquals(2, revision.getRun().getAsof().getTime());
    }
    
    @Test
    public void testParseOutageCluster() {
      final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
      final AbstractOutageRevision abstractOutageRevision = Iterables.getOnlyElement(OutageScraper.parseOutages(TestUtil.loadXml(getClass().getResourceAsStream("/testxml/outages_cluster.xml")).getElementsByTagName("item"), run));
      assertEquals(OutageClusterRevision.class, abstractOutageRevision.getClass());
      final OutageClusterRevision revision = (OutageClusterRevision) abstractOutageRevision;
      assertEquals(10, revision.getNumCustomersAffected());
      
      assertEquals("Jan 1, 1:00 PM", PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision.getOutage().getEarliestReport().getTime())));
      assertEquals("Jan 1, 2:00 PM", PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision.getEstimatedRestoration().getTime())));
      assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear());
      assertEquals(new DateTime().getYear(),new DateTime(revision.getEstimatedRestoration().getTime()).getYear());
      assertEquals(1, revision.getOutage().getLat(), .0001);
      assertEquals(2, revision.getOutage().getLon(), .0001);
      assertEquals(2, revision.getNumOutages());
      assertEquals(2, revision.getRun().getAsof().getTime());
    }
    @Test
    public void testParsePendingEstimatedRestorationOutageCluster() {
      final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
      final AbstractOutageRevision abstractOutageRevision = Iterables.getOnlyElement(OutageScraper.parseOutages(TestUtil.loadXml(getClass().getResourceAsStream("/testxml/outages_cluster_pending_restoration.xml")).getElementsByTagName("item"), run));
      assertEquals(OutageClusterRevision.class, abstractOutageRevision.getClass());
      final OutageClusterRevision revision = (OutageClusterRevision) abstractOutageRevision;
      assertEquals(10, revision.getNumCustomersAffected());
      assertEquals("Jan 1, 1:00 PM", PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision.getOutage().getEarliestReport().getTime())));
      //The year should always be the current year as Pepco does not send back the year of the outage.
      assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear());
      assertEquals(1, revision.getOutage().getLat(), .0001);
      assertEquals(2, revision.getOutage().getLon(), .0001);
      assertEquals(2, revision.getNumOutages());
      assertEquals(2, revision.getRun().getAsof().getTime());
      assertNull(revision.getEstimatedRestoration());
    }
    @Test
    public void testParsePendingRestorationOutage() {
      final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
      final AbstractOutageRevision abstractOutageRevision = Iterables.getOnlyElement(OutageScraper.parseOutages(TestUtil.loadXml(getClass().getResourceAsStream("/testxml/outages_single_pending_restoration.xml")).getElementsByTagName("item"), run));
      assertEquals(OutageRevision.class, abstractOutageRevision.getClass());
      final OutageRevision revision = (OutageRevision) abstractOutageRevision;
      assertEquals(10, revision.getNumCustomersAffected());
      assertEquals("Jan 1, 1:00 PM", PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision.getOutage().getEarliestReport().getTime())));
      assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear());
      assertEquals(2, revision.getOutage().getLat(), .0001);
      assertEquals(3, revision.getOutage().getLon(), .0001);
      assertEquals("Under Evaluation", revision.getCause());
      assertEquals(CrewStatus.PENDING, revision.getStatus());
      assertEquals(2, revision.getRun().getAsof().getTime());
      assertNull(revision.getEstimatedRestoration());
    }

    @Test
    def testZoomInOnCluster() = {
      val run = new ParserRun(new Timestamp(1), new Timestamp(1));
      val outageDao = mock(classOf[OutageDAO]);
      val loader = mock(classOf[StormCenterLoader]);
      val indicesZoom8=PepcoUtil.getSpatialIndicesForPoint(0, 0, 8);
      val now=DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss").print(new DateTime());
      when(loader.loadXMLRequest(PepcoScraper.dataHTMLPrefix+"outages/"+now+"/"+indicesZoom8.head+".xml")).thenReturn(XML.load(getClass().getResourceAsStream("/zoomOutageClusterXml/outages_cluster.xml")));
      
      val indicesZoom9=PepcoUtil.getSpatialIndicesForPoint(1,2,9);
      when(loader.loadXMLRequest(PepcoScraper.dataHTMLPrefix+"outages/"+now+"/"+indicesZoom9.head+".xml")).thenReturn(XML.load(getClass().getResourceAsStream("/zoomOutageClusterXml/outages_single.xml")));
      PepcoScraper.scrapeAllOutages(new PointDouble(0, 0), 8, now, loader, run, outageDao, new HashSet[String](), new HashSet[Integer]());
      (indicesZoom8 ::: indicesZoom9).foreach(x=>verify(loader).loadXMLRequest(PepcoScraper.dataHTMLPrefix+"outages/"+now+"/"+x+".xml"))
    }
}
