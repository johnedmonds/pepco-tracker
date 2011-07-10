package com.pocketcookies.pepco.model;

import java.util.Date;

public class OutageClusterRevision extends AbstractOutageRevision {
	// The number of outages associated with this cluster. We don't currently
	// have a way of tracking exactly which outages correspond with this
	// cluster so for now we just keep track of the count.
	private int numOutages;

	public OutageClusterRevision() {
		super();
	}

	public OutageClusterRevision(final int numCustomersAffected,
			final Date estimatedRestoration, final OutageCluster cluster,
			int numOutages) {
		super(numCustomersAffected, estimatedRestoration, cluster);
		setNumOutages(numOutages);
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof AbstractOutageRevision))
			return false;
		final OutageClusterRevision revision = (OutageClusterRevision) o;
		return super.equals(o) && revision.numOutages == this.numOutages;
	}

	@Override
	public int hashCode() {
		return super.hashCode() + numOutages;
	}

	public int getNumOutages() {
		return numOutages;
	}

	private void setNumOutages(int numOutages) {
		this.numOutages = numOutages;
	}

}
