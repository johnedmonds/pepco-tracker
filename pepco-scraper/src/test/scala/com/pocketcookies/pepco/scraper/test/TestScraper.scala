package com.pocketcookies.pepco.scraper.test

import org.junit._
import Assert._
import org.mockito.Mockito._
import org.junit.Test
import com.pocketcookies.pepco.scraper.PepcoScraper
import scala.xml.XML
import com.pocketcookies.pepco.model.Summary
import com.pocketcookies.pepco.model.ParserRun
import java.sql.Timestamp
import java.io.InputStream
import org.xml.sax.InputSource
import com.pocketcookies.pepco.model.OutageArea
import com.pocketcookies.pepco.model.OutageAreaRevision
import org.jsoup.Jsoup
import com.pocketcookies.pepco.model.OutageRevision
import com.pocketcookies.pepco.model.AbstractOutageRevision
import org.joda.time.DateTime
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus
import com.pocketcookies.pepco.model.OutageClusterRevision
import com.pocketcookies.pepco.scraper.PepcoUtil
import com.pocketcookies.pepco.model.dao.OutageDAO
import com.pocketcookies.pepco.scraper.PointDouble
import scala.collection.mutable.HashSet
import com.pocketcookies.pepco.scraper.StormCenterLoader
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

@Test
class ScraperTest {

  @Test
  def testParseSummary() = {
    val run = new ParserRun(new Timestamp(1), new Timestamp(1));
    val s: Summary = PepcoScraper.parseSummary(XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/summary.xml")), run)
    assertEquals(4, s.getTotalOutages())
    assertEquals(1, s.getDcAffectedCustomers())
    assertEquals(10, s.getDcTotalCustomers())
    assertEquals(2, s.getPgAffectedCustomers())
    assertEquals(20, s.getPgTotalCustomers())
    assertEquals(3, s.getMontAffectedCustomers())
    assertEquals(30, s.getMontTotalCustomers())
    assertEquals(run, s.getRun())
    assertEquals(new DateTime().getYear(), new DateTime(s.getWhenGenerated().getTime()).getYear())
  }
  @Test
  def testParseArea() = {
    val run = new ParserRun(new Timestamp(1), new Timestamp(1))
    val areas: Seq[OutageAreaRevision] = (XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/thematic.xml")) \\ "item")
      .map(n => PepcoScraper.parseThematicArea(n, run))
    assertEquals(2, areas.size)
    assertEquals("00000,00001", areas(0).getArea().getId())
    assertEquals(10, areas(0).getCustomersOut())
    assertEquals("00002", areas(1).getArea().getId())
    assertEquals(0, areas(1).getCustomersOut())
    assertEquals(run, areas(0).getParserRun())
    assertEquals(run, areas(1).getParserRun())
  }
  @Test
  def testParseOutage() = {
    val run = new ParserRun(new Timestamp(1), new Timestamp(2))
    val revision: OutageRevision = PepcoScraper.parseOutage((XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/outages_single.xml")) \\ "item")(0), run) match { case r: OutageRevision => r case _ => throw new Exception("Wrong type") }
    assertEquals(10, revision.getNumCustomersAffected())
    assertEquals("Jan 1, 1:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getOutage().getEarliestReport().getTime())))
    assertEquals("Jan 1, 2:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getEstimatedRestoration().getTime())))
    assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear())
    assertEquals(new DateTime().getYear(),new DateTime(revision.getEstimatedRestoration().getTime()).getYear())
    assertEquals(2, revision.getOutage().getLat(), .0001)
    assertEquals(3, revision.getOutage().getLon(), .0001)
    assertEquals("Under Evaluation", revision.getCause())
    assertEquals(CrewStatus.PENDING, revision.getStatus())
    assertEquals(2, revision.getRun().getAsof().getTime())
  }
  @Test
  def testParseOutageCluster() = {
    val run = new ParserRun(new Timestamp(1), new Timestamp(2))
    val revision: OutageClusterRevision = PepcoScraper.parseOutage((XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/outages_cluster.xml")) \\ "item")(0), run) match { case r: OutageClusterRevision => r case _ => throw new Exception("Wrong type") }
    assertEquals(10, revision.getNumCustomersAffected())
    assertEquals("Jan 1, 1:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getOutage().getEarliestReport().getTime())))
    assertEquals("Jan 1, 2:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getEstimatedRestoration().getTime())))
    assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear())
    assertEquals(new DateTime().getYear(),new DateTime(revision.getEstimatedRestoration().getTime()).getYear())
    assertEquals(1, revision.getOutage().getLat(), .0001)
    assertEquals(2, revision.getOutage().getLon(), .0001)
    assertEquals(2, revision.getNumOutages())
    assertEquals(2, revision.getRun().getAsof().getTime())
  }
  @Test
  def testParsePendingEstimatedRestorationOutageCluster() = {
    val run = new ParserRun(new Timestamp(1), new Timestamp(2));
    val revision: OutageClusterRevision = PepcoScraper.parseOutage((XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/outages_cluster_pending_restoration.xml")) \\ "item")(0), run) match { case r: OutageClusterRevision => r case _ => throw new Exception("Wrong type") }
    assertEquals(10, revision.getNumCustomersAffected())
    assertEquals("Jan 1, 1:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getOutage().getEarliestReport().getTime())))
    //The year should always be the current year as Pepco does not send back the year of the outage.
    assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear())
    assertEquals(1, revision.getOutage().getLat(), .0001)
    assertEquals(2, revision.getOutage().getLon(), .0001)
    assertEquals(2, revision.getNumOutages())
    assertEquals(2, revision.getRun().getAsof().getTime())
    assertNull(revision.getEstimatedRestoration());
  }
  @Test
  def testParsePendingRestorationOutage() = {
    val run = new ParserRun(new Timestamp(1), new Timestamp(2))
    val revision: OutageRevision = PepcoScraper.parseOutage((XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/outages_single_pending_restoration.xml")) \\ "item")(0), run) match { case r: OutageRevision => r case _ => throw new Exception("Wrong type") }
    assertEquals(10, revision.getNumCustomersAffected())
    assertEquals("Jan 1, 1:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getOutage().getEarliestReport().getTime())))
    assertEquals(new DateTime().getYear(),new DateTime(revision.getOutage().getEarliestReport().getTime()).getYear())
    assertEquals(2, revision.getOutage().getLat(), .0001)
    assertEquals(3, revision.getOutage().getLon(), .0001)
    assertEquals("Under Evaluation", revision.getCause())
    assertEquals(CrewStatus.PENDING, revision.getStatus())
    assertEquals(2, revision.getRun().getAsof().getTime())
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
    PepcoScraper.scrapeAllOutages(new PointDouble(0, 0), 8, now, loader, run, outageDao, new HashSet[String](), new HashSet[Integer](), None);
    (indicesZoom8 ::: indicesZoom9).foreach(x=>verify(loader).loadXMLRequest(PepcoScraper.dataHTMLPrefix+"outages/"+now+"/"+x+".xml"))
  }

  @Test
  def testYearChangeParseDateTime() = {
    val jan1=new DateTime(2012, 1, 1, 0, 0);
    val dec31=new DateTime(2011, 12, 31, 0, 0);
    val jan2=new DateTime(2012, 1, 2, 0, 0);
    assertEquals(jan1.getYear(), PepcoScraper.parsePepcoDateTime("Jan 1, 12:00 AM", jan1).getYear());
    assertEquals(jan1.getYear() - 1, PepcoScraper.parsePepcoDateTime("Dec 31, 12:00 AM", jan1).getYear());
    assertEquals(jan2.getYear(), PepcoScraper.parsePepcoDateTime("Dec 31, 12:00 AM", jan2).getYear());
    assertEquals(dec31.getYear(), PepcoScraper.parsePepcoDateTime("Dec 31, 12:00 AM", dec31).getYear());
    assertEquals(dec31.getYear(), PepcoScraper.parsePepcoDateTime("Jan 1, 12:00 AM", dec31).getYear());
  }
}