package com.pocketcookies.pepco.scraper.test

import org.junit._
import Assert._
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

@Test
class ScraperTest {

  @Test
  def testParseSummary() = {
    val run = new ParserRun(new Timestamp(1));
    val s: Summary = PepcoScraper.parseSummary(XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/summary.xml")), run)
    assertEquals(4, s.getTotalOutages())
    assertEquals(1, s.getDcAffectedCustomers())
    assertEquals(10, s.getDcTotalCustomers())
    assertEquals(2, s.getPgAffectedCustomers())
    assertEquals(20, s.getPgTotalCustomers())
    assertEquals(3, s.getMontAffectedCustomers())
    assertEquals(30, s.getMontTotalCustomers())
    assertEquals(run, s.getRun())
  }
  @Test
  def testParseArea() = {
    val run = new ParserRun(new Timestamp(1))
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
    val run = new ParserRun(new Timestamp(1))
    val revision: OutageRevision = PepcoScraper.parseOutage((XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/outages_single.xml")) \\ "item")(0), run) match { case r: OutageRevision => r case _ => throw new Exception("Wrong type") }
    assertEquals(10, revision.getNumCustomersAffected())
    assertEquals("Jan 1, 1:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getOutage().getEarliestReport().getTime())))
    assertEquals("Jan 1, 2:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getEstimatedRestoration().getTime())))
    assertEquals(2, revision.getOutage().getLat(), .0001)
    assertEquals(3, revision.getOutage().getLon(), .0001)
    assertEquals("Under Evaluation", revision.getCause())
    assertEquals(CrewStatus.PENDING, revision.getStatus())
  }
  @Test
  def testParseOutageCluster() = {
    val run = new ParserRun(new Timestamp(1))
    val revision: OutageClusterRevision = PepcoScraper.parseOutage((XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/outages_cluster.xml")) \\ "item")(0), run) match { case r: OutageClusterRevision => r case _ => throw new Exception("Wrong type") }
    assertEquals(10, revision.getNumCustomersAffected())
    assertEquals("Jan 1, 1:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getOutage().getEarliestReport().getTime())))
    assertEquals("Jan 1, 2:00 PM", PepcoScraper.getPepcoDateFormat().print(new DateTime(revision.getEstimatedRestoration().getTime())))
    assertEquals(1, revision.getOutage().getLat(), .0001)
    assertEquals(2, revision.getOutage().getLon(), .0001)
    assertEquals(2, revision.getNumOutages())
  }
}
