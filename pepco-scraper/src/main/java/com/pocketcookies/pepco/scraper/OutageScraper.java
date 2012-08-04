package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
public class OutageScraper implements Scraper {
	private static final Logger logger = Logger.getLogger(OutageScraper.class);

	private static final Function<AbstractOutageRevision, Integer> REVISION_TO_ID = new Function<AbstractOutageRevision, Integer>() {
		@Override
		public Integer apply(AbstractOutageRevision input) {
			return input.getOutage().getId();
		}
	};

	private final OutageDAO dao;
	private final OutageDownloader outageDownloader;

	@Inject
	public OutageScraper(final OutageDAO dao,
			final OutageDownloader outageDownloader) {
		this.dao = dao;
		this.outageDownloader = outageDownloader;
	}

	/**
	 * Used only for Spring. Don't use this constructor!
	 */
	protected OutageScraper() {
		this(null, null);
	}

	@Override
	@Transactional
	public void scrape(ParserRun run) throws InterruptedException,
			ExecutionException {
		logger.info("Starting download.");
		final Set<AbstractOutageRevision> revisions = outageDownloader.downloadOutages(run);
		logger.info("Finished download.");
		for (AbstractOutageRevision revision : revisions) {
			dao.updateOutage(revision);
		}
		dao.closeMissingOutages(
				Collections2.transform(revisions, REVISION_TO_ID),
				run.getAsof());
	}
}
