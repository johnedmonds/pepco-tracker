package com.pocketcookies.pepco.model.dao;

/**
 *
 * Holds data summarizing a parser run.
 *
 * This includes the number of new, updated, and closed outages.
 */
public class ParserRunSummary {

    public final int newOutages;
    public final int updatedOutages;
    public final int closedOutages;

    public ParserRunSummary(final int newOutages, final int updatedOutages, final int closedOutages) {
        this.newOutages = newOutages;
        this.updatedOutages = updatedOutages;
        this.closedOutages = closedOutages;
    }
}
