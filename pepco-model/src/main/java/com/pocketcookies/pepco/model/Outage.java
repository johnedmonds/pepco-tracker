package com.pocketcookies.pepco.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * Represents a single outage over its lifetime.
 * 
 * @author John Edmonds
 * 
 */
@Entity
@Table(name="OUTAGES")
public class Outage implements Serializable {
	private int id;
	private double lat;
        private double lon;
	private Timestamp earliestReport;
        //The zoom levels on which this outage appears.
        private Set<Integer> zoomLevels;

	/**
	 * The time we scraped the site and this outage disappeared. This will
	 * always be later than the actual time.
	 */
	private Timestamp observedEnd;

	private SortedSet<AbstractOutageRevision> revisions = new TreeSet<AbstractOutageRevision>();

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
		if (this.getObservedEnd() != null) {
			if (!this.getObservedEnd().equals(a.getObservedEnd()))
				return false;
		} else if (a.getObservedEnd() != null)
			return false;
		return o instanceof Outage && a.getLat() == this.getLat()
				&& a.getLon() == this.getLon()
				&& a.getEarliestReport().equals(this.getEarliestReport());
	}
        
        @Override
        public int hashCode()
        {
            return (int)(getLat()+getLon())+getEarliestReport().hashCode()+getObservedEnd().hashCode();
        }

	@Id
	@GeneratedValue
        @Column(name="ID")
        public int getId() {
		return id;
	}

        @Column(name="LAT")
	public double getLat() {
		return lat;
	}

        @Column(name="LON")
	public double getLon() {
		return lon;
	}

        @Column(name="EARLIESTREPORT")
	public Timestamp getEarliestReport() {
		return earliestReport;
	}

        @Column(name="OBSERVEDEND")
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
        
        @OneToMany(targetEntity=AbstractOutageRevision.class,mappedBy="outage")
	@Sort(type = SortType.NATURAL)
	public SortedSet<AbstractOutageRevision> getRevisions() {
		return revisions;
	}

	@SuppressWarnings("unused")
	private void setRevisions(SortedSet<AbstractOutageRevision> revisions) {
		this.revisions = revisions;
	}

        @ElementCollection
        @CollectionTable(name="ZOOMLEVELS", joinColumns={@JoinColumn(name="ID")})
        @Column(name="ZOOMLEVEL")
        public Set<Integer>getZoomLevels(){return this.zoomLevels;}
        public void setZoomLevels(final Set<Integer>zoomLevels){this.zoomLevels=zoomLevels;}
}