package com.pocketcookies.pepco.model;

import java.util.SortedSet;
import java.util.TreeSet;

public class OutageArea {
	// The zip code(s) that represent this area. There may be more than one zip
	// code because Pepco sometimes combines them.
	private String id;

	private SortedSet<OutageAreaRevision> revisions;

	public OutageArea(final String id) {
		setId(id);
		setRevisions(new TreeSet<OutageAreaRevision>());
	}

	public OutageArea() {
	}

	public String getId() {
		return id;
	}

	private void setId(String id) {
		this.id = id;
	}

	public SortedSet<OutageAreaRevision> getRevisions() {
		return revisions;
	}

	private void setRevisions(SortedSet<OutageAreaRevision> set) {
		this.revisions = set;
	}
}
