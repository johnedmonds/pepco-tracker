package com.pocketcookies.pepco.scraper;

import com.pocketcookies.pepco.model.dao.ParserRunSummary
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.util.Properties
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import org.json.JSONTokener
import twitter4j.StatusUpdate
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

object Tweeter {
  val twitterPropertiesStream = getClass.getClassLoader.getResourceAsStream("twitter.properties")
  val twitterProperties = new Properties();
  val twitter = if (twitterPropertiesStream != null){
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
          val shortenedURL=new JSONObject(new JSONTokener(new InputStreamReader(new URL(
            "https://api-ssl.bitly.com/v3/shorten?login=%s&apiKey=%s&longUrl=http://pepcotracker.com/outages?asof=%s"
            .format(twitterProperties.getProperty("bit.ly.login"),
                    twitterProperties.getProperty("bit.ly.apikey"),
                    URLEncoder.encode(urlFormat.print(runTime), "UTF-8")))
            .openStream))).getJSONObject("data").getString("url")
          val status=new StatusUpdate("#pepco experienced %d new outage(s), updated %d ongoing outage(s), and fixed %d outage(s). Details available at %s.".format(
              summary.newOutages, summary.updatedOutages, summary.closedOutages, shortenedURL))
          t.updateStatus(status)
        }
      case _=>{}
    }
  }
  
}