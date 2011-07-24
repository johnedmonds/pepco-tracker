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
	val run=new ParserRun(new Timestamp(1))
	val areas:Seq[OutageAreaRevision]=(XML.load(PepcoScraper.getClass().getResourceAsStream("/testxml/thematic.xml")) \\ "item")
	.map(n=>PepcoScraper.parseThematicArea(n,run))
	assertEquals(2,areas.size)
	assertEquals("00000,00001",areas(0).getArea().getId())
	assertEquals(10, areas(0).getCustomersOut())
	assertEquals("00002",areas(1).getArea().getId())
	assertEquals(20, areas(1).getCustomersOut())
	assertEquals(run, areas(0).getParserRun())
	assertEquals(run, areas(1).getParserRun())
  }

}
