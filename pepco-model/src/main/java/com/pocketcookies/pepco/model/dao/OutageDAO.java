package com.pocketcookies.pepco.model.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.Summary;

public class OutageDAO {
	private final SessionFactory sessionFactory;

	public OutageDAO(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	@SuppressWarnings("unchecked")
	public Outage getActiveOutage(final double lat, final double lon) {

		final List<Outage> outages;
		outages = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Outage where lat=:lat and lon=:lon and observedEnd=null")
				.setDouble("lat", lat).setDouble("lon", lon).list();
		if (outages.isEmpty())
			return null;
		return outages.get(0);
	}

	/**
	 * Gets summaries ordered by when they were generated occurring after [from]
	 * and before [to] inclusive.
	 * 
	 * Note that this function will not reorder [from] and [to]. If [to] is
	 * before [from], no results will be returned.
	 * 
	 * @param from
	 *            Only summaries generated after this date/time will be
	 *            returned.
	 * @param to
	 *            Only summaries before this date/time will be returned.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Summary> getSummaries(Timestamp from, Timestamp to) {
		// If the caller did not specify [from], we want to return all Summaries
		// from as early as possible. Thus, we pick a safe date, before which
		// there were no summaries. We started collecting data in 2011, so
		// picking 1970 is a safe (and easy) choice for [from].
		if (from == null)
			from = new Timestamp(0);
		// If the caller did not specify [to], we want to return all summaries
		// including the newest ones. Since there can be no summaries in the
		// future, we will pick now as a good default [to].
		if (to == null)
			to = new Timestamp(new Date().getTime());
		return this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Summary where whenGenerated >= :from and whenGenerated <= :to")
				.setTimestamp("from", from).setTimestamp("to", to).list();
	}
}
