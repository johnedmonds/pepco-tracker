package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.ParserRun;
import com.pocketcookies.pepco.model.dao.OutageDAO;

public class OutageScraper implements Scraper {
    private static final Logger logger = Logger.getLogger(OutageScraper.class);
    private static final PointDouble DEFAULT_STARTING_POINT = new PointDouble(
            38.96, -77.03);
    private static final int STARTING_ZOOM = 8;
    private static final int MAX_ZOOM = 15;
    private static final Function<AbstractOutageRevision, Integer> REVISION_TO_ID = new Function<AbstractOutageRevision, Integer>() {
        @Override
        public Integer apply(AbstractOutageRevision input) {
            return input.getId();
        }
    };

    private final OutageDAO dao;
    private final StormCenterLoader stormCenterLoader;
    private final String outagesFolderName;
    private final PointDouble startingPoint;

    public OutageScraper(final OutageDAO dao,
            final StormCenterLoader stormCenterLoader) throws IOException {
        this(dao, stormCenterLoader, PepcoUtil.getTextFromOnlyElement(
                stormCenterLoader.loadXMLRequest(PepcoScraper.DATA_HTML_PREFIX
                        + PepcoScraper.DIRECTORY_SUFFIX), "directory"),
                DEFAULT_STARTING_POINT);
    }

    OutageScraper(final OutageDAO dao,
            final StormCenterLoader stormCenterLoader,
            final String outagesFolderName, final PointDouble startingPoint) {
        this.stormCenterLoader = stormCenterLoader;
        this.dao = dao;
        this.outagesFolderName = outagesFolderName;
        this.startingPoint = startingPoint;
    }

    private static Timestamp parseEstimatedRestoration(
            String sEstimatedRestoration) {
        try {
            if (sEstimatedRestoration.equals("Pending"))
                return null;
            else {
                return new Timestamp(PepcoUtil.parsePepcoDateTime(
                        sEstimatedRestoration).getMillis());
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Error parsing estimated restoration.", e);
            return null;
        }
    }

    static AbstractOutageRevision parseOutage(final Node item,
            final ParserRun run) {
        final Document doc = Jsoup.parseBodyFragment(PepcoUtil
                .getTextFromOnlyElement(item, "description"));
        final String sCustomersAffected = ((TextNode) doc
                .select(":containsOwn(Customers Affected)").first()
                .nextSibling()).text().trim();
        final int customersAffected = sCustomersAffected.equals("Less than 5") ? 0
                : Integer.parseInt(sCustomersAffected);
        final DateTime earliestReport = PepcoUtil
                .parsePepcoDateTime(((TextNode) doc
                        .select(":containsOwn(Report)").first().nextSibling())
                        .text().trim());
        final Timestamp estimatedRestoration = parseEstimatedRestoration(((TextNode) doc
                .select(":containsOwn(Restoration)").first().nextSibling())
                .text().trim());
        if (((Element) item).getElementsByTagName("georss:point").getLength() != 0) {
            final PointDouble latLon = new PointDouble(
                    PepcoUtil.getTextFromOnlyElement(item, "georss:point"));
            final int numOutages = Integer.parseInt(((TextNode) doc
                    .select(":containsOwn(Number of Outage Orders)").first()
                    .nextSibling()).text().trim());
            final Outage outage = new Outage(latLon.lat, latLon.lon,
                    new Timestamp(earliestReport.getMillis()), null);
            final OutageClusterRevision outageRevision = new OutageClusterRevision(
                    customersAffected, estimatedRestoration, outage, run,
                    numOutages);
            outage.getRevisions().add(outageRevision);
            return outageRevision;
        } else {
            final PointDouble latLon = new Polygon(
                    PepcoUtil.getTextFromOnlyElement(item, "georss:polygon"))
                    .getCenter();
            final String cause = ((TextNode) doc.select(":containsOwn(Cause)")
                    .first().nextSibling()).text().trim();
            final CrewStatus status = CrewStatus.valueOf(((TextNode) doc
                    .select(":containsOwn(Crew Status)").first().nextSibling())
                    .text().trim().replace(' ', '_').toUpperCase());
            final Outage outage = new Outage(latLon.lat, latLon.lon,
                    new Timestamp(earliestReport.getMillis()), null);
            final OutageRevision outageRevision = new OutageRevision(
                    customersAffected, estimatedRestoration, outage, run,
                    cause, status);
            outage.getRevisions().add(outageRevision);
            return outageRevision;
        }
    }

    static Collection<AbstractOutageRevision> parseOutages(
            final NodeList items, final ParserRun run) {
        final ImmutableList.Builder<AbstractOutageRevision> builder = ImmutableList
                .builder();
        for (int i = 0; i < items.getLength(); i++) {
            builder.add(parseOutage(items.item(i), run));
        }
        return builder.build();
    }

    /**
     * Downloads and parses outages from Pepco. Note that the downloads happen
     * in parallel.
     */
    Set<AbstractOutageRevision> downloadOutages(final ParserRun run)
            throws InterruptedException, ExecutionException {
        /**
         * How many sections we're currently downloading. We use this to know
         * when there's nothing more to download. At this point, we can shutdown
         * the download executor. Essentially, this variable tells us how many
         * "paths" we're exploring.
         */
        final AtomicInteger downloadsInProgress = new AtomicInteger(0);
        final ExecutorService downloadExecutor = Executors
                .newFixedThreadPool(10);
        final Collection<Future<Collection<AbstractOutageRevision>>> revisionFutures = new LinkedList<Future<Collection<AbstractOutageRevision>>>();
        final Set<String> visitedIndices = Collections
                .synchronizedSet(new HashSet<String>());
        /**
         * Downloads and parses a list of outages. If an outage cluster exists
         * 
         * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
         */
        class Downloader implements
                Callable<Collection<AbstractOutageRevision>> {
            private final String sectionIdBeingDownloaded;
            private final int zoom;

            public Downloader(final String sectionIdBeingDownloaded,
                    final int zoom) {
                this.sectionIdBeingDownloaded = sectionIdBeingDownloaded;
                this.zoom = zoom;
            }

            @Override
            public Collection<AbstractOutageRevision> call() throws Exception {
                try {
                    downloadsInProgress.incrementAndGet();
                    if (zoom > MAX_ZOOM
                            || visitedIndices
                                    .contains(sectionIdBeingDownloaded)) {
                        return ImmutableList.<AbstractOutageRevision> of();
                    }
                    visitedIndices.add(sectionIdBeingDownloaded);
                    final org.w3c.dom.Document doc = stormCenterLoader
                            .loadXMLRequest(PepcoScraper.DATA_HTML_PREFIX
                                    + "outages/" + outagesFolderName + "/"
                                    + sectionIdBeingDownloaded + ".xml");
                    if (doc == null) {
                        return ImmutableList.<AbstractOutageRevision> of();
                    }
                    final Collection<AbstractOutageRevision> builtRevisions = parseOutages(
                            doc.getElementsByTagName("item"), run);
                    for (AbstractOutageRevision revision : builtRevisions) {
                        // We only need to zoom in if there's a cluster.
                        // Otherwise, zooming in will give us no more useful
                        // information.
                        if (revision instanceof OutageClusterRevision) {
                            for (String index : PepcoUtil
                                    .getSpatialIndicesForPoint(revision
                                            .getOutage().getLat(), revision
                                            .getOutage().getLon(), zoom + 1)) {
                                // Recurse. Basically, submit a download task
                                // for the next highest zoomlevel.
                                revisionFutures
                                        .add(downloadExecutor
                                                .submit(new Downloader(index,
                                                        zoom + 1)));
                            }
                        }
                    }
                    return builtRevisions;
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                } finally {
                    if (downloadsInProgress.decrementAndGet() == 0) {
                        downloadExecutor.shutdown();
                    }
                }

            }
        }
        // Increment to "lock" the downloadExecutor just in case the first
        // "seed" task finishes before we can finish submitting the "seed"
        // tasks.
        downloadsInProgress.incrementAndGet();
        // Submit the "seed" (starting) download tasks.
        for (String index : PepcoUtil.getSpatialIndicesForPoint(
                startingPoint.lat, startingPoint.lon, STARTING_ZOOM)) {
            revisionFutures.add(downloadExecutor.submit(new Downloader(index,
                    STARTING_ZOOM)));
        }
        // We're done submitting "seed" tasks, remove the "lock" and check if we
        // finished everything by now.
        if (downloadsInProgress.decrementAndGet() == 0) {
            downloadExecutor.shutdown();
        }
        downloadExecutor.awaitTermination(30, TimeUnit.MINUTES);
        final ImmutableSet.Builder<AbstractOutageRevision> builder = ImmutableSet
                .builder();
        for (Future<Collection<AbstractOutageRevision>> future : revisionFutures) {
            builder.addAll(future.get());
        }
        return builder.build();
    }

    @Override
    public void scrape(ParserRun run) throws InterruptedException,
            ExecutionException {
        final Set<AbstractOutageRevision> revisions = downloadOutages(run);
        for (AbstractOutageRevision revision : revisions) {
            dao.updateOutage(revision);
        }
        dao.closeMissingOutages(
                Collections2.transform(revisions, REVISION_TO_ID),
                run.getAsof());
    }
}
