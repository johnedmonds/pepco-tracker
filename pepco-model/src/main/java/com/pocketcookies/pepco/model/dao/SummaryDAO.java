package com.pocketcookies.pepco.model.dao;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.Summary;

public class SummaryDAO {

	private final SessionFactory sessionFactory;

	public SummaryDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Used by CGLIB.
	 */
    @SuppressWarnings("unused")
    protected SummaryDAO() {
        this.sessionFactory = null;
    }

	public void saveSummary(final Summary summary) {
		this.sessionFactory.getCurrentSession().save(summary);
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

	public Summary getMostRecentSummary() {
		@SuppressWarnings("unchecked")
		final List<Summary> ret = (List<Summary>) this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Summary where whenGenerated = (select max(whenGenerated) from Summary)")
				.list();
		assert ret.size() <= 1;
		if (ret.size() == 0)
			return null;
		return ret.get(0);
	}

}
