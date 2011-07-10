package com.pocketcookies.pepco.model;

public class OutageClusterRevision {
	private int id;
	// The number of outages associated with this cluster. We don't currently
	// have a way of tracking exactly which outages correspond with this
	// cluster so for now we just keep track of the count.
	private int numOutages;
	private OutageCluster cluster;

	public OutageClusterRevision() {
		super();
	}

	public OutageClusterRevision(int id, int numOutages,
			final OutageCluster cluster) {
		this();
		setId(id);
		setNumOutages(numOutages);
		setCluster(cluster);
	}

	public int getNumOutages() {
		return numOutages;
	}

	private void setNumOutages(int numOutages) {
		this.numOutages = numOutages;
	}

	public OutageCluster getCluster() {
		return cluster;
	}

	private void setCluster(OutageCluster cluster) {
		this.cluster = cluster;
	}

	public int getId() {
		return id;
	}

	private void setId(int id) {
		this.id = id;
	}
}
