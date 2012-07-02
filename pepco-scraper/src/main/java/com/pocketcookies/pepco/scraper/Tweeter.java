package com.pocketcookies.pepco.scraper;

import com.pocketcookies.pepco.model.dao.ParserRunSummary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Properties;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

public class Tweeter {
    private final Twitter twitter;
    private final Properties twitterProperties = new Properties();

    public Tweeter() throws IOException {
        final InputStream twitterPropertiesStream = getClass().getClassLoader()
                .getResourceAsStream("twitter.properties");
        if (twitterPropertiesStream != null) {
            twitterProperties.load(twitterPropertiesStream);
            if (Boolean.parseBoolean(twitterProperties.getProperty("enabled"))) {
                twitter = new TwitterFactory().getInstance();
                twitter.setOAuthConsumer(twitterProperties.getProperty("key"),
                        twitterProperties.getProperty("secret"));
                twitter.setOAuthAccessToken(new AccessToken(twitterProperties
                        .getProperty("access"), twitterProperties
                        .getProperty("access_secret")));
            } else {
                twitter = null;
            }
        } else {
            twitter = null;
        }
    }

    public void tweetSummary(ParserRunSummary summary)
            throws MalformedURLException, UnsupportedEncodingException,
            JSONException, IOException, TwitterException {
        if (twitter == null) {
            return;
        }
        final DateTimeFormatter urlFormat = DateTimeFormat
                .forPattern("yyyy-MM-dd HH:mm:ss");
        final DateTime runTime = new DateTime(summary.run.getRunTime()
                .getTime());
        final String shortenedURL = new JSONObject(
                new JSONTokener(
                        new InputStreamReader(
                                new URL(
                                        String.format(
                                                "https://api-ssl.bitly.com/v3/shorten?login=%s&apiKey=%s&longUrl=http://pepcotracker.com/outages?asof=%s",
                                                twitterProperties
                                                        .getProperty("bit.ly.login"),
                                                twitterProperties
                                                        .getProperty("bit.ly.apikey"),
                                                URLEncoder.encode(urlFormat
                                                        .print(runTime),
                                                        "UTF-8"))).openStream())))
                .getJSONObject("data").getString("url");

        final StatusUpdate status = new StatusUpdate(
                String.format(
                        "#pepco experienced %d new outage(s), updated %d ongoing outage(s), and fixed %d outage(s). Details available at %s.",
                        summary.newOutages, summary.updatedOutages,
                        summary.closedOutages, shortenedURL));
        twitter.updateStatus(status);
    }

}