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
        final Outage o = new Outage(1, 1, new Timestamp(1), null);
        final OutageRevision or = new OutageRevision(0, new Timestamp(1), o, pr, "cause", OutageRevision.CrewStatus.PENDING);

        this.sessionFactory.getCurrentSession().save(pr);
        this.sessionFactory.getCurrentSession().save(o);
        this.sessionFactory.getCurrentSession().save(or);

        final ParserRunSummary summary = dao.getParserRunSummary(pr);
        assertEquals(1, summary.newOutages);
        assertEquals(0, summary.closedOutages);
        assertEquals(0, summary.updatedOutages);
    }

    /**
     * Tests that we record only one outage as being updated for a certain ParserRun.
     * Also incidentally tests that outages occurring after the ParserRun are not take into account when determining how many outages of each there are.
     */
    public void testOneUpdatedOutage() {
        this.sessionFactory.getCurrentSession().beginTransaction();
        final OutageDAO dao = new OutageDAO(this.sessionFactory);
        final ParserRun pr1 = new ParserRun(new Timestamp(1), new Timestamp(1));
        final ParserRun pr2 = new ParserRun(new Timestamp(2), new Timestamp(2));
        final Outage o = new Outage(1, 1, new Timestamp(1), null);
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
        assertEquals(0, summary1.closedOutages);
        assertEquals(1, summary1.updatedOutages);
        //Now check that as of pr1, the outage was new.
        final ParserRunSummary summary2 = dao.getParserRunSummary(pr1);
        assertEquals(1, summary2.newOutages);
        assertEquals(0, summary2.closedOutages);
        assertEquals(0, summary2.updatedOutages);
    }
    
    /**
     * Tests that we properly return 2 of each: open outages, closed outages, and updated outages.
     */
    public void testTwoOfEach(){
        this.sessionFactory.getCurrentSession().beginTransaction();
        final OutageDAO dao = new OutageDAO(sessionFactory);
        
        final ParserRun pr1=new ParserRun(new Timestamp(1), new Timestamp(1));
        final ParserRun pr2=new ParserRun(new Timestamp(2), new Timestamp(2));
        final Outage new1=new Outage(1, 1, new Timestamp(1), null);
        final Outage new2=new Outage(2,2, new Timestamp(1), null);
        final Outage updated1=new Outage(3,3, new Timestamp(1), null);
        final Outage updated2=new Outage(4,4, new Timestamp(1), null);
        final Outage closed1=new Outage(5,5, new Timestamp(1), new Timestamp(2));
        final Outage closed2=new Outage(6, 6, new Timestamp(1), new Timestamp(2));
        final OutageRevision ornew1=new OutageRevision(1, new Timestamp(10), new1, pr2, "cause", OutageRevision.CrewStatus.PENDING);
        final OutageRevision ornew2=new OutageRevision(1, new Timestamp(10), new2, pr2, "cause", OutageRevision.CrewStatus.PENDING);
        final OutageRevision orupdated11=new OutageRevision(1, new Timestamp(10), updated1, pr1, "cause", OutageRevision.CrewStatus.PENDING);
        final OutageRevision orupdated12=new OutageRevision(2, new Timestamp(10), updated1, pr2, "cause", OutageRevision.CrewStatus.PENDING);
        final OutageRevision orupdated21=new OutageRevision(1, new Timestamp(10), updated2, pr1, "cause", OutageRevision.CrewStatus.PENDING);
        final OutageRevision orupdated22=new OutageRevision(2, new Timestamp(10), updated2, pr2, "cause", OutageRevision.CrewStatus.PENDING);
        this.sessionFactory.getCurrentSession().save(pr1);
        this.sessionFactory.getCurrentSession().save(pr2);
        this.sessionFactory.getCurrentSession().save(new1);
        this.sessionFactory.getCurrentSession().save(new2);
        this.sessionFactory.getCurrentSession().save(updated1);
        this.sessionFactory.getCurrentSession().save(updated2);
        this.sessionFactory.getCurrentSession().save(closed1);
        this.sessionFactory.getCurrentSession().save(closed2);
        this.sessionFactory.getCurrentSession().save(orupdated11);
        this.sessionFactory.getCurrentSession().save(orupdated12);
        this.sessionFactory.getCurrentSession().save(orupdated21);
        this.sessionFactory.getCurrentSession().save(orupdated22);
        this.sessionFactory.getCurrentSession().save(ornew1);
        this.sessionFactory.getCurrentSession().save(ornew2);
        
        final ParserRunSummary summary1=dao.getParserRunSummary(pr2);
        assertEquals(2, summary1.newOutages);
        assertEquals(2, summary1.updatedOutages);
        assertEquals(2, summary1.closedOutages);
        
        final ParserRunSummary summary2=dao.getParserRunSummary(pr1);
        assertEquals(2, summary2.newOutages);
        assertEquals(0, summary2.updatedOutages);
        assertEquals(0, summary2.closedOutages);
    }
}
