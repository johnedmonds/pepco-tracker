package com.pocketcookies.pepco.model;

import com.pocketcookies.pepco.model.dao.OutageDAO;
import com.pocketcookies.pepco.model.dao.ParserRunSummary;
import java.sql.Timestamp;
import junit.framework.TestCase;
import org.hibernate.SessionFactory;

/**
 * Tests that we properly retrieve the number of opened and closed outages.
 *
 * @author John Edmonds
 */
public class TestParserRunSummary extends TestCase {

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

    public void testOneNewOutage() {
        this.sessionFactory.getCurrentSession().beginTransaction();
        final OutageDAO dao = new OutageDAO(this.sessionFactory);
        final ParserRun pr = new ParserRun(new Timestamp(1), new Timestamp(1));
        final Outage o = new Outage(1, 1, new Timestamp(1), new Timestamp(1));
        final OutageRevision or = new OutageRevision(0, new Timestamp(1), o, pr, "cause", OutageRevision.CrewStatus.PENDING);

        this.sessionFactory.getCurrentSession().save(pr);
        this.sessionFactory.getCurrentSession().save(o);
        this.sessionFactory.getCurrentSession().save(or);

        final ParserRunSummary summary = dao.getParserRunSummary(pr);
        assertEquals(1, summary.newOutages);
        assertEquals(-1, summary.closedOutages);
        assertEquals(0, summary.updatedOutages);
    }

    public void testOneUpdatedOutage() {
        this.sessionFactory.getCurrentSession().beginTransaction();
        final OutageDAO dao = new OutageDAO(this.sessionFactory);
        final ParserRun pr1 = new ParserRun(new Timestamp(1), new Timestamp(1));
        final ParserRun pr2 = new ParserRun(new Timestamp(2), new Timestamp(2));
        final Outage o = new Outage(1, 1, new Timestamp(1), new Timestamp(1));
        final OutageRevision or1 = new OutageRevision(0, new Timestamp(1), o, pr1, "cause", OutageRevision.CrewStatus.PENDING);
        final OutageRevision or2 = new OutageRevision(0, new Timestamp(1), o, pr2, "cause2", OutageRevision.CrewStatus.PENDING);

        this.sessionFactory.getCurrentSession().save(pr1);
        this.sessionFactory.getCurrentSession().save(pr2);
        this.sessionFactory.getCurrentSession().save(o);
        this.sessionFactory.getCurrentSession().save(or1);
        this.sessionFactory.getCurrentSession().save(or2);

        //Check that as of pr2, the outage has been updated.
        final ParserRunSummary summary1 = dao.getParserRunSummary(pr2);
        assertEquals(0, summary1.newOutages);
        assertEquals(-1, summary1.closedOutages);
        assertEquals(1, summary1.updatedOutages);
        //Now check that as of pr1, the outage was new.
        final ParserRunSummary summary2 = dao.getParserRunSummary(pr1);
        assertEquals(1, summary2.newOutages);
        assertEquals(-1, summary2.closedOutages);
        assertEquals(0, summary2.updatedOutages);
    }
}
