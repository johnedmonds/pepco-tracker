package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.pocketcookies.pepco.model.dao.OutageDAO;

public class TestOutageDAO extends TestCase {
	private SessionFactory sessionFactory;

	public void setUp() {
		this.sessionFactory = new Configuration().configure()
				.buildSessionFactory();
	}

	public void testGetSummaries() {
		final OutageDAO dao = new OutageDAO(this.sessionFactory);
		// Set up data.
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		Summary s1 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(1), null);
		Summary s2 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(2), null);
		session.save(s1);
		session.save(s2);
		session.getTransaction().commit();
		session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		s1 = (Summary) session.load(Summary.class, s1.getId());
		s2 = (Summary) session.load(Summary.class, s2.getId());
		System.out.println(s1.getWhenGenerated().getTime());
		assertEquals(s2, dao.getSummaries(new Timestamp(2), new Timestamp(2))
				.get(0));
		assertEquals(s1, dao.getSummaries(new Timestamp(0), new Timestamp(1))
				.get(0));
		assertEquals(s1, dao.getSummaries(null, new Timestamp(1)).get(0));
		assertEquals(s1, dao.getSummaries(new Timestamp(1), null).get(0));
		assertEquals(2, dao.getSummaries(null, null).size());
		assertTrue(dao.getSummaries(null, null).contains(s1));
		assertTrue(dao.getSummaries(null, null).contains(s2));
	}
}
