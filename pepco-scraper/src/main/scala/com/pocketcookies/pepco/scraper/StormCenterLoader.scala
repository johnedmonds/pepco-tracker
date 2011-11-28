package com.pocketcookies.pepco.scraper

import org.apache.http.client.HttpClient
import scala.xml.Elem
import org.apache.http.client.methods.HttpGet
import org.joda.time.DateTime
import org.apache.http.HttpResponse
import scala.xml.XML
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger

class StormCenterLoader (client:HttpClient){
  val logger: Logger = Logger.getLogger("PepcoScraper")
  /**
   * Makes a request and parses the response as XML.  If the request fails for some reason, returns null.
   */
  def loadXMLRequest(getPath: String): Elem = {
    val get: HttpGet = new HttpGet(getPath)
    get.getParams().setLongParameter("timestamp", new DateTime().getMillis())
    val response: HttpResponse = client.execute(get)
    try {
      XML.load(response.getEntity().getContent())
    } catch { case e: Exception => { logger.warn("Error while downloading " + getPath, e); null } }
    finally { EntityUtils.consume(response.getEntity()) }
  }
}