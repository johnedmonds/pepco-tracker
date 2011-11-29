package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="PARSERRUN")
public class ParserRun {
	private int id;
        /**
         * The time at which the parser was actually run.
         */
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

        @Id
        @GeneratedValue
        @Column(name="ID")
	public int getId() {
		return id;
	}

        @Column(name="RUNTIME")
	public Timestamp getRunTime() {
		return runTime;
	}

        @OneToMany(targetEntity=AbstractOutageRevision.class,mappedBy="run")
	public Collection<AbstractOutageRevision> getOutageRevisions() {
		return outageRevisions;
	}

        @OneToMany(targetEntity=OutageAreaRevision.class,mappedBy="parserRun")
	public Collection<OutageAreaRevision> getAreaRevisions() {
		return areaRevisions;
	}

}
