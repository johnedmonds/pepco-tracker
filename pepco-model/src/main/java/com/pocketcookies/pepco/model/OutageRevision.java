package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
public class OutageRevision extends AbstractOutageRevision {
	// What caused the outage according to Pepco.
	// Shouldn't be null.
	private String cause;
	// The status of the crew(s) resolving the outage according to Pepco
	// Note, this should never be null however, we cannot make it
	// "nullable=false"
	// because the *column* can be null (when inserting a
	// OutageClusterRevision).
	private CrewStatus status;

	public static enum CrewStatus {
		PENDING, ASSIGNED, EN_ROUTE, ON_SITE
	}

	public OutageRevision(int numCustomersAffected,
			Timestamp estimatedRestoration, final Outage outage,
			final ParserRun run, String cause, CrewStatus status,
			int firstSeenZoomLevel) {
		super(numCustomersAffected, estimatedRestoration, outage, run,
				firstSeenZoomLevel);
		setCause(cause);
		setStatus(status);
	}

	@Override
	public boolean equalsIgnoreRun(final AbstractOutageRevision o) {
		if (!(o instanceof OutageRevision)) {
			return false;
		}
		final OutageRevision r = (OutageRevision) o;
		return super.equalsIgnoreRun(o) && getCause().equals(r.getCause())
				&& getStatus().equals(r.getStatus());
	}

	public OutageRevision() {
		super();
	}

	@Column(name = "CAUSE")
	public String getCause() {
		return cause;
	}

	private void setCause(String cause) {
		this.cause = cause;
	}

	@Column(name = "STATUS")
	@Enumerated(EnumType.STRING)
	public CrewStatus getStatus() {
		return status;
	}

	private void setStatus(CrewStatus status) {
		this.status = status;
	};

}
