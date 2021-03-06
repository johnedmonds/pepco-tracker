package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.dao.SummaryDAO;

public class TestSummaryDAO extends TestCase {

	private SessionFactory sessionFactory;

    @Override
	public void setUp() {
		this.sessionFactory = SessionFactoryLoader.loadSessionFactory();
	}
    @Override public void tearDown(){
        this.sessionFactory.getCurrentSession().close();
        this.sessionFactory.close();
    }

	public void testSaveSummary() {
		final SummaryDAO summaryDao = new SummaryDAO(this.sessionFactory);
		final Summary s = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(2),
				null);
		this.sessionFactory.getCurrentSession().beginTransaction();
		summaryDao.saveSummary(s);
		assertEquals(
				s,
				this.sessionFactory.getCurrentSession()
						.createQuery("from Summary").list().get(0));
		assertEquals(
				1L,
				this.sessionFactory.getCurrentSession()
						.createQuery("select count(*) from Summary").list()
						.get(0));
	}

	public void testGetSummaries() {
		final SummaryDAO dao = new SummaryDAO(this.sessionFactory);
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
		assertEquals(s2,
				dao.getSummaries(new Timestamp(2), new Timestamp(2), true, 0)
						.get(0));
		assertEquals(s1,
				dao.getSummaries(new Timestamp(0), new Timestamp(1), true, 0)
						.get(0));
		assertEquals(s1,
				dao.getSummaries(null, new Timestamp(1), true, 0).get(0));
		assertEquals(s2,
				dao.getSummaries(new Timestamp(1), null, true, 0).get(0));
		assertEquals(2, dao.getSummaries(null, null, true, 0).size());
		assertTrue(dao.getSummaries(null, null, true, 0).contains(s1));
		assertTrue(dao.getSummaries(null, null, true, 0).contains(s2));
	}

	public void testSummaryLimit() {
		final SummaryDAO dao = new SummaryDAO(this.sessionFactory);
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		Summary s1 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(1), null);
		Summary s2 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(2), null);
		Summary s3 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(3), null);
		session.save(s1);
		session.save(s2);
		session.save(s3);

		assertEquals(2, dao.getSummaries(null, null, true, 2).size());
	}

	public void testSummaryOrder() {
		final SummaryDAO dao = new SummaryDAO(this.sessionFactory);
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		Summary s1 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(1), null);
		Summary s2 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(2), null);
		Summary s3 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(3), null);
		session.save(s1);
		session.save(s2);
		session.save(s3);
		session.getTransaction().commit();
		session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		s1 = (Summary) session.load(Summary.class, s1.getId());
		s2 = (Summary) session.load(Summary.class, s2.getId());
		s3 = (Summary) session.load(Summary.class, s3.getId());

		assertEquals(s3, dao.getSummaries(null, null, true, 0).get(0));
		assertEquals(s1, dao.getSummaries(null, null, true, 0).get(2));
		assertEquals(s1, dao.getSummaries(null, null, false, 0).get(0));
		assertEquals(s3, dao.getSummaries(null, null, false, 0).get(2));
	}

	public void testGetFirstSummary() {
		final SummaryDAO dao = new SummaryDAO(this.sessionFactory);
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		Summary s1 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(1), null);
		Summary s2 = new Summary(1, 1, 1, 1, 1, 1, 1, new Timestamp(2), null);
		assertEquals(null, dao.getMostRecentSummary());
		session.save(s1);
		assertEquals(s1, dao.getMostRecentSummary());
		session.save(s2);
		assertEquals(s2, dao.getMostRecentSummary());
	}
}
