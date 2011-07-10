package com.pocketcookies.pepco.model;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

public class Outage extends AbstractOutage {
	private Collection<OutageRevision> revisions = new LinkedList<OutageRevision>();

	public Outage() {
		super();
	}

	public Outage(int id, double lat, double lon, Date earliestReport,
			Date observedEnd) {
		super(id, lat, lon, earliestReport, observedEnd);
	}

	public Collection<OutageRevision> getRevisions() {
		return revisions;
	}

	@SuppressWarnings("unused")
	private void setRevisions(Collection<OutageRevision> revisions) {
		this.revisions = revisions;
	}

}
