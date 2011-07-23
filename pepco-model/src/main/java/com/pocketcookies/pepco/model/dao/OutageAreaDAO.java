package com.pocketcookies.pepco.model.dao;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.OutageArea;
import com.pocketcookies.pepco.model.OutageAreaRevision;

public class OutageAreaDAO {
	private SessionFactory sessionFactory;

	public OutageAreaDAO(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
	}

	protected OutageAreaDAO() {
	}

	// Used by spring.
	@SuppressWarnings("unused")
	private void setSessionFactory(final SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * If the outage area with the given zip code(s) exists, we retrieve and
	 * return that OutageArea. Otherwise, we create a new OutageArea and persist
	 * it, then return that area.
	 * 
	 * @param zip
	 *            The zip code(s) to use when searching for an OutageArea.
	 * @return The outage area with the given zip code or a new outage area.
	 */
	public OutageArea getOrCreateArea(final String zip) {
		final OutageArea existing = (OutageArea) sessionFactory
				.getCurrentSession().get(OutageArea.class, zip);
		if (existing == null) {
			final OutageArea ret = new OutageArea(zip);
			this.sessionFactory.getCurrentSession().save(ret);
			return ret;
		}
		return existing;
	}

	/**
	 * Updates the area referred to by revision.getArea() with the given
	 * revision. If the statistics for the area have not changed, nothing will
	 * be written to the database and this method will return false.
	 * 
	 * If the statistics in the revision have changed, this method will add the
	 * new revision and return true.
	 * 
	 * @param revision
	 * @return True if the revision is new and the database has been update
	 *         successfully, false otherwise.
	 */
	public boolean updateArea(final OutageAreaRevision revision) {
		// Hibernate doesn't seem to immediately write rows to the database so
		// that when we call refresh(), we may get an exception if the
		// OutageArea has not been written to the database. This forces the
		// OutageArea to get written to the database so we can refresh it later.
		this.sessionFactory.getCurrentSession().flush();
		// Usually, we don't add the revision to the area but the association
		// still exists in the database. We need to reload to check whether the
		// passed revision has already been added to this area.
		this.sessionFactory.getCurrentSession().refresh(revision.getArea());
		if (revision.getArea().getRevisions().isEmpty()
				|| !revision.getArea().getRevisions().first()
						.equalsIgnoreTime(revision)) {
			this.sessionFactory.getCurrentSession().save(revision);
			return true;
		}
		return false;
	}
}
