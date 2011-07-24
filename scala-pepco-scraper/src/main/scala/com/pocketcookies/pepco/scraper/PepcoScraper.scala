package com.pocketcookies.pepco.scraper

import com.pocketcookies.pepco.model.dao.SummaryDAO
import com.pocketcookies.pepco.model.dao.OutageAreaDAO
import org.apache.http.client.methods.HttpGet
import org.apache.http.params.HttpParams
import org.joda.time.DateTime
import scala.xml.XML
import org.apache.http.client.HttpClient
import scala.collection.mutable.LinkedList
import com.pocketcookies.pepco.model.Summary
import org.apache.log4j.Logger
import org.apache.http.HttpResponse
import org.apache.http.util.EntityUtils
import scala.xml.Elem
import scala.xml.NodeSeq
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import com.pocketcookies.pepco.model.ParserRun
import java.sql.Timestamp
import com.pocketcookies.pepco.model.Outage
import org.jsoup.Jsoup
import com.pocketcookies.pepco.model.OutageClusterRevision
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus
import com.pocketcookies.pepco.model.OutageRevision
import com.pocketcookies.pepco.model.dao.OutageDAO
import com.pocketcookies.pepco.model.OutageAreaRevision
import com.pocketcookies.pepco.model.OutageArea
import scala.xml.Node
import com.pocketcookies.pepco.model.AbstractOutageRevision
import org.apache.http.impl.client.DefaultHttpClient
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import scala.collection.mutable.HashSet
import org.jsoup.nodes.TextNode

object PepcoScraper {
  val dataHTMLPrefix: String = "http://www.pepco.com/home/emergency/maps/stormcenter/data/";
  val thematicSuffix: String = "thematic/current/thematic_areas.xml";
  val summarySuffix: String = "data.xml";
  val directorySuffix: String = "/outages/metadata.xml"
  val maxZoom: Int = 15;
  val logger: Logger = Logger.getLogger("PepcoScraper")
  def getPepcoDateFormat(): DateTimeFormatter = {
    return DateTimeFormat.forPattern("MMM d, h:mm a");
  }
  def getOutagesFolderName(doc: Elem): String = {
    doc \\ "directory" text
  }
  /**
   * Makes a request and parses the response as XML.  If the request fails for some reason, returns null.
   */
  def loadXMLRequest(client: HttpClient, getPath: String): Elem = {
    val get: HttpGet = new HttpGet(getPath)
    get.getParams().setLongParameter("timestamp", new DateTime().getMillis())
    val response: HttpResponse = client.execute(get)
    try {
      XML.load(response.getEntity().getContent())
    } catch { case e: Exception => { logger.warn("Error while downloading " + getPath, e); null } }
    finally { EntityUtils.consume(response.getEntity()) }
  }
  def parseSummary(doc: Elem, run: ParserRun): Summary = {
    val areas: NodeSeq = doc \\ "area"
    val dc = areas(0)
    val pg = areas(1)
    val mont = areas(2)
    val ret = new Summary(
      Integer.parseInt(doc \\ "total_outages" text),
      Integer.parseInt(dc \\ "custs_out" text),
      Integer.parseInt(dc \\ "total_custs" text),
      Integer.parseInt(pg \\ "custs_out" text),
      Integer.parseInt(pg \\ "total_custs" text),
      Integer.parseInt(mont \\ "custs_out" text),
      Integer.parseInt(mont \\ "total_custs" text),
      new Timestamp(getPepcoDateFormat().parseDateTime(doc \\ "date_generated" text).getMillis()),
      run)
    return ret
  }
  /**
   * Parses the given XML document and returns an Outage.  The returned Outage
   * will have an OutageRevision as its only revision.  The Outage and
   * OutageRevision are neither loaded from nor persisted to the database.
   */
  def parseOutage(item: Node, run: ParserRun): AbstractOutageRevision = {
    val doc = Jsoup.parseBodyFragment(item \\ "description" text)
    val sCustomersAffected = doc.select(":containsOwn(Customers Affected)").first().nextSibling() match { case n: TextNode => n.text().trim() case _ => throw new ClassCastException() }
    val customersAffected = if (sCustomersAffected equals "Less than 5") 0 else Integer.parseInt(sCustomersAffected)
    val earliestReport = getPepcoDateFormat().parseDateTime(doc.select(":containsOwn(Report)").first().nextSibling() match { case n: TextNode => n.text().trim() case _ => throw new ClassCastException() })
    val estimatedRestoration = getPepcoDateFormat().parseDateTime(doc.select(":containsOwn(Restoration)").first().nextSibling() match { case n: TextNode => n.text().trim() case _ => throw new ClassCastException() })
    if ((item \\ "point" size) != 0) {
      val latLon = new PointDouble(List.fromString((item \\ "point")(0) text, ' '))
      val numOutages = Integer.parseInt(doc.select(":containsOwn(Number of Outage Orders)").first().nextSibling() match {case n:TextNode=>n.text().trim() case _=>throw new ClassCastException()})
      val outage = new Outage(latLon.lat, latLon.lon, new Timestamp(earliestReport.getMillis()), null)
      val outageRevision = new OutageClusterRevision(customersAffected, new Timestamp(estimatedRestoration.getMillis()), new Timestamp(new DateTime().getMillis()), outage, run, numOutages)
      outage.getRevisions().add(outageRevision)
      outageRevision
    } else {
      val latLon: PointDouble = new Polygon(item \\ "polygon" text).getCenter();
      val cause = doc.select(":containsOwn(Cause)").first().nextSibling() match {case n:TextNode=>n.text().trim() case _=>throw new ClassCastException()}
      val status = CrewStatus.valueOf(doc.select(":containsOwn(Crew Status)").first().nextSibling() match {case n:TextNode=>n.text().trim().replace(' ', '_').toUpperCase() case _=>throw new ClassCastException()})
      val outage = new Outage(latLon.lat, latLon.lon, new Timestamp(earliestReport.getMillis()), null)
      val outageRevision = new OutageRevision(customersAffected, new Timestamp(estimatedRestoration.getMillis()), new Timestamp(new DateTime().getMillis()), outage, run, cause, status)
      outage.getRevisions().add(outageRevision)
      outageRevision
    }
  }
  /**
   * Parses a single thematic area into an OutageAreaRevision.
   */
  def parseThematicArea(area: Node, run: ParserRun): OutageAreaRevision = {
    val sCustomersOut = Jsoup.parseBodyFragment(area \\ "description" text).select(":containsOwn(Customers Affected)").first().nextSibling() match { case n: TextNode => n.text().trim() case _ => throw new ClassCastException() }
    val customersOut = if (sCustomersOut equals "Less than 5") 0 else Integer.parseInt(sCustomersOut)
    new OutageAreaRevision(new OutageArea(area \\ "title" text), customersOut, run)
  }

  def scrape(client: HttpClient, outageDao: OutageDAO, outageAreaDao: OutageAreaDAO, summaryDao: SummaryDAO, outagesFolderName: String, run: ParserRun) = {
    //Parse Summary
    summaryDao.saveSummary(parseSummary(loadXMLRequest(client, dataHTMLPrefix + summarySuffix), run))
    //Parse thematic
    (loadXMLRequest(client, dataHTMLPrefix + thematicSuffix) \\ "item")
      .map(area => parseThematicArea(area, run))
      .foreach(a => outageAreaDao.updateArea(a));

    //Parse outages.
    scrapeAllOutages(new PointDouble(38.96, -77.03), 8, outagesFolderName, client, run, outageDao, new HashSet[String]());
  }
  def scrapeAllOutages(point: PointDouble, zoom: Int, outagesFolderName: String, client: HttpClient, run: ParserRun, outageDao: OutageDAO, visitedIndices: HashSet[String]): Unit = {
    if (zoom > maxZoom) return ;
    val indices = PepcoUtil.getSpatialIndicesForPoint(point.lat, point.lon, zoom)
      .filter(index => !visitedIndices.contains(index))
    indices.foreach(index => {visitedIndices.add(index); logger.debug("scraping "+index)})

    val outages = indices
      .map(id => dataHTMLPrefix + "outages/" + outagesFolderName + "/" + id + ".xml") //Convert to Get requests.
      .map(url => loadXMLRequest(client, url))
      .filter(a => a != null) //Remove failed requests.
      .map(el => el \\ "item") //Each request returns a list of outages as xml.  Here we turn the XML list into a Scala list.
      .flatten //No need to parse each list individually.
      .map(n => parseOutage(n, run))
    outages.foreach(outageRevision => outageDao.updateOutage(outageRevision))
    //We only want to zoom in on clusters as there may be more information at the next zoom level.
    outages.filter(outageRevision => outageRevision match {
      case r: OutageClusterRevision => true
      case _ => false
    })
      //Recurse.
      .foreach(outageRevision => scrapeAllOutages(new PointDouble(outageRevision.getOutage().getLat(), outageRevision.getOutage().getLon()), zoom + 1, outagesFolderName, client, run, outageDao, visitedIndices))
  }

  def main(args: Array[String]): Unit = {
    val client: HttpClient = new DefaultHttpClient();
    val outagesFolderName = loadXMLRequest(client, dataHTMLPrefix + directorySuffix) \\ "directory" text
    val run: ParserRun = new ParserRun(new Timestamp(DateTimeFormat
      .forPattern("yyyy_MM_dd_HH_mm_ss")
      .parseDateTime(outagesFolderName).getMillis()))
    val sessionFactory: SessionFactory = new Configuration().configure().configure("hibernate.ds.cfg.xml").buildSessionFactory()
    sessionFactory.getCurrentSession().beginTransaction()
    sessionFactory.getCurrentSession().save(run)
    scrape(client, new OutageDAO(sessionFactory), new OutageAreaDAO(sessionFactory), new SummaryDAO(sessionFactory), outagesFolderName, run)
    sessionFactory.getCurrentSession().getTransaction().commit()
    sessionFactory.getCurrentSession().close()
    sessionFactory.close()
  }
}