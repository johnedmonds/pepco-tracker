package com.pocketcookies.pepco.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class OutageCluster extends AbstractOutage {
	private Collection<OutageClusterRevision> revisions = new LinkedList<OutageClusterRevision>();

	public OutageCluster() {
		super();
	}

	public OutageCluster(int id, double lat, double lon, Date earliestReport,
			Date observedEnd) {
		super(id, lat, lon, earliestReport, observedEnd);
	}

	public Collection<OutageClusterRevision> getRevisions() {
		return revisions;
	}

	@SuppressWarnings("unused")
	private void setRevisions(Collection<OutageClusterRevision> revisions) {
		this.revisions = revisions;
	}

}
