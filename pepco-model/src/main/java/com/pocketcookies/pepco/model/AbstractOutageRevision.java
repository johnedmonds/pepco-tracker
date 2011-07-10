package com.pocketcookies.pepco.model;

import java.util.Date;

/**
 * We like to keep track of all the things that happen to a revision over its
 * lifetime.
 * 
 * @author jack
 * 
 */
public abstract class AbstractOutageRevision {
	private int id;
	private int numCustomersAffected;
	private Date estimatedRestoration;
	private AbstractOutage outage;

	protected AbstractOutageRevision() {
		super();
	}

	public AbstractOutageRevision(int id, int numCustomersAffected,
			Date estimatedRestoration, AbstractOutage outage) {
		this();
		setId(id);
		setNumCustomersAffected(numCustomersAffected);
		setEstimatedRestoration(estimatedRestoration);
		setOutage(outage);
	}

	public int getId() {
		return id;
	}

	public int getNumCustomersAffected() {
		return numCustomersAffected;
	}

	public Date getEstimatedRestoration() {
		return estimatedRestoration;
	}

	public AbstractOutage getOutage() {
		return outage;
	}

	private void setId(int id) {
		this.id = id;
	}

	private void setNumCustomersAffected(int numCustomersAffected) {
		this.numCustomersAffected = numCustomersAffected;
	}

	private void setEstimatedRestoration(Date estimatedRestoration) {
		this.estimatedRestoration = estimatedRestoration;
	}

	private void setOutage(AbstractOutage outage) {
		this.outage = outage;
	}

}
