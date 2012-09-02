package com.pocketcookies.pepco.scraper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.ParserRun;

public class OutageDownloaderTest {

	private static final int DEFAULT_FIRST_SEEN_ZOOM_LEVEL = 1;
	@Test
	public void testParseOutage() throws SAXException, IOException,
	        ParserConfigurationException {
	    final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
	    final Collection<AbstractOutageRevision> revisions = OutageDownloader
	            .parseOutages(
	                    TestUtil.loadXml(
	                            getClass().getResourceAsStream(
	                                    "/testxml/outages_single.xml"))
	                            .getElementsByTagName("item"), run, DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
	    final AbstractOutageRevision abstractOutageRevision = Iterables
	            .getOnlyElement(revisions);
	    assertEquals(OutageRevision.class, abstractOutageRevision.getClass());
	    final OutageRevision revision = (OutageRevision) abstractOutageRevision;
	    assertEquals(10, revision.getNumCustomersAffected());
	    assertEquals(
	            "Jan 1, 1:00 PM",
	            PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision
	                    .getOutage().getEarliestReport().getTime())));
	    assertEquals("Jan 1, 2:00 PM",
	            PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision
	                    .getEstimatedRestoration().getTime())));
	    assertEquals(new DateTime().getYear(), new DateTime(revision
	            .getOutage().getEarliestReport().getTime()).getYear());
	    assertEquals(new DateTime().getYear(), new DateTime(revision
	            .getEstimatedRestoration().getTime()).getYear());
	    assertEquals(2, revision.getOutage().getLat(), .0001);
	    assertEquals(3, revision.getOutage().getLon(), .0001);
	    assertEquals("Under Evaluation", revision.getCause());
	    assertEquals(CrewStatus.PENDING, revision.getStatus());
	    assertEquals(2, revision.getRun().getAsof().getTime());
	    assertEquals(DEFAULT_FIRST_SEEN_ZOOM_LEVEL, revision.getFirstSeenZoomLevel());
	}

	@Test
	public void testParseOutageCluster() throws SAXException, IOException,
	        ParserConfigurationException {
	    final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
	    final AbstractOutageRevision abstractOutageRevision = Iterables
	            .getOnlyElement(OutageDownloader.parseOutages(
	                    TestUtil.loadXml(
	                            getClass().getResourceAsStream(
	                                    "/testxml/outages_cluster.xml"))
	                            .getElementsByTagName("item"), run, DEFAULT_FIRST_SEEN_ZOOM_LEVEL));
	    assertEquals(OutageClusterRevision.class,
	            abstractOutageRevision.getClass());
	    final OutageClusterRevision revision = (OutageClusterRevision) abstractOutageRevision;
	    assertEquals(10, revision.getNumCustomersAffected());
	
	    assertEquals(
	            "Jan 1, 1:00 PM",
	            PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision
	                    .getOutage().getEarliestReport().getTime())));
	    assertEquals("Jan 1, 2:00 PM",
	            PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision
	                    .getEstimatedRestoration().getTime())));
	    assertEquals(new DateTime().getYear(), new DateTime(revision
	            .getOutage().getEarliestReport().getTime()).getYear());
	    assertEquals(new DateTime().getYear(), new DateTime(revision
	            .getEstimatedRestoration().getTime()).getYear());
	    assertEquals(1, revision.getOutage().getLat(), .0001);
	    assertEquals(2, revision.getOutage().getLon(), .0001);
	    assertEquals(2, revision.getNumOutages());
	    assertEquals(2, revision.getRun().getAsof().getTime());
	    assertEquals(DEFAULT_FIRST_SEEN_ZOOM_LEVEL, revision.getFirstSeenZoomLevel());
	    assertEquals(DEFAULT_FIRST_SEEN_ZOOM_LEVEL, revision.getLastSeenZoomLevel());
	}

	@Test
	public void testParsePendingEstimatedRestorationOutageCluster()
	        throws SAXException, IOException, ParserConfigurationException {
	    final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
	    final AbstractOutageRevision abstractOutageRevision = Iterables
	            .getOnlyElement(OutageDownloader
	                    .parseOutages(
	                            TestUtil.loadXml(
	                                    getClass()
	                                            .getResourceAsStream(
	                                                    "/testxml/outages_cluster_pending_restoration.xml"))
	                                    .getElementsByTagName("item"), run, DEFAULT_FIRST_SEEN_ZOOM_LEVEL));
	    assertEquals(OutageClusterRevision.class,
	            abstractOutageRevision.getClass());
	    final OutageClusterRevision revision = (OutageClusterRevision) abstractOutageRevision;
	    assertEquals(10, revision.getNumCustomersAffected());
	    assertEquals(
	            "Jan 1, 1:00 PM",
	            PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision
	                    .getOutage().getEarliestReport().getTime())));
	    // The year should always be the current year as Pepco does not send
	    // back the year of the outage.
	    assertEquals(new DateTime().getYear(), new DateTime(revision
	            .getOutage().getEarliestReport().getTime()).getYear());
	    assertEquals(1, revision.getOutage().getLat(), .0001);
	    assertEquals(2, revision.getOutage().getLon(), .0001);
	    assertEquals(2, revision.getNumOutages());
	    assertEquals(2, revision.getRun().getAsof().getTime());
	    assertEquals(DEFAULT_FIRST_SEEN_ZOOM_LEVEL, revision.getFirstSeenZoomLevel());
	    assertEquals(DEFAULT_FIRST_SEEN_ZOOM_LEVEL, revision.getLastSeenZoomLevel());
	    assertNull(revision.getEstimatedRestoration());
	}

	@Test
	public void testParsePendingRestorationOutage() throws SAXException,
	        IOException, ParserConfigurationException {
	    final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(2));
	    final AbstractOutageRevision abstractOutageRevision = Iterables
	            .getOnlyElement(OutageDownloader
	                    .parseOutages(
	                            TestUtil.loadXml(
	                                    getClass()
	                                            .getResourceAsStream(
	                                                    "/testxml/outages_single_pending_restoration.xml"))
	                                    .getElementsByTagName("item"), run, DEFAULT_FIRST_SEEN_ZOOM_LEVEL));
	    assertEquals(OutageRevision.class, abstractOutageRevision.getClass());
	    final OutageRevision revision = (OutageRevision) abstractOutageRevision;
	    assertEquals(10, revision.getNumCustomersAffected());
	    assertEquals(
	            "Jan 1, 1:00 PM",
	            PepcoUtil.PEPCO_DATE_FORMATTER.print(new DateTime(revision
	                    .getOutage().getEarliestReport().getTime())));
	    assertEquals(new DateTime().getYear(), new DateTime(revision
	            .getOutage().getEarliestReport().getTime()).getYear());
	    assertEquals(2, revision.getOutage().getLat(), .0001);
	    assertEquals(3, revision.getOutage().getLon(), .0001);
	    assertEquals("Under Evaluation", revision.getCause());
	    assertEquals(CrewStatus.PENDING, revision.getStatus());
	    assertEquals(2, revision.getRun().getAsof().getTime());
	    assertEquals(DEFAULT_FIRST_SEEN_ZOOM_LEVEL, revision.getFirstSeenZoomLevel());
	    assertNull(revision.getEstimatedRestoration());
	}

	@Test
	public void testZoomInOnCluster() throws IOException, SAXException,
	        ParserConfigurationException, InterruptedException,
	        ExecutionException {
	    final ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(1));
	    final StormCenterLoader loader = mock(StormCenterLoader.class);
	    final List<String> indicesZoom8 = PepcoUtil.getSpatialIndicesForPoint(
	            0, 0, 8);
	    final String now = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss")
	            .print(new DateTime());
	    when(
	            loader.loadXMLRequest(PepcoScraper.DATA_HTML_PREFIX
	                    + "outages/" + now + "/"
	                    + Iterables.getFirst(indicesZoom8, null) + ".xml"))
	            .thenReturn(
	                    TestUtil.loadXml(getClass().getResourceAsStream(
	                            "/zoomOutageClusterXml/outages_cluster.xml")));
	
	    final List<String> indicesZoom9 = PepcoUtil.getSpatialIndicesForPoint(
	            1, 2, 9);
	    when(
	            loader.loadXMLRequest(PepcoScraper.DATA_HTML_PREFIX
	                    + "outages/" + now + "/"
	                    + Iterables.getFirst(indicesZoom9, null) + ".xml"))
	            .thenReturn(
	                    TestUtil.loadXml(getClass().getResourceAsStream(
	                            "/zoomOutageClusterXml/outages_single.xml")));
	
	    new OutageDownloader(loader, now, new PointDouble(0, 0))
	            .downloadOutages(run);
	    for (String index : Iterables.concat(indicesZoom8, indicesZoom9)) {
	        verify(loader).loadXMLRequest(
	                PepcoScraper.DATA_HTML_PREFIX + "outages/" + now + "/"
	                        + index + ".xml");
	    }
	}

}
