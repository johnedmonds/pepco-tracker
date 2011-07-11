package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

public class OutageRevision extends AbstractOutageRevision {
	private String cause;
	private CrewStatus status;

	public static enum CrewStatus {
		PENDING, ASSIGNED, EN_ROUTE, ON_SITE
	}

	public OutageRevision(int numCustomersAffected,
			Timestamp estimatedRestoration, final Timestamp observationDate,
			final Outage outage, final ParserRun run, String cause,
			CrewStatus status) {
		super(numCustomersAffected, estimatedRestoration, observationDate,
				outage, run);
		setCause(cause);
		setStatus(status);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OutageRevision))
			return false;
		final OutageRevision revision = (OutageRevision) o;
		return super.equals(revision) && revision.cause.equals(this.cause)
				&& revision.status.equals(this.status);
	}

	@Override
	public int hashCode() {
		return super.hashCode() + cause.hashCode() + status.hashCode();
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
