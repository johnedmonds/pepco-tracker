package com.pocketcookies.pepco.model.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.Summary;

public class OutageDAO {
	private SessionFactory sessionFactory;

	public OutageDAO(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	protected OutageDAO() {
	}

	// Used by Spring
	@SuppressWarnings("unused")
	private void setSessionFactory(final SessionFactory sessionFactory) {
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
	 * @param desc
	 *            If true, the returned list at position 0 will have the most
	 *            recent summary. If false, the item at position list.length()-1
	 *            will have the most recent summary.
	 * @param limit
	 *            The number of summaries to return. If limit <= 0, there is no
	 *            limit.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Summary> getSummaries(Timestamp from, Timestamp to,
			boolean desc, int limit) {
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
		final Query q = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Summary where whenGenerated >= :from and whenGenerated <= :to order by whenGenerated "
								+ (desc ? "desc" : ""))
				.setTimestamp("from", from).setTimestamp("to", to);
		if (limit > 0)
			q.setMaxResults(limit);
		return q.list();
	}

	/**
	 * If the revision's outage is not already in the database, we save the
	 * outage. Otherwise, we update revision's outage with the outage from the
	 * database. If the revision is different from the most recent revision for
	 * the outage, we save the revision and return true. Otherwise, we do
	 * nothing and return false.
	 * 
	 * @param revision
	 * @return True if the revision is new and is successfully added to the
	 *         database, false otherwise.
	 */
	public boolean updateOutage(final AbstractOutageRevision revision) {
		this.sessionFactory.getCurrentSession().flush();
		final Outage existingOutage = getActiveOutage(revision.getOutage()
				.getLat(), revision.getOutage().getLon());
		if (!revision.getOutage().equals(existingOutage)) {
			this.sessionFactory.getCurrentSession().save(revision.getOutage());
			this.sessionFactory.getCurrentSession().save(revision);
			return true;
		} else {
			// Sometimes we load from the database and Hibernate pulls from its
			// cache. We rarely add outage revisions to the list of outages so
			// this could very well be out of date. We need the most up-to-date
			// version for when we are checking whether the revision is
			// different from the current revision.
			this.sessionFactory.getCurrentSession().refresh(existingOutage);
			revision.setOutage(existingOutage);
			if (revision.getOutage().getRevisions().get(0)
					.equalsIgnoreObservationDate(revision))
				return false;
			this.sessionFactory.getCurrentSession().save(revision);
			return true;
		}

	}
}
