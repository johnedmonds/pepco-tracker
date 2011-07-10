package com.pocketcookies.pepco.scraper;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.htmlparser.Parser;
import org.htmlparser.filters.StringFilter;
import org.htmlparser.util.ParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.pocketcookies.pepco.model.AbstractOutage;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.OutageCluster;
import com.pocketcookies.pepco.model.OutageClusterRevision;
import com.pocketcookies.pepco.model.OutageRevision;
import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.dao.OutageDAO;

public class PepcoScraper {
	private static final String dataHTMLPrefix = "http://www.pepco.com/home/emergency/maps/stormcenter/data/outages/";
	private static final Logger logger = Logger.getLogger(PepcoScraper.class);
	private static final int maxZoom = 15;
	private final String outagesFolder;
	private final SessionFactory sessionFactory;
	private final OutageDAO outageDao;

	/**
	 * Used to record which indices we have scraped so we don't have to scrape
	 * them again.
	 */
	private final Set<String> scrapedSpatialIndices = new TreeSet<String>();

	public PepcoScraper(final SessionFactory sessionFactory,
			final OutageDAO outageDao) throws ClientProtocolException,
			IllegalStateException, IOException, SAXException,
			ParserConfigurationException, FactoryConfigurationError {
		this.outagesFolder = getOutagesFolderName();
		this.sessionFactory = sessionFactory;
		this.outageDao = outageDao;
	}

	public String getOutagesFolderName() throws ClientProtocolException,
			IOException, IllegalStateException, SAXException,
			ParserConfigurationException, FactoryConfigurationError {
		HttpGet get = new HttpGet(dataHTMLPrefix + "metadata.xml");
		final HttpParams params = get.getParams();
		params.setLongParameter("timestamp", new Date().getTime());
		return DocumentBuilderFactory
				.newInstance()
				.newDocumentBuilder()
				.parse(new DefaultHttpClient().execute(get).getEntity()
						.getContent()).getElementsByTagName("directory")
				.item(0).getFirstChild().getNodeValue();
	}

	/**
	 * Here is a short description of how scraping will work.
	 * 
	 * We request the most zoomed out version of the data. Then we save all
	 * outages to the database. Then, for each clustered outage, we that that
	 * clustered outage's lat lon coords and request the next level of detail
	 * for those coordinates.
	 * 
	 * @author John Edmonds
	 */
	private class Scraper implements Runnable {
		public final String spatialIndex;
		// Used for the cookie. Otherwise, we seem to get a 403.
		public final double lat, lon;
		// Used for the cookie. Otherwise, we seem to get a 403.
		public final int zoom;

		public Scraper(String spatialIndex, double lat, double lon, int zoom) {
			super();
			this.spatialIndex = spatialIndex;
			this.lat = lat;
			this.lon = lon;
			this.zoom = zoom;
		}

		@Override
		public void run() {
			try {
				if (this.zoom > maxZoom
						|| scrapedSpatialIndices.contains(this.spatialIndex))
					return;
				scrapedSpatialIndices.add(this.spatialIndex);
				logger.debug("Scraping " + this.spatialIndex);
				parse(makeRequest());
			} catch (Exception e) {
				logger.error("Error while attempting to download outage list.",
						e);
			}
		}

		private Document makeRequest() throws ClientProtocolException,
				IOException, IllegalStateException, SAXException,
				ParserConfigurationException, FactoryConfigurationError {
			final HttpClient client = new DefaultHttpClient();
			final HttpGet get = new HttpGet(dataHTMLPrefix + outagesFolder
					+ "/" + spatialIndex + ".xml");
			get.getParams().setLongParameter("timestamp", new Date().getTime());
			final CookieStore cookies = new BasicCookieStore();
			final HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, cookies);
			cookies.addCookie(new BasicClientCookie("pepscstate",
					"map_type:r|homepage:" + lat + "," + lon + "," + zoom));
			final HttpResponse response = client.execute(get);
			return DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(response.getEntity().getContent());
		}

		/**
		 * Parses the document.
		 * 
		 * @param doc
		 * @throws ParserException
		 * @throws ParseException
		 */
		private void parse(Document doc) throws ParserException, ParseException {
			final SimpleDateFormat dateParser = new SimpleDateFormat(
					"MMM dd, h:mm a");
			final NodeList list = doc.getElementsByTagName("item");
			for (int i = 0; i < list.getLength(); i++) {
				final Element el = (Element) list.item(i);
				final double lat, lon;
				// Find lat and lon.
				// The string containing the unparsed lat lon.
				final StringTokenizer slatlon;
				if (el.getElementsByTagName("georss:point").getLength() == 0)
					slatlon = new StringTokenizer(el
							.getElementsByTagName("georss:polygon").item(0)
							.getFirstChild().getNodeValue());
				else
					slatlon = new StringTokenizer(el
							.getElementsByTagName("georss:point").item(0)
							.getFirstChild().getNodeValue());
				lat = Double.parseDouble(slatlon.nextToken());
				lon = Double.parseDouble(slatlon.nextToken());

				// Parse the description.
				final int numCustomersAffected;
				final Date earliestReport;
				final Date estimatedRestoration;
				final org.htmlparser.util.NodeList descList = Parser
						.createParser(
								"<body>" // Make sure nodes get a parent.
										+ el.getElementsByTagName("description")
												.item(0).getFirstChild()
												.getNodeValue() + "</body>",
								null).parse(null).elementAt(0).getChildren();
				final String sNumCustomersAffected = descList
						.extractAllNodesThatMatch(
								new StringFilter("Customers Affected:"))
						.elementAt(0).getNextSibling().getNextSibling()
						.getText().trim();
				numCustomersAffected = sNumCustomersAffected
						.equals("Less than 5") ? 0 : Integer
						.parseInt(sNumCustomersAffected);
				earliestReport = dateParser.parse(descList
						.extractAllNodesThatMatch(new StringFilter("Report"))
						.elementAt(0).getNextSibling().getNextSibling()
						.getText().trim());
				estimatedRestoration = dateParser.parse(descList
						.extractAllNodesThatMatch(
								new StringFilter("Restoration")).elementAt(0)
						.getNextSibling().getNextSibling().getText().trim());
				final AbstractOutage outage;
				final AbstractOutageRevision revision;
				// Check if this is an Outage or an OutageCluster.
				if (el.getElementsByTagName("title").item(0).getFirstChild()
						.getNodeValue().contains("outagesclusters")) {
					final int numOutages;
					numOutages = Integer
							.parseInt(descList
									.extractAllNodesThatMatch(
											new StringFilter(
													"Number of Outage Orders:"))
									.elementAt(0).getNextSibling()
									.getNextSibling().getText().trim());
					outage = new OutageCluster(lat, lon, earliestReport, null);
					revision = new OutageClusterRevision(numCustomersAffected,
							estimatedRestoration, (OutageCluster) outage,
							numOutages);
				} else {
					final String cause;
					final CrewStatus status;
					cause = descList
							.extractAllNodesThatMatch(new StringFilter("Cause"))
							.elementAt(0).getNextSibling().getNextSibling()
							.getText().trim();
					status = CrewStatus.valueOf(descList
							.extractAllNodesThatMatch(
									new StringFilter("Crew Status"))
							.elementAt(0).getNextSibling().getNextSibling()
							.getText().trim().replace(' ', '_').toUpperCase());
					outage = new Outage(lat, lon, earliestReport, null);
					revision = new OutageRevision(numCustomersAffected,
							estimatedRestoration, (Outage) outage, cause,
							status);

				}
				final AbstractOutage existingOutage = outageDao
						.getActiveOutage(outage.getLat(), outage.getLon(),
								outage.getClass());
				if (existingOutage == null) {
					sessionFactory.getCurrentSession().save(outage);
					sessionFactory.getCurrentSession().save(revision);
				} else {
					if (existingOutage instanceof Outage
							&& !((Outage) existingOutage).getRevisions()
									.contains(revision)) {
						revision.setOutage(existingOutage);
						sessionFactory.getCurrentSession().save(revision);
					} else if (existingOutage instanceof OutageCluster
							&& !((OutageCluster) existingOutage).getRevisions()
									.contains(revision)) {
						revision.setOutage(existingOutage);
						sessionFactory.getCurrentSession().save(revision);
					}
				}

				// If this is a clustered outage, try to take a closer look and
				// see if we can get the individual outages.
				for (final String spatialIndex : getSpatialIndicesForPoint(lat,
						lon, zoom + 1))
					new Scraper(spatialIndex, lat, lon, zoom + 1).run();
			}
		}
	}

	public Collection<String> getSpatialIndicesForPoint(double lat, double lon,
			double zoom) {
		double minLat = -85.05112878;
		double maxLat = 85.05112878;
		double minLong = -180;
		double maxLong = 180;
		String indexName = "";
		int A = 0;
		int curZoom = 0;
		int D = 0;
		Collection<String> indexNames = new LinkedList<String>();
		String[] zoomLevels = new String[] { "0", "1", "2", "3" };
		for (curZoom = 0; curZoom < zoom; curZoom++) {
			A = 0;
			if (lat < ((maxLat + minLat) / 2)) {
				A = A + 2;
				maxLat = (maxLat + minLat) / 2;
			} else {
				minLat = (maxLat + minLat) / 2;
			}
			if (lon > ((maxLong + minLong) / 2)) {
				A = A + 1;
				minLong = (maxLong + minLong) / 2;
			} else {
				maxLong = (maxLong + minLong) / 2;
			}
			indexName = indexName + A;
		}
		if (indexName.length() > 1) {
			indexName = indexName.substring(0, indexName.length() - 2);
			for (curZoom = 0; curZoom < zoomLevels.length; curZoom++) {
				for (D = 0; D < zoomLevels.length; D++) {
					indexNames.add(indexName + zoomLevels[curZoom]
							+ zoomLevels[D]);
				}
			}
		} else {
			indexNames = Arrays.asList(zoomLevels);
		}
		return indexNames;
	};

	public void scrape() {
		for (final String spatialIndex : getSpatialIndicesForPoint(
				38.96000000000001, -77.02999999999999, 7)) {
			new Scraper(spatialIndex, 38.96000000000001, -77.02999999999999, 7)
					.run();
		}
	}

	public static void main(final String[] args)
			throws ClientProtocolException, IOException, IllegalStateException,
			SAXException, ParserConfigurationException,
			FactoryConfigurationError, ParserException {
		final SessionFactory sessionFactory = new Configuration().configure()
				.buildSessionFactory();
		sessionFactory.getCurrentSession().beginTransaction();
		new PepcoScraper(sessionFactory, new OutageDAO(sessionFactory))
				.scrape();
		sessionFactory.getCurrentSession().getTransaction().commit();
		sessionFactory.getCurrentSession().close();
		sessionFactory.close();
	}
}
