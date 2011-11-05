package com.pocketcookies.pepco.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="OUTAGEAREAREVISION")
public class OutageAreaRevision implements Comparable<OutageAreaRevision> {
	private int id;
	private OutageArea area;
	private int customersOut;
	// Associates us with a parser run. We also get the time at which the page
	// was requested for free.
	private ParserRun parserRun;

	public OutageAreaRevision(OutageArea area, int customersOut,
			final ParserRun parserRun) {
		super();
		setArea(area);
		setCustomersOut(customersOut);
		setParserRun(parserRun);
	}

	public OutageAreaRevision(final int id, final OutageArea area,
			final int customersOut, final ParserRun parserRun) {
		setId(id);
		setArea(area);
		setCustomersOut(customersOut);
		setParserRun(parserRun);
	}

	public OutageAreaRevision() {
	}

        @Id
        @GeneratedValue
        @Column(name="ID")
	public int getId() {
		return id;
	}

        @ManyToOne
        @JoinColumn(name="AREA")
	public OutageArea getArea() {
		return area;
	}

        @Column(name="CUSTOMERSOUT")
	public int getCustomersOut() {
		return customersOut;
	}

	private void setId(int id) {
		this.id = id;
	}

	public void setArea(OutageArea area) {
		this.area = area;
	}

	private void setCustomersOut(int customersOut) {
		this.customersOut = customersOut;
	}

        @ManyToOne
        @JoinColumn(name="PARSERRUN")
	public ParserRun getParserRun() {
		return parserRun;
	}

	private void setParserRun(ParserRun parserRun) {
		this.parserRun = parserRun;
	}

	@Override
	public int compareTo(OutageAreaRevision o) {
		if (this.getParserRun().getRunTime().getTime() < o.getParserRun()
				.getRunTime().getTime())
			return -1;
		else if (this.getParserRun().getRunTime().getTime() > o.getParserRun()
				.getRunTime().getTime())
			return 1;
		else
			// We only use the times for sorting. Since we're actually putting
			// this stuff in a TreeSet, we don't want to accidentally lose
			// entries just because they occur at the same time. Thus, we
			// compare by the id to check whether they are truly equal.
			return this.getId() - o.getId();
	}

	public boolean equalsIgnoreTime(final OutageAreaRevision r) {
		return this.getCustomersOut() == r.getCustomersOut();
	}

}
