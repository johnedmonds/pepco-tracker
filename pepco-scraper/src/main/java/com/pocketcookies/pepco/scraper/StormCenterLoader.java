package com.pocketcookies.pepco.scraper;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

class StormCenterLoader {
    private HttpClient client;

    public StormCenterLoader(final HttpClient client) {
        this.client = client;
    }

    private static final Logger logger = Logger.getLogger("PepcoScraper");

    /**
     * Makes a request and parses the response as XML. If the request fails for
     * some reason, returns null.
     * 
     * @throws IOException
     * @throws ClientProtocolException
     */
    public Document loadXMLRequest(final String getPath)
            throws ClientProtocolException, IOException {
        final HttpGet get = new HttpGet(getPath);
        get.getParams().setLongParameter("timestamp",
                new DateTime().getMillis());
        final HttpResponse response = client.execute(get);
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(response.getEntity().getContent());
        } catch (Exception e) {
            logger.warn("Error while downloading " + getPath, e);
            return null;
        } finally {
            EntityUtils.consume(response.getEntity());
        }
    }
}