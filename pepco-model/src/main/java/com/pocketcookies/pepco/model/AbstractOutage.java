package com.pocketcookies.pepco.model;

import java.util.Date;

/**
 * Represents a single outage over its lifetime.
 * 
 * @author John Edmonds
 * 
 */
public abstract class AbstractOutage {
	private int id;
	private double lat, lon;
	private Date earliestReport;

	/**
	 * The time we scraped the site and this outage disappeared. This will
	 * always be later than the actual time.
	 */
	private Date observedEnd;

	protected AbstractOutage() {
		super();
	}

	public AbstractOutage(int id, double lat, double lon, Date earliestReport,
			Date observedEnd) {
		super();
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.earliestReport = earliestReport;
		if (earliestReport == null)
			throw new IllegalArgumentException("earliestReport cannot be null.");
		this.observedEnd = observedEnd;
	}

	@Override
	public boolean equals(final Object o) {
		final AbstractOutage a = (AbstractOutage) o;
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

	@SuppressWarnings("unused")
	private void setLat(double lat) {
		this.lat = lat;
	}

	@SuppressWarnings("unused")
	private void setLon(double lon) {
		this.lon = lon;
	}

	@SuppressWarnings("unused")
	private void setEarliestReport(Date earliestReport) {
		this.earliestReport = earliestReport;
	}

	@SuppressWarnings("unused")
	private void setObservedEnd(Date observedEnd) {
		this.observedEnd = observedEnd;
	}

}