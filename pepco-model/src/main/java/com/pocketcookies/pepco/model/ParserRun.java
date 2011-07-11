package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.Collection;

public class ParserRun {
	private int id;
	private Timestamp runTime;
	private Collection<AbstractOutageRevision> revisions;

	public ParserRun(Timestamp runTime) {
		super();
		setRunTime(runTime);
	}

	public ParserRun() {
		super();
	}

	public int getId() {
		return id;
	}

	public Timestamp getRunTime() {
		return runTime;
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	private void setRunTime(Timestamp runTime) {
		this.runTime = runTime;
	}

	public Collection<AbstractOutageRevision> getRevisions() {
		return revisions;
	}

	@SuppressWarnings("unused")
	private void setRevisions(Collection<AbstractOutageRevision> revisions) {
		this.revisions = revisions;
	}

}
