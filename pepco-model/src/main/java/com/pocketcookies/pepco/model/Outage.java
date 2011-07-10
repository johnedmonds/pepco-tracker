package com.pocketcookies.pepco.model;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a single outage over its lifetime.
 * 
 * @author John Edmonds
 * 
 */
public class Outage {
	private int id;
	private double lat, lon;
	private Date earliestReport;

	/**
	 * The time we scraped the site and this outage disappeared. This will
	 * always be later than the actual time.
	 */
	private Date observedEnd;

	private List<AbstractOutageRevision> revisions = new LinkedList<AbstractOutageRevision>();

	protected Outage() {
		super();
	}

	public Outage(double lat, double lon, Date earliestReport, Date observedEnd) {
		super();
		if (earliestReport == null)
			throw new IllegalArgumentException("earliestReport cannot be null.");
		setLat(lat);
		setLon(lon);
		setEarliestReport(earliestReport);
		setObservedEnd(observedEnd);
	}

	@Override
	public boolean equals(final Object o) {
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

	public Date getEarliestReport() {
		return earliestReport;
	}

	public Date getObservedEnd() {
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

	private void setEarliestReport(Date earliestReport) {
		this.earliestReport = earliestReport;
	}

	private void setObservedEnd(Date observedEnd) {
		this.observedEnd = observedEnd;
	}

	public List<AbstractOutageRevision> getRevisions() {
		return revisions;
	}

	@SuppressWarnings("unused")
	private void setRevisions(List<AbstractOutageRevision> revisions) {
		this.revisions = revisions;
	}

}