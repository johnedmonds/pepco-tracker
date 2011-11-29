package com.pocketcookies.pepco.model;

import java.io.Serializable;
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
@Table(name = "PARSERRUN")
public class ParserRun implements Serializable, Comparable<ParserRun> {

    private int id;
    /**
     * The time at which the parser was actually run.
     */
    private Timestamp runTime;
    /**
     * Pepco provides a what seems to be a way to request the state of the
     * outage map as-of a certain time.  This is the "As-of" time we requested.
     */
    private Timestamp asof;
    private Collection<AbstractOutageRevision> outageRevisions;
    private Collection<OutageAreaRevision> areaRevisions;

    public ParserRun(final Timestamp runTime, final Timestamp asof) {
        setRunTime(runTime);
        setAsof(asof);
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

    private void setAsof(final Timestamp asof) {
        this.asof = asof;
    }

    @Id
    @GeneratedValue
    @Column(name = "ID")
    public int getId() {
        return id;
    }

    @Column(name = "RUNTIME")
    public Timestamp getRunTime() {
        return runTime;
    }

    @OneToMany(targetEntity = AbstractOutageRevision.class, mappedBy = "run")
    public Collection<AbstractOutageRevision> getOutageRevisions() {
        return outageRevisions;
    }

    @OneToMany(targetEntity = OutageAreaRevision.class, mappedBy = "parserRun")
    public Collection<OutageAreaRevision> getAreaRevisions() {
        return areaRevisions;
    }

    @Column(name = "ASOF")
    public Timestamp getAsof() {
        return this.asof;
    }
    
    @Override public boolean equals(final Object o){
	return o instanceof ParserRun && ((ParserRun)o).getAsof().equals(getAsof());
    }
    @Override public int hashCode(){return (int) getAsof().getTime();}

    @Override
    public int compareTo(ParserRun o) {
	return getAsof().compareTo(o.getAsof());
    }
}
