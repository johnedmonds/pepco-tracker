package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableList;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.ParserRun;

/**
 * Handles quickly downloading and parsing outages from Pepco.
 * 
 * Outages are downloaded in a multi-threaded fashion so we won't have many
 * problems where Pepco removes data before we can get to it.
 * 
 * @author john.a.edmonds@gmail.com (John "Jack" Edmonds)
 */
@Service
public class OutageDownloader {
	private static final Logger logger = Logger
			.getLogger(OutageDownloader.class);
	private static final PointDouble DEFAULT_STARTING_POINT = new PointDouble(
			38.96, -77.03);
	private static final int STARTING_ZOOM = 8;
	private static final int MAX_ZOOM = 15;
	private final StormCenterLoader stormCenterLoader;
	private final String outagesFolderName;
	private final PointDouble startingPoint;

	@Inject
	public OutageDownloader(final StormCenterLoader stormCenterLoader) throws IOException {
		this(stormCenterLoader, PepcoUtil.getTextFromOnlyElement(
				stormCenterLoader.loadXMLRequest(PepcoScraper.DATA_HTML_PREFIX
						+ PepcoScraper.DIRECTORY_SUFFIX), "directory"),
				DEFAULT_STARTING_POINT);
	}

	public OutageDownloader(final StormCenterLoader stormCenterLoader,
			String outagesFolderName, PointDouble startingPoint) {
		this.stormCenterLoader = stormCenterLoader;
		this.outagesFolderName = outagesFolderName;
		this.startingPoint = startingPoint;
	}

	/**
	 * Downloads and parses outages from Pepco. Note that the downloads happen
	 * in parallel.
	 */
	public Set<AbstractOutageRevision> downloadOutages(final ParserRun run)
			throws InterruptedException, ExecutionException {
		/*
		 * We can use a fixed thread pool because submitting tasks does not
		 * block. They just get backed up.
		 */
		final ExecutorService downloadExecutor = Executors
				.newFixedThreadPool(10);
		final Queue<Future<Collection<AbstractOutageRevision>>> revisionFutures = new LinkedBlockingQueue<Future<Collection<AbstractOutageRevision>>>();

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
				if (zoom > MAX_ZOOM
						|| visitedIndices.contains(sectionIdBeingDownloaded)) {
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
						doc.getElementsByTagName("item"), run, zoom);
				for (AbstractOutageRevision revision : builtRevisions) {
					// We only need to zoom in if there's a cluster.
					// Otherwise, zooming in will give us no more useful
					// information.
					if (revision instanceof OutageClusterRevision) {
						for (String index : PepcoUtil
								.getSpatialIndicesForPoint(revision.getOutage()
										.getLat(), revision.getOutage()
										.getLon(), zoom + 1)) {
							// Recurse. Basically, submit a download task
							// for the next highest zoomlevel.
							revisionFutures.add(downloadExecutor
									.submit(new Downloader(index, zoom + 1)));
						}
					}
				}
				return builtRevisions;
			}
		}
		// Submit the "seed" (starting) download tasks.
		for (String index : PepcoUtil.getSpatialIndicesForPoint(
				startingPoint.lat, startingPoint.lon, STARTING_ZOOM)) {
			revisionFutures.add(downloadExecutor.submit(new Downloader(index,
					STARTING_ZOOM)));
		}
		final OutageRevisionCombiner combiner = new OutageRevisionCombiner();
		/*
		 * When the queue is empty, we are done. We know the queue will never be
		 * empty before we're done because each future we're waiting on will not
		 * return until it either submits another future to run, or doesn't need
		 * to zoom in any more and returns.
		 */
		while (!revisionFutures.isEmpty()) {
			combiner.addAll(revisionFutures.poll().get());
		}
		for (Future<Collection<AbstractOutageRevision>> future : revisionFutures) {
			combiner.addAll(future.get());
		}
		downloadExecutor.shutdown();
		/*
		 * The executor should shut down *IMMEDIATELY* because we know (unless
		 * one of my assumptions is wrong (that's what asserts are for! :-) ))
		 * that everything is done processing by now so nothing should be
		 * running.
		 */
		assert downloadExecutor.isShutdown();
		return combiner.getCombinedRevisions();
	}

	static AbstractOutageRevision parseOutage(final Node item,
			final ParserRun run, int zoomLevel) {
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
					numOutages, zoomLevel, zoomLevel);
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
					cause, status, zoomLevel);
			outage.getRevisions().add(outageRevision);
			return outageRevision;
		}
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

	static Collection<AbstractOutageRevision> parseOutages(
			final NodeList items, final ParserRun run, final int zoomLevel) {
		final ImmutableList.Builder<AbstractOutageRevision> builder = ImmutableList
				.builder();
		for (int i = 0; i < items.getLength(); i++) {
			builder.add(parseOutage(items.item(i), run, zoomLevel));
		}
		return builder.build();
	}
}
