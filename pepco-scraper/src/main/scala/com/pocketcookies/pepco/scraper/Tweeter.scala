package com.pocketcookies.pepco.scraper;

import com.pocketcookies.pepco.model.dao.ParserRunSummary
import java.net.URLEncoder
import java.util.Properties
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

class Tweeter {
  val twitterPropertiesStream = getClass.getClassLoader.getResourceAsStream("twitter.properties")
  val twitter = if (twitterPropertiesStream != null){
    val twitterProperties = new Properties();
    twitterProperties.load(twitterPropertiesStream);
    if (java.lang.Boolean.parseBoolean(twitterProperties.getProperty("enabled"))) {
      val twitter = new TwitterFactory().getInstance();
      twitter.setOAuthConsumer(twitterProperties.getProperty("key"), twitterProperties.getProperty("secret"));
      twitter.setOAuthAccessToken(new AccessToken(twitterProperties.getProperty("access"), twitterProperties.getProperty("access_secret")));
      Some(twitter)
    } else {
      None
    }
  } else { None }
  
  def tweetSummary(summary:ParserRunSummary) = {
    twitter match {
      case Some(t)=>{
          val format=DateTimeFormat.forPattern("yyyy-MM-dd hh:mm:ss a");
          val urlFormat=DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
          val runTime = new DateTime(summary.run.getRunTime.getTime());
          t.updateStatus("At %s we found that #pepco experienced %d new outages, posted updates to %d existing (still ongoing) outages, and fixed %d outages.  Get more details by visiting http://pepcotracker.com/%s".format(
              format.print(runTime), summary.newOutages, summary.updatedOutages, summary.closedOutages, URLEncoder.encode(urlFormat.print(runTime), "UTF-8")))
        }
      case _=>{}
    }
  }
  
}