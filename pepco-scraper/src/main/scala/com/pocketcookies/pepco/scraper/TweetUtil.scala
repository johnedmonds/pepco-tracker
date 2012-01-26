import com.pocketcookies.pepco.model.OutageRevision
import java.util.Properties
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken

object TweetUtil {
  val twitterPropertiesStream = getClass.getClassLoader.getResourceAsStream("twitter.properties")
  val twitter = if (twitterPropertiesStream != null){
    val twitterProperties = new Properties();
    twitterProperties.load(twitterPropertiesStream);
    val twitter = new TwitterFactory().getInstance();
    twitter.setOAuthConsumer(twitterProperties.getProperty("key"), twitterProperties.getProperty("secret"));
    twitter.setOAuthAccessToken(new AccessToken(twitterProperties.getProperty("access"), twitterProperties.getProperty("access_secret")));
    Some(twitter)
  } else { None }
  
  
  def getTweetText(or:OutageRevision):String = {
    val outageURL = "http://pepcotracker.com/outages/"+or.getOutage.getId;
    if (or.getOutage.getRevisions.size>1) {
      "Outage updated: " + outageURL
    } else {
      "New outage: " + outageURL
    }
  }
  
  def tweet(or:OutageRevision) = {
    twitter match {
      case Some(twitter)=> twitter.updateStatus(getTweetText(or))
      case None=>{}
    }
  }
}