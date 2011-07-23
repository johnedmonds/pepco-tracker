package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

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
}
