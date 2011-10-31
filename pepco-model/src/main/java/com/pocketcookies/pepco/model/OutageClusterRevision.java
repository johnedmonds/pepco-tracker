package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class OutageClusterRevision extends AbstractOutageRevision {
	// The number of outages associated with this cluster. We don't currently
	// have a way of tracking exactly which outages correspond with this
	// cluster so for now we just keep track of the count.
	private int numOutages;

	public OutageClusterRevision() {
		super();
	}

	public OutageClusterRevision(final int numCustomersAffected,
			final Timestamp estimatedRestoration,
			final Timestamp observationDate, final Outage outage,
			final ParserRun run, int numOutages) {
		super(numCustomersAffected, estimatedRestoration, observationDate,
				outage, run);
		setNumOutages(numOutages);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof OutageClusterRevision))
			return false;
		final OutageClusterRevision revision = (OutageClusterRevision) o;
		return super.equals(o) && revision.getNumOutages() == this.getNumOutages();
	}

	@Override
	public int hashCode() {
		return super.hashCode() + getNumOutages();
	}

        @Column(name="NUMOUTAGES")
	public int getNumOutages() {
		return numOutages;
	}

	private void setNumOutages(int numOutages) {
		this.numOutages = numOutages;
	}

}
