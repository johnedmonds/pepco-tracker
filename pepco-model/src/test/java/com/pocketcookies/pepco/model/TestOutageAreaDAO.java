package com.pocketcookies.pepco.model;

import java.sql.Timestamp;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.dao.OutageAreaDAO;

public class TestOutageAreaDAO extends TestCase {

	private SessionFactory sessionFactory;

    @Override
	public void setUp() {
		this.sessionFactory = SessionFactoryLoader.loadSessionFactory();
	}
    @Override public void tearDown(){
        this.sessionFactory.getCurrentSession().close();
        this.sessionFactory.close();
    }

	public void testUpdateArea() {
		final ParserRun now = new ParserRun(new Timestamp(0), new Timestamp(0));
		final ParserRun later = new ParserRun(new Timestamp(1), new Timestamp(1));
		final OutageAreaDAO dao = new OutageAreaDAO(sessionFactory);
		this.sessionFactory.getCurrentSession().beginTransaction();
		OutageArea a1 = new OutageArea("00000");
		this.sessionFactory.getCurrentSession().save(now);
		this.sessionFactory.getCurrentSession().save(later);
		final OutageAreaRevision r1 = new OutageAreaRevision(a1, 1, now);
		assertTrue(dao.updateArea(r1));
		final OutageAreaRevision r2 = new OutageAreaRevision(a1, 1, later);
		assertFalse(dao.updateArea(r2));
		assertEquals(1, a1.getRevisions().size());
		assertTrue(a1.getRevisions().contains(r1));
		assertFalse(a1.getRevisions().contains(r2));
	}
}
