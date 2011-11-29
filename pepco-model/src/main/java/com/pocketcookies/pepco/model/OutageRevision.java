package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
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
		return super.equals(revision) && revision.getCause().equals(this.getCause())
				&& revision.getStatus().equals(this.getStatus());
	}
        @Override
	public boolean equalsIgnoreObservationDate(final AbstractOutageRevision o) {
		if (!(o instanceof OutageRevision)) {
			return false;
		}
		final OutageRevision r = (OutageRevision) o;
		return super.equalsIgnoreObservationDate(o) && getCause().equals(r.getCause()) && getStatus().equals(r.getStatus());
        }

	@Override
	public int hashCode() {
		return super.hashCode() + getCause().hashCode() + getStatus().hashCode();
	}

	public OutageRevision() {
		super();
	}

        @Column(name="CAUSE")
	public String getCause() {
		return cause;
	}

	private void setCause(String cause) {
		this.cause = cause;
	}

        @Column(name="STATUS")
        @Enumerated(EnumType.STRING)
	public CrewStatus getStatus() {
		return status;
	}

	private void setStatus(CrewStatus status) {
		this.status = status;
	};

}
