package com.pocketcookies.pepco.model.dao;

import com.pocketcookies.pepco.model.ParserRun;

/**
 *
 * Holds data summarizing a parser run.
 *
 * Essentially, this includes the number of new, updated, and closed outages
 * occurring between two ParserRuns.
 */
public class ParserRunSummary {

    public final int newOutages;
    public final int updatedOutages;
    public final int closedOutages;
    public final ParserRun run;

    public ParserRunSummary(final int newOutages, final int updatedOutages, final int closedOutages, final ParserRun run) {
        this.newOutages = newOutages;
        this.updatedOutages = updatedOutages;
        this.closedOutages = closedOutages;
        this.run = run;
    }
}
