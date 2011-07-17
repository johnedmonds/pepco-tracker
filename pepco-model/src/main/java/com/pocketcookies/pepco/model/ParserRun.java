package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;

public class ParserRun {
	private int id;
	private Timestamp runTime;
	private Collection<AbstractOutageRevision> outageRevisions;
	private Collection<OutageAreaRevision> areaRevisions;

	public ParserRun(final Timestamp runTime) {
		setRunTime(runTime);
		setOutageRevisions(new LinkedList<AbstractOutageRevision>());
		setAreaRevisions(new LinkedList<OutageAreaRevision>());
	}

	public ParserRun() {
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	private void setRunTime(Timestamp runTime) {
		this.runTime = runTime;
	}

	private void setOutageRevisions(
			Collection<AbstractOutageRevision> outageRevisions) {
		this.outageRevisions = outageRevisions;
	}

	private void setAreaRevisions(Collection<OutageAreaRevision> areaRevisions) {
		this.areaRevisions = areaRevisions;
	}

	public int getId() {
		return id;
	}

	public Timestamp getRunTime() {
		return runTime;
	}

	public Collection<AbstractOutageRevision> getOutageRevisions() {
		return outageRevisions;
	}

	public Collection<OutageAreaRevision> getAreaRevisions() {
		return areaRevisions;
	}

}
