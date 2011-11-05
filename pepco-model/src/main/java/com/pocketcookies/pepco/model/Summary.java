package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Pepco provides summaries. This represents one summary.
 * 
 * @author jack
 * 
 */
@Entity
@Table(name="SUMMARY")
public class Summary {
    @Id
    @GeneratedValue
    @Column(name="ID")
	private int id;
	// The number of outages (not customers).
    @Column(name="TOTALOUTAGES")
	private int totalOutages;
        @Column(name="DCAFFECTEDCUSTOMERS")
	private int dcAffectedCustomers;
        @Column(name="DCTOTALCUSTOMERS")
	private int dcTotalCustomers;
        @Column(name="PGAFFECTEDCUSTOMERS")
	private int pgAffectedCustomers;
        @Column(name="PGTOTALCUSTOMERS")
	private int pgTotalCustomers;
	// Montgomery county
        @Column(name="MONTAFFECTEDCUSTOMERS")
	private int montAffectedCustomers;
        @Column(name="MONTTOTALCUSTOMERS")
	private int montTotalCustomers;
	// Pepco says when it was generated in the metadata.
        @Column(name="WHENGENERATED")
	private Timestamp whenGenerated;
        @ManyToOne(targetEntity=ParserRun.class)
        @JoinColumn(name="RUN")
	private ParserRun run;

	protected Summary() {
	}

	public Summary(int totalOutages, int dcAffectedCustomers,
			int dcTotalCustomers, int pgAffectedCustomers,
			int pgTotalCustomers, int montAffectedCustomers,
			int montTotalCustomers, Timestamp whenGenerated, ParserRun run) {
		super();
		setTotalOutages(totalOutages);
		setDcAffectedCustomers(dcAffectedCustomers);
		setDcTotalCustomers(dcTotalCustomers);
		setPgAffectedCustomers(pgAffectedCustomers);
		setPgTotalCustomers(pgTotalCustomers);
		setMontAffectedCustomers(montAffectedCustomers);
		setMontTotalCustomers(montTotalCustomers);
		setWhenGenerated(whenGenerated);
		setRun(run);
	}

	@SuppressWarnings("unused")
	private void setId(int id) {
		this.id = id;
	}

	private void setTotalOutages(int totalOutages) {
		this.totalOutages = totalOutages;
	}

	private void setDcAffectedCustomers(int dcAffectedCustomers) {
		this.dcAffectedCustomers = dcAffectedCustomers;
	}

	private void setDcTotalCustomers(int dcTotalCustomers) {
		this.dcTotalCustomers = dcTotalCustomers;
	}

	private void setPgAffectedCustomers(int pgAffectedCustomers) {
		this.pgAffectedCustomers = pgAffectedCustomers;
	}

	private void setPgTotalCustomers(int pgTotalCustomers) {
		this.pgTotalCustomers = pgTotalCustomers;
	}

	private void setMontAffectedCustomers(int montAffectedCustomers) {
		this.montAffectedCustomers = montAffectedCustomers;
	}

	private void setMontTotalCustomers(int montTotalCustomers) {
		this.montTotalCustomers = montTotalCustomers;
	}

	private void setWhenGenerated(Timestamp whenGenerated) {
		this.whenGenerated = whenGenerated;
	}

	private void setRun(ParserRun run) {
		this.run = run;
	}

	public int getId() {
		return id;
	}

	public int getTotalOutages() {
		return totalOutages;
	}

	public int getDcAffectedCustomers() {
		return dcAffectedCustomers;
	}

	public int getDcTotalCustomers() {
		return dcTotalCustomers;
	}

	public int getPgAffectedCustomers() {
		return pgAffectedCustomers;
	}

	public int getPgTotalCustomers() {
		return pgTotalCustomers;
	}

	public int getMontAffectedCustomers() {
		return montAffectedCustomers;
	}

	public int getMontTotalCustomers() {
		return montTotalCustomers;
	}

	public Timestamp getWhenGenerated() {
		return whenGenerated;
	}

	public ParserRun getRun() {
		return run;
	}

}
