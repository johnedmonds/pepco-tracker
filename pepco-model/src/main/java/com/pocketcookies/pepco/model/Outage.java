package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a single outage over its lifetime.
 * 
 * @author John Edmonds
 * 
 */
public class Outage {
	private int id;
	private double lat, lon;
	private Timestamp earliestReport;
        //The zoom levels on which this outage appears.
        private Set<Integer> zoomLevels;

	/**
	 * The time we scraped the site and this outage disappeared. This will
	 * always be later than the actual time.
	 */
	private Timestamp observedEnd;

	private List<AbstractOutageRevision> revisions = new LinkedList<AbstractOutageRevision>();

	protected Outage() {
		super();
	}

	public Outage(double lat, double lon, Timestamp earliestReport,
			Timestamp observedEnd) {
		super();
		if (earliestReport == null)
			throw new IllegalArgumentException("earliestReport cannot be null.");
		setLat(lat);
		setLon(lon);
		setEarliestReport(earliestReport);
		setObservedEnd(observedEnd);
                setZoomLevels(new TreeSet<Integer>());
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		final Outage a = (Outage) o;
		if (this.observedEnd != null) {
			if (!this.observedEnd.equals(a.observedEnd))
				return false;
		} else if (a.observedEnd != null)
			return false;
		return o.getClass().equals(this.getClass()) && a.lat == this.lat
				&& a.lon == this.lon
				&& a.earliestReport.equals(this.earliestReport);
	}

	public int getId() {
		return id;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public Timestamp getEarliestReport() {
		return earliestReport;
	}

	public Timestamp getObservedEnd() {
		return observedEnd;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	private void setLat(double lat) {
		this.lat = lat;
	}

	private void setLon(double lon) {
		this.lon = lon;
	}

	private void setEarliestReport(Timestamp earliestReport) {
		this.earliestReport = earliestReport;
	}

	public void setObservedEnd(Timestamp observedEnd) {
		this.observedEnd = observedEnd;
	}

	public List<AbstractOutageRevision> getRevisions() {
		return revisions;
	}

	@SuppressWarnings("unused")
	private void setRevisions(List<AbstractOutageRevision> revisions) {
		this.revisions = revisions;
	}

        public Set<Integer>getZoomLevels(){return this.zoomLevels;}
        public void setZoomLevels(final Set<Integer>zoomLevels){this.zoomLevels=zoomLevels;}
}