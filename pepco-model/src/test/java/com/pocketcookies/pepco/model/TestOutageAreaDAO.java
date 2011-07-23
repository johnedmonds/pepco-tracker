package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.pocketcookies.pepco.model.dao.OutageAreaDAO;

public class TestOutageAreaDAO extends TestCase {

	private SessionFactory sessionFactory;

	public void setUp() {
		this.sessionFactory = new Configuration().configure()
				.buildSessionFactory();
	}

	public void testGetOrCreateOutageArea() {
		final OutageAreaDAO dao = new OutageAreaDAO(sessionFactory);
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		OutageArea a1 = new OutageArea("00000");
		session.save(a1);
		session.getTransaction().commit();
		session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		a1 = (OutageArea) session.load(a1.getClass(), a1.getId());
		assertEquals(a1, dao.getOrCreateArea(a1.getId()));
		assertEquals(1, session.createQuery("from OutageArea").list().size());
		dao.getOrCreateArea("00001");
		assertEquals(2, session.createQuery("from OutageArea").list().size());
	}

	public void testUpdateArea() {
		final ParserRun now = new ParserRun(new Timestamp(0));
		final ParserRun later = new ParserRun(new Timestamp(1));
		final OutageAreaDAO dao = new OutageAreaDAO(sessionFactory);
		this.sessionFactory.getCurrentSession().beginTransaction();
		OutageArea a1 = new OutageArea("00000");
		this.sessionFactory.getCurrentSession().save(now);
		this.sessionFactory.getCurrentSession().save(later);
		this.sessionFactory.getCurrentSession().save(a1);
		final OutageAreaRevision r1 = new OutageAreaRevision(a1, 1, now);
		assertTrue(dao.updateArea(r1));
		final OutageAreaRevision r2 = new OutageAreaRevision(a1, 1, later);
		assertFalse(dao.updateArea(r2));
		assertEquals(1, a1.getRevisions().size());
		assertTrue(a1.getRevisions().contains(r1));
		assertFalse(a1.getRevisions().contains(r2));
	}
}
