package com.pocketcookies.pepco.model.dao;

import java.util.List;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;

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
