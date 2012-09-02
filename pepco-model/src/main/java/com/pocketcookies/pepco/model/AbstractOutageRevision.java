package com.pocketcookies.pepco.model;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * We like to keep track of all the things that happen to a revision over its
 * lifetime.
 * 
 * There are two different types of revisions, those for regular outages, and
 * those for outage clusters. This is the parent class for both those types of
 * outages.
 * 
 * @author jack
 * 
 */
@Entity
@Table(name = "OUTAGEREVISIONS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "OUTAGETYPE")
public abstract class AbstractOutageRevision implements Serializable,
		Comparable<AbstractOutageRevision> {

	private int id;
	/**
	 * The number of customers affected. If fewer than 5 customers are affected,
	 * Pepco will say "Less than 5". To represent this uncertainty, we will use
	 * 0 to represent that there are fewer than 5 customers for that outage.
	 */
	private int numCustomersAffected;
	private Timestamp estimatedRestoration;
	private Outage outage;
	// The parser run with which this revision is associated. This lets us
	// group together outages so that we can know the state of all the
	// outages at a particular time.
	private ParserRun run;
	private int firstSeenZoomLevel;

	protected AbstractOutageRevision() {
		super();
	}

	public AbstractOutageRevision(int numCustomersAffected,
			Timestamp estimatedRestoration, Outage outage, final ParserRun run,
			int firstSeenZoomLevel) {
		this();
		setNumCustomersAffected(numCustomersAffected);
		setEstimatedRestoration(estimatedRestoration);
		setFirstSeenZoomLevel(firstSeenZoomLevel);
		setOutage(outage);
		setRun(run);
	}

	/**
	 * Checks that these two objects are the same except for the observationDate
	 * which is disregarded.
	 * 
	 * @param revision
	 *            The revision to compare against.
	 * @return True of the objects are the same ignoring the observationDate.
	 */
	public boolean equalsIgnoreRun(AbstractOutageRevision revision) {
		return new EqualsBuilder()
				.append(getEstimatedRestoration(),
						revision.getEstimatedRestoration())
				.append(getNumCustomersAffected(),
						revision.getNumCustomersAffected())
				.append(getFirstSeenZoomLevel(),
						revision.getFirstSeenZoomLevel()).isEquals();
	}

	@Id
	@GeneratedValue
	@Column(name = "ID")
	public int getId() {
		return id;
	}

	@Column(name = "NUMCUSTOMERSAFFECTED")
	public int getNumCustomersAffected() {
		return numCustomersAffected;
	}

	@Column(name = "ESTIMATEDRESTORATION")
	public Timestamp getEstimatedRestoration() {
		return estimatedRestoration;
	}

	@Column(name = "FIRST_SEEN_ZOOM_LEVEL")
	public int getFirstSeenZoomLevel() {
		return firstSeenZoomLevel;
	}

	@ManyToOne
	@JoinColumn(name = "OUTAGE")
	public Outage getOutage() {
		return outage;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	private void setNumCustomersAffected(int numCustomersAffected) {
		this.numCustomersAffected = numCustomersAffected;
	}

	private void setEstimatedRestoration(Timestamp estimatedRestoration) {
		this.estimatedRestoration = estimatedRestoration;
	}

	public void setFirstSeenZoomLevel(int firstSeenZoomLevel) {
		this.firstSeenZoomLevel = firstSeenZoomLevel;
	}

	public void setOutage(Outage outage) {
		this.outage = outage;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "RUN")
	public ParserRun getRun() {
		return run;
	}

	private void setRun(ParserRun run) {
		this.run = run;
	}

	@Override
	public int compareTo(final AbstractOutageRevision o) {
		final int comparison = getRun().compareTo(o.getRun());
		if (comparison == 0) {
			// Make sure we don't say two objects are equal just because they
			// occur at the same time.
			return getId() - o.getId();
		} else {
			return -comparison;
		}
	}
}
