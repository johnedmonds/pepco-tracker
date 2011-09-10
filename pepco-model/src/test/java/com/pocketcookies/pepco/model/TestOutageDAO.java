package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.Arrays;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.pocketcookies.pepco.model.dao.OutageDAO;

public class TestOutageDAO extends TestCase {
	private SessionFactory sessionFactory;

	public void setUp() {
		this.sessionFactory = new Configuration().configure()
				.buildSessionFactory();
	}

	public void testUpdateOutage() {
		this.sessionFactory.getCurrentSession().beginTransaction();
		final OutageDAO outageDao = new OutageDAO(this.sessionFactory);
		// Saved
		final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
		// Gets saved
		final OutageClusterRevision r1 = new OutageClusterRevision(1,
				new Timestamp(1), new Timestamp(1), o1, null, 1);
		// Gets saved
		final OutageClusterRevision r2 = new OutageClusterRevision(2,
				new Timestamp(2), new Timestamp(1), o1, null, 1);
		// Not saved
		final OutageClusterRevision r3 = new OutageClusterRevision(1,
				new Timestamp(1), new Timestamp(1), o1, null, 1);
		outageDao.updateOutage(r1);
		assertEquals(
				1,
				this.sessionFactory.getCurrentSession()
						.createQuery("from Outage").list().size());
		assertEquals(
				1,
				this.sessionFactory.getCurrentSession()
						.createQuery("from AbstractOutageRevision").list()
						.size());
		outageDao.updateOutage(r2);
		assertEquals(
				1,
				this.sessionFactory.getCurrentSession()
						.createQuery("from Outage").list().size());
		assertEquals(
				2,
				this.sessionFactory.getCurrentSession()
						.createQuery("from AbstractOutageRevision").list()
						.size());
		outageDao.updateOutage(r3);
		assertEquals(
				1,
				this.sessionFactory.getCurrentSession()
						.createQuery("from Outage").list().size());
		assertEquals(
				2,
				this.sessionFactory.getCurrentSession()
						.createQuery("from AbstractOutageRevision").list()
						.size());
	}

	public void testCloseMissingOutages() {
		final OutageDAO outageDao = new OutageDAO(sessionFactory);
		this.sessionFactory.getCurrentSession().beginTransaction();
		final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
		final Outage o2 = new Outage(2, 2, new Timestamp(1), null);
		final Outage o3 = new Outage(3, 3, new Timestamp(1), new Timestamp(2));
		this.sessionFactory.getCurrentSession().save(o1);
		this.sessionFactory.getCurrentSession().save(o2);
		this.sessionFactory.getCurrentSession().save(o3);
		this.sessionFactory.getCurrentSession().flush();
		outageDao.closeMissingOutages(
				Arrays.asList(new Integer[] { o1.getId() }), new Timestamp(3));
		this.sessionFactory.getCurrentSession().flush();
		this.sessionFactory.getCurrentSession().refresh(o1);
		this.sessionFactory.getCurrentSession().refresh(o2);
		this.sessionFactory.getCurrentSession().refresh(o3);
		assertNull(o1.getObservedEnd());
		assertEquals(3, o2.getObservedEnd().getTime());
		assertEquals(2, o3.getObservedEnd().getTime());
	}

	public void testUpdateNullExpectedRestoration() {
		this.sessionFactory.getCurrentSession().beginTransaction();
		final OutageDAO dao = new OutageDAO(sessionFactory);
		final Outage o1 = new Outage(1, 1, new Timestamp(1), null);
		final OutageRevision or1 = new OutageRevision(1, null,
				new Timestamp(1), o1, null, null, null);
		final OutageRevision or2 = new OutageRevision(2, null,
				new Timestamp(2), o1, null, null, null);
		dao.updateOutage(or1);
		final OutageRevision retrievedor1 = (OutageRevision) this.sessionFactory
				.getCurrentSession().createQuery("from AbstractOutageRevision")
				.list().get(0);
		assertEquals(1, retrievedor1.getNumCustomersAffected());
		assertEquals(null, retrievedor1.getEstimatedRestoration());
		dao.updateOutage(or2);
		final OutageRevision retrievedor2 = (OutageRevision) this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from AbstractOutageRevision order by observationDate desc")
				.list().get(0);
		assertEquals(2, retrievedor2.getNumCustomersAffected());
		assertEquals(null, retrievedor2.getEstimatedRestoration());
	}
}
