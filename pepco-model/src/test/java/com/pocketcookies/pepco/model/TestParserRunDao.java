package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import junit.framework.TestCase;

import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.dao.ParserRunDao;

public class TestParserRunDao extends TestCase {
    private SessionFactory sessionFactory;

    @Override
    public void setUp() {
        this.sessionFactory = SessionFactoryLoader.loadSessionFactory();
    }

    @Override
    public void tearDown() {
        this.sessionFactory.getCurrentSession().close();
        this.sessionFactory.close();
    }

    public void testSaveParserRun() {
        ParserRun run = new ParserRun(new Timestamp(1), new Timestamp(1));
        ParserRunDao dao = new ParserRunDao(sessionFactory);
        sessionFactory.getCurrentSession().getTransaction().begin();
        dao.saveParserRun(run);
        sessionFactory.getCurrentSession().getTransaction().commit();
        
        sessionFactory.getCurrentSession().getTransaction().begin();
        assertEquals(run, sessionFactory.getCurrentSession().get(ParserRun.class, run.getId()));
    }
}
