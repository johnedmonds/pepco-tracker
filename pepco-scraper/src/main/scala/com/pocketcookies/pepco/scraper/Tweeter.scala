package com.pocketcookies.pepco.scraper;

import com.pocketcookies.pepco.model.AbstractOutageRevision
import java.util.Properties
import org.joda.time.DateTime
import scala.actors.Actor
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

class Tweeter extends Actor {
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
  
  
  def getTweetText(or:AbstractOutageRevision):String = {
    val outageURL = "http://pepcotracker.com/outages/"+or.getOutage.getId;
    if (or.getOutage.getRevisions.size>1) {
      "Outage updated as of " + PepcoScraper.getPepcoDateFormat.print(new DateTime(or.getRun.getAsof.getTime)) + ": " + outageURL
    } else {
      "New outage: " + outageURL
    }
  }
  
  def act() = {
    loop{
      react{
        case Some(or:AbstractOutageRevision) => {
          twitter match {
            case Some(twitter)=> twitter.updateStatus(getTweetText(or))
            case None=>{}
          }
        }
        case None => exit()
      }
    }
    
  }
}