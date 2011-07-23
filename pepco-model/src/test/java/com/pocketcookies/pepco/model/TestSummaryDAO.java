package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.pocketcookies.pepco.model.dao.SummaryDAO;

public class TestSummaryDAO extends TestCase {

	private SessionFactory sessionFactory;

	public void setUp() {
		this.sessionFactory = new Configuration().configure()
				.buildSessionFactory();
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
}
