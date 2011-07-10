package com.pocketcookies.pepco.model;

import java.util.Date;

public class OutageRevision extends AbstractOutageRevision {
	private String cause;
	private CrewStatus status;

	public static enum CrewStatus {
		PENDING, ASSIGNED, EN_ROUTE, ON_SITE
	}

	public OutageRevision(int numCustomersAffected, Date estimatedRestoration,
			final Outage outage, String cause, CrewStatus status) {
		super(numCustomersAffected, estimatedRestoration, outage);
		setCause(cause);
		setStatus(status);
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
	};

}
