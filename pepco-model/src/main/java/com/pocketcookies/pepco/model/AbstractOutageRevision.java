package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

/**
 * We like to keep track of all the things that happen to a revision over its
 * lifetime.
 * 
 * @author jack
 * 
 */
public abstract class AbstractOutageRevision {
	private int id;
	/**
	 * The number of customers affected. If fewer than 5 customers are affected,
	 * Pepco will say "Less than 5". To represent this uncertainty, we will use
	 * 0 to represent that there are fewer than 5 customers for that outage.
	 */
	private int numCustomersAffected;
	private Timestamp estimatedRestoration;
	private Outage outage;
	private Timestamp observationDate;

	protected AbstractOutageRevision() {
		super();
	}

	public AbstractOutageRevision(int numCustomersAffected,
			Timestamp estimatedRestoration, final Timestamp observationDate,
			Outage outage) {
		this();
		setNumCustomersAffected(numCustomersAffected);
		setEstimatedRestoration(estimatedRestoration);
		setOutage(outage);
		setObservationDate(observationDate);
	}

	@Override
	/**
	 * Checks whether this object is the same as o.
	 * 
	 * Note that we do not check observationDate.  Thus, use care when storing collections of AbstractOutageRevision in sets. 
	 */
	public boolean equals(final Object o) {
		if (!(o instanceof AbstractOutageRevision))
			return false;
		final AbstractOutageRevision revision = (AbstractOutageRevision) o;
		return equalsIgnoreObservationDate(revision)
				&& this.observationDate.equals(revision.observationDate);
	}

	public boolean equalsIgnoreObservationDate(AbstractOutageRevision revision) {
		return this.numCustomersAffected == revision.numCustomersAffected
				&& this.estimatedRestoration
						.equals(revision.estimatedRestoration);
	}

	@Override
	public int hashCode() {
		return (int) (numCustomersAffected + estimatedRestoration.getTime() + observationDate
				.getTime());
	}

	public int getId() {
		return id;
	}

	public int getNumCustomersAffected() {
		return numCustomersAffected;
	}

	public Timestamp getEstimatedRestoration() {
		return estimatedRestoration;
	}

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

	public void setOutage(Outage outage) {
		this.outage = outage;
	}

	public Timestamp getObservationDate() {
		return observationDate;
	}

	private void setObservationDate(Timestamp observationDate) {
		this.observationDate = observationDate;
	}

}
