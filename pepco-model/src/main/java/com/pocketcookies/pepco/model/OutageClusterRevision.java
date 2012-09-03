package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang.builder.EqualsBuilder;

@Entity
public class OutageClusterRevision extends AbstractOutageRevision {
	// The number of outages associated with this cluster. We don't currently
	// have a way of tracking exactly which outages correspond with this
	// cluster so for now we just keep track of the count.
	private int numOutages;

	// The last zoom level at which we detected this revision.
	private Integer lastSeenZoomLevel;

	public OutageClusterRevision() {
		super();
	}

	public OutageClusterRevision(final int numCustomersAffected,
			final Timestamp estimatedRestoration, final Outage outage,
			final ParserRun run, int numOutages, int firstSeenZoomLevel, Integer lastSeenZoomLevel) {
		super(numCustomersAffected, estimatedRestoration, outage, run, firstSeenZoomLevel);
		setNumOutages(numOutages);
		setLastSeenZoomLevel(lastSeenZoomLevel);
	}

	@Override
	public boolean equalsIgnoreRun(final AbstractOutageRevision o) {
		if (!(o instanceof OutageClusterRevision)) {
			return false;
		}
		final OutageClusterRevision r = (OutageClusterRevision) o;
		return super.equalsIgnoreRun(o)
				&& new EqualsBuilder()
						.append(getNumOutages(), r.getNumOutages())
						.append(getLastSeenZoomLevel(),
								r.getLastSeenZoomLevel()).isEquals();
	}

	@Column(name = "NUMOUTAGES")
	public int getNumOutages() {
		return numOutages;
	}

	@Column(name = "LASTSEENZOOMLEVEL")
	public Integer getLastSeenZoomLevel() {
		return lastSeenZoomLevel;
	}

	public void setLastSeenZoomLevel(Integer lastSeenZoomLevel) {
		this.lastSeenZoomLevel = lastSeenZoomLevel;
	}

	private void setNumOutages(int numOutages) {
		this.numOutages = numOutages;
	}

}
