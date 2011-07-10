package com.pocketcookies.pepco.model;

public class OutageRevision {
	private int id;
	private String cause;
	private CrewStatus status;
	private int numCustomersAffected;
	// Parent outage.
	private Outage outage;

	public static enum CrewStatus {
		PENDING, ASSIGNED, EN_ROUTE, ON_SITE
	}

	public OutageRevision(int id, String cause, CrewStatus status,
			int numCustomersAffected, final Outage outage) {
		this();
		setId(id);
		setCause(cause);
		setStatus(status);
		setNumCustomersAffected(numCustomersAffected);
		setOutage(outage);
	}

	public OutageRevision() {
		super();
	}

	public String getCause() {
		return cause;
	}

	private void setCause(String cause) {
		this.cause = cause;
	}

	public CrewStatus getStatus() {
		return status;
	}

	private void setStatus(CrewStatus status) {
		this.status = status;
	}

	public Outage getOutage() {
		return outage;
	}

	private void setOutage(Outage outage) {
		this.outage = outage;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}

	public int getNumCustomersAffected() {
		return numCustomersAffected;
	}

	private void setNumCustomersAffected(int numCustomersAffected) {
		this.numCustomersAffected = numCustomersAffected;
	};

}
