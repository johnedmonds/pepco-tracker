package com.pocketcookies.pepco.model.dao;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.OutageArea;
import com.pocketcookies.pepco.model.OutageAreaRevision;

public class OutageAreaDAO {
	private final SessionFactory sessionFactory;

	public OutageAreaDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Used by CGLIB.
	 */
    protected OutageAreaDAO() {
        this.sessionFactory = null;
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
		final OutageArea existingArea = (OutageArea) this.sessionFactory
				.getCurrentSession().get(OutageArea.class,
						revision.getArea().getId());
		if (existingArea == null) {
			this.sessionFactory.getCurrentSession().save(revision.getArea());
			this.sessionFactory.getCurrentSession().save(revision);
			return true;
		} else {
			revision.setArea(existingArea);
			// Usually, we don't add the revision to the area but the
			// association
			// still exists in the database. We need to reload to check whether
			// the
			// passed revision has already been added to this area.
			this.sessionFactory.getCurrentSession().refresh(revision.getArea());

			if (revision.getArea().getRevisions().first()
					.equalsIgnoreTime(revision))
				return false;

			this.sessionFactory.getCurrentSession().save(revision);
			return true;
		}
	}
}
