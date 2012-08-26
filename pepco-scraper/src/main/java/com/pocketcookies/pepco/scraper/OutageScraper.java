package com.pocketcookies.pepco.scraper;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
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
		dao.updateOutages(revisions);
		dao.closeMissingOutages(
				Collections2.transform(revisions, REVISION_TO_ID),
				run.getAsof());
	}
}
