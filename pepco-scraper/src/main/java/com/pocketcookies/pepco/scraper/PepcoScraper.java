package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.pocketcookies.pepco.model.ParserRun;

/**
 * Scrapes Pepco's outages.  This is the main class (with the main method) that calls all the other scrapers.
 * 
 * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
 */
@Service
public class PepcoScraper {
    public static final String DATA_HTML_PREFIX = "http://www.pepco.com/home/emergency/maps/stormcenter/data/";
    public static final String DIRECTORY_SUFFIX = "/outages/metadata.xml";
    private static final Logger logger = Logger.getLogger("PepcoScraper");
    private final OutageScraper outageScraper;
    private final SummaryScraper summaryScraper;
    private final OutageAreaScraper outageAreaScraper;

    @Inject
    public PepcoScraper(OutageScraper outageScraper,
            SummaryScraper summaryScraper, OutageAreaScraper outageAreaScraper) {
        this.outageScraper = outageScraper;
        this.summaryScraper = summaryScraper;
        this.outageAreaScraper = outageAreaScraper;
    }

    private void scrape() throws IOException, InterruptedException {
        final StormCenterLoader client = new StormCenterLoader(
                new DefaultHttpClient(new PoolingClientConnectionManager()));
        final String outagesFolderName = PepcoUtil.getTextFromOnlyElement(
                client.loadXMLRequest(DATA_HTML_PREFIX + DIRECTORY_SUFFIX),
                "directory");
        final Timestamp observationDate = new Timestamp(DateTimeFormat
                .forPattern("yyyy_MM_dd_HH_mm_ss")
                .parseDateTime(outagesFolderName).getMillis());
        final ParserRun run = new ParserRun(new Timestamp(
                new DateTime().getMillis()), observationDate);

        ExecutorService scraperExecutor = Executors.newFixedThreadPool(3);
        for (final Scraper scraper : ImmutableList.<Scraper> of(summaryScraper,
                outageScraper, outageAreaScraper)) {
            scraperExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        scraper.scrape(run);
                    } catch (Exception e) {
                        logger.error(e);
                    }
                }
            });
        }
        scraperExecutor.shutdown();
        scraperExecutor.awaitTermination(10, TimeUnit.MINUTES);
    }

    public static void main(final String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] { "spring-config.xml" });
        PepcoScraper scraper = context.getBean(PepcoScraper.class);
        try {
            scraper.scrape();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}