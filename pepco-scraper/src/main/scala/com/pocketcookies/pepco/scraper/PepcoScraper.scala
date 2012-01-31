package com.pocketcookies.pepco.scraper

import com.pocketcookies.pepco.model.dao.SummaryDAO
import com.pocketcookies.pepco.model.dao.OutageAreaDAO
import org.joda.time.DateTime
import com.pocketcookies.pepco.model.Summary
import org.apache.log4j.Logger
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
import org.hibernate.cfg.AnnotationConfiguration
import scala.collection.mutable.HashSet
import org.jsoup.nodes.TextNode
import collection.JavaConversions._

object PepcoScraper {
  val dataHTMLPrefix: String = "http://www.pepco.com/home/emergency/maps/stormcenter/data/";
  val thematicSuffix: String = "thematic/current/thematic_areas.xml";
  val summarySuffix: String = "data.xml";
  val directorySuffix: String = "/outages/metadata.xml"
  val maxZoom: Int = 15;
  val logger: Logger = Logger.getLogger("PepcoScraper")
  def getPepcoDateFormat():DateTimeFormatter = {DateTimeFormat.forPattern("MMM d, h:mm a")}
  /**
   * Parses the string representing the date/time from Pepco.
   * @param date The date/time string to parse.
   */
  def parsePepcoDateTime(date:String):DateTime = {parsePepcoDateTime(date, new DateTime())}
  /**
   * Parses the string representing the date/time from Pepco.
   * This is mainly used to allow unit testing.  It's rather difficult to set
   * the current time just for unit testing.
   * @param date The date/time string to parse.
   * @param now Should represent now, as in the current moment in time.
   *   For unit testing, we will set this so our unit tests don't only work on Jan 1.
   */
  def parsePepcoDateTime(date:String, now:DateTime):DateTime = {
    val ret = getPepcoDateFormat().parseDateTime(date);
    /*
     * Check whether it is January and we are parsing a date in December.
     * 
     * Pepco's dates don't include the year.  So at the turn of the year
     * (Jan 1), we'll probably read something like "Dec 31 12:58 PM".
     * It is very unlikely that this actually represents December 31 of this
     * year.  More likely, this is December 31 of last year (either that or
     * some poor group of customers is going to have to wait more than a year
     * for their power to get fixed).
     */
    if (now.getMonthOfYear() == 1 && now.getDayOfMonth() == 1 && ret.getMonthOfYear() == 12 && ret.getDayOfMonth() == 31)
      ret.withYear(now.getYear() - 1)
    else
      ret.withYear(now.getYear())
  }
      
  def getOutagesFolderName(doc: Elem): String = {
    doc \\ "directory" text
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
      new Timestamp(parsePepcoDateTime(doc \\ "date_generated" text).getMillis()),
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
    val earliestReport = parsePepcoDateTime(doc.select(":containsOwn(Report)").first().nextSibling() match { case n: TextNode => n.text().trim() case _ => throw new ClassCastException() })
    val estimatedRestoration = try {
      val sEstimatedRestoration = doc.select(":containsOwn(Restoration)").first().nextSibling() match { case n: TextNode => n.text().trim() case _ => throw new ClassCastException() } 
      if (sEstimatedRestoration equals "Pending") null
      else new Timestamp(parsePepcoDateTime(sEstimatedRestoration).getMillis())
    } catch { case e:IllegalArgumentException => {logger.warn("Error parsing estimated restoration.", e); null}}
    if ((item \\ "point" size) != 0) {
      val latLon = new PointDouble(List.fromString((item \\ "point")(0) text, ' '))
      val numOutages = Integer.parseInt(doc.select(":containsOwn(Number of Outage Orders)").first().nextSibling() match {case n:TextNode=>n.text().trim() case _=>throw new ClassCastException()})
      val outage = new Outage(latLon.lat, latLon.lon, new Timestamp(earliestReport.getMillis()), null)
      val outageRevision = new OutageClusterRevision(customersAffected, estimatedRestoration, outage, run, numOutages)
      outage.getRevisions().add(outageRevision)
      outageRevision
    } else {
      val latLon: PointDouble = new Polygon(item \\ "polygon" text).getCenter();
      val cause = doc.select(":containsOwn(Cause)").first().nextSibling() match {case n:TextNode=>n.text().trim() case _=>throw new ClassCastException()}
      val status = CrewStatus.valueOf(doc.select(":containsOwn(Crew Status)").first().nextSibling() match {case n:TextNode=>n.text().trim().replace(' ', '_').toUpperCase() case _=>throw new ClassCastException()})
      val outage = new Outage(latLon.lat, latLon.lon, new Timestamp(earliestReport.getMillis()), null)
      val outageRevision = new OutageRevision(customersAffected, estimatedRestoration, outage, run, cause, status)
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

  /**
   * Basically the "hub" function that calls all the scraping functions.
   * Also closes out all outages which appear resolved.
   */
  def scrape(client: StormCenterLoader, outageDao: OutageDAO, outageAreaDao: OutageAreaDAO, summaryDao: SummaryDAO, outagesFolderName: String, run: ParserRun) = {
    //Parse Summary
    summaryDao.saveSummary(parseSummary(client.loadXMLRequest(dataHTMLPrefix + summarySuffix), run))
    //Parse thematic
    (client.loadXMLRequest(dataHTMLPrefix + thematicSuffix) \\ "item")
      .map(area => parseThematicArea(area, run))
      .foreach(a => outageAreaDao.updateArea(a));

    //Parse outages.
    val outageIds=new HashSet[Integer]();
    scrapeAllOutages(new PointDouble(38.96, -77.03), 8, outagesFolderName, client, run, outageDao, new HashSet[String](), outageIds);
    val temp:java.util.Collection[Integer]=outageIds
    outageDao.closeMissingOutages(temp, run.getRunTime());
  }
  def scrapeAllOutages(point: PointDouble, zoom: Int, outagesFolderName: String, client: StormCenterLoader, run: ParserRun, outageDao: OutageDAO, visitedIndices: HashSet[String], outageIds:HashSet[Integer]): Unit = {
    if (zoom > maxZoom) return ;
    val indices = PepcoUtil.getSpatialIndicesForPoint(point.lat, point.lon, zoom)
      .filter(index => !visitedIndices.contains(index))
    indices.foreach(index => {visitedIndices.add(index); logger.debug("scraping "+index)})

    val outages = indices
      .map(id => dataHTMLPrefix + "outages/" + outagesFolderName + "/" + id + ".xml") //Convert to Get requests.
      .map(url => client.loadXMLRequest(url))
      .filter(a => a != null) //Remove failed requests.  Usually requests can fail because they were made for data outside of Pepco's service area.
      .map(el => el \\ "item") //Each request returns a list of outages as xml.  Here we turn the XML list into a Scala list.
      .flatten //No need to parse each list individually.
      .map(n => parseOutage(n, run))
    //Add the current zoom level to all the outages.
    outages.foreach(outageRevision => {outageRevision.getOutage().getZoomLevels.add(zoom)})
    outages.foreach(outageRevision => {
        outageDao.updateOutage(outageRevision);
        outageIds.add(outageRevision.getOutage().getId());
    })
    //We only want to zoom in on clusters as there may be more information at the next zoom level.
    outages.filter(outageRevision => outageRevision match {
      case r: OutageClusterRevision => true
      case _ => false
    })
      //Recurse.
      .foreach(outageRevision => scrapeAllOutages(new PointDouble(outageRevision.getOutage().getLat(), outageRevision.getOutage().getLon()), zoom + 1, outagesFolderName, client, run, outageDao, visitedIndices, outageIds))
  }

  def main(args: Array[String]): Unit = {
    val client = new StormCenterLoader(new DefaultHttpClient());
    val outagesFolderName = client.loadXMLRequest(dataHTMLPrefix + directorySuffix) \\ "directory" text;
    val observationDate=new Timestamp(DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss").parseDateTime(outagesFolderName).getMillis());
    val run: ParserRun = new ParserRun(new Timestamp(new DateTime().getMillis()), observationDate)
    val sessionFactory: SessionFactory = new AnnotationConfiguration().configure("hibernate-mappings.cfg.xml").configure("hibernate.ds.cfg.xml").buildSessionFactory()
    sessionFactory.getCurrentSession().beginTransaction()
    sessionFactory.getCurrentSession().save(run)
    val outageDao = new OutageDAO(sessionFactory);
    scrape(client, outageDao, new OutageAreaDAO(sessionFactory), new SummaryDAO(sessionFactory), outagesFolderName, run)
    sessionFactory.getCurrentSession().flush();
    Tweeter.tweetSummary(outageDao.getParserRunSummary(run));
    sessionFactory.getCurrentSession().getTransaction().commit()
    sessionFactory.getCurrentSession().close()
    sessionFactory.close()
  }
}