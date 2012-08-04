package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.dao.OutageDAO;
import java.util.Arrays;

public class TestOutage extends TestCase {

    private SessionFactory sessionFactory;

    @Override
    public void setUp() {
	this.sessionFactory = SessionFactoryLoader.loadSessionFactory();
    }
    
    @Override
    public void tearDown(){
        this.sessionFactory.getCurrentSession().close();
        this.sessionFactory.close();
    }

    /**
     * Make sure we can insert and retrieve both types of AbstractOutageRevision.
     */
    public void testOutageRevisionDiscriminator() {
	Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	final Timestamp now = new Timestamp(new Date().getTime());
	Outage outage = new Outage(1, 1, now, now);
	OutageRevision r1 = new OutageRevision(1, now, outage, new ParserRun(now, now), "test", CrewStatus.PENDING);
	OutageClusterRevision r2 = new OutageClusterRevision(1, now, outage, new ParserRun(now, now), 1);
	session.save(outage);
	session.save(r1.getRun());
	session.save(r2.getRun());
	session.save(r1);
	session.save(r2);
	session.getTransaction().commit();
	session = sessionFactory.getCurrentSession();
	session.beginTransaction();
	final List<AbstractOutageRevision> revisions = session.createQuery(
		"from AbstractOutageRevision order by id").list();
	assertTrue(revisions.get(0) instanceof OutageRevision);
	assertTrue(revisions.get(1) instanceof OutageClusterRevision);
	outage = (Outage) session.load(Outage.class, outage.getId());
	assertTrue(outage.getRevisions().contains(revisions.get(0)));
	assertTrue(outage.getRevisions().contains(revisions.get(1)));
    }

    public void testRevisions() {
	Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	Timestamp now = new Timestamp(new Date().getTime());
	final ParserRun nowRun = new ParserRun(now, now);
	Outage o1 = new Outage(1, 1, now, now);
	Outage o2 = new Outage(2, 2, now, now);

	session.save(o1);
	session.save(o2);

	OutageRevision or1 = new OutageRevision(1, now, o1, nowRun, "test", CrewStatus.PENDING);
	OutageRevision or2 = new OutageRevision(2, now, o2, nowRun, "test", CrewStatus.PENDING);
	OutageClusterRevision cr1 = new OutageClusterRevision(1, now, o1, nowRun, 1);
	OutageClusterRevision cr2 = new OutageClusterRevision(2, now, o2, nowRun, 2);

	session.save(nowRun);
	session.save(or1);
	session.save(or2);
	session.save(cr1);
	session.save(cr2);

	session.getTransaction().commit();
	session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();

	o1 = (Outage) session.load(Outage.class, o1.getId());
	o2 = (Outage) session.load(Outage.class, o2.getId());
	or1 = (OutageRevision) session.load(OutageRevision.class, or1.getId());
	or2 = (OutageRevision) session.load(OutageRevision.class, or2.getId());
	cr1 = (OutageClusterRevision) session.load(OutageClusterRevision.class,
		cr1.getId());
	cr2 = (OutageClusterRevision) session.load(OutageClusterRevision.class,
		cr2.getId());
	assertEquals(2, o1.getRevisions().size());
	assertEquals(2, o2.getRevisions().size());
	assertEquals(o1, or1.getOutage());
	assertEquals(o2, or2.getOutage());
	assertEquals(o1, cr1.getOutage());
	assertEquals(o2, cr2.getOutage());
	assertTrue(o1.getRevisions().contains(or1));
	assertTrue(o1.getRevisions().contains(cr1));
	assertTrue(o2.getRevisions().contains(or2));
	assertTrue(o2.getRevisions().contains(cr2));
    }

    public void testActiveOutage() {
	final OutageDAO dao = new OutageDAO(sessionFactory);
	final Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();

	assertNull(dao.getActiveOutage(1, 1));

	final Timestamp now = new Timestamp(new Date().getTime());

	session.save(new Outage(1, 1, now, now));

	assertNull(dao.getActiveOutage(1, 1));

	final Outage outage = new Outage(1, 1, now, null);
	session.save(outage);

	assertEquals(outage,
		dao.getActiveOutage(outage.getLat(), outage.getLon()));

    }

    public void testOutageEquals() {
	final Timestamp now = new Timestamp(new Date().getTime());
	final Timestamp now2 = new Timestamp(now.getTime());
	assertEquals(new Outage(1, 1, now, now), new Outage(1, 1, now2, now2));
	// Make sure we don't crash due to nulls
	// Note that we don't need to test some columns being null because we
	// set not-null=true on them in Hibernate. I know this doesn't truly
	// prevent them from being null but I believe it is acceptable to crash
	// hard if these expectations are broken.
	assertEquals(new Outage(1, 1, now, null), new Outage(1, 1, now, null));
	assertFalse(new Outage(1, 1, now, null).equals(new Outage(1, 1, now,
		now)));
	assertFalse(new Outage(1, 1, now, now).equals(new Outage(1, 1, now,
		null)));
    }

    public void testRevisionEquals() {
	final Timestamp now = new Timestamp(new Date().getTime());
	final Timestamp then = new Timestamp(now.getTime() + 1);
	final Timestamp now2 = new Timestamp(now.getTime());
	final OutageRevision or1 = new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED);
	final OutageRevision or2 = new OutageRevision(1, now2, null, new ParserRun(now2, now2), "test", CrewStatus.ASSIGNED);
	final OutageClusterRevision cr1 = new OutageClusterRevision(1, now, null, new ParserRun(now, now), 1);
	final OutageClusterRevision cr2 = new OutageClusterRevision(1, now2, null, new ParserRun(now2, now2), 1);
	assertEquals(or1, or2);
	assertEquals(cr1, cr2);
	assertEquals(or1.hashCode(), or2.hashCode());
	assertEquals(cr1.hashCode(), cr2.hashCode());

	//Test observation date is used in equality check.
	assertFalse(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.PENDING).equals(
		new OutageRevision(1, now, null, new ParserRun(then, then), "test", CrewStatus.PENDING)));
	//Test observation date is ignored in equalsIgnoreObservationDate.
	assertTrue(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.PENDING).equalsIgnoreRun(
		new OutageRevision(1, now, null, new ParserRun(then, then), "test", CrewStatus.PENDING)));

	//customersAffected causes revisions to be not equal.
	assertFalse(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED).equals(
		new OutageRevision(2, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED)));
	//observationDate and estimatedRestoration cause revisions to be unequal.
	assertFalse(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED).equals(
		new OutageRevision(1, then, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED)));
	assertFalse(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED).equals(
		new OutageRevision(1, now, null, new ParserRun(then, then), "test", CrewStatus.ASSIGNED)));
	//Different causes cause inequality.
	assertFalse(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED).equals(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test2", CrewStatus.ASSIGNED)));
	//Different statuses cause inequality.
	assertFalse(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.ASSIGNED).equals(
		new OutageRevision(1, now, null, new ParserRun(now, now), "test", CrewStatus.PENDING)));

	//Testing clusters.

	//customersAffected is included in equality check.
	assertFalse(
		new OutageClusterRevision(1, now, null, new ParserRun(now, now), 1).equals(
		new OutageClusterRevision(2, now, null, new ParserRun(now, now), 1)));
	//Different observation dates and estimated restorations cause revisions to be different.
	assertFalse(
		new OutageClusterRevision(1, now, null, new ParserRun(now, now), 1).equals(
		new OutageClusterRevision(1, now, null, new ParserRun(then, then), 1)));
	assertFalse(
		new OutageClusterRevision(1, now, null, new ParserRun(now, now), 1).equals(
		new OutageClusterRevision(1, then, null, new ParserRun(now, now), 1)));
	//Different number of outages causes revision to be different.
	assertFalse(
		new OutageClusterRevision(1, now, null, new ParserRun(now, now), 1).equals(
		new OutageClusterRevision(1, now, null, new ParserRun(now, now), 2)));
    }

    /**
     * Checks that the order by annotation is working.
     */
    public void testRevisionOrdering() {
	Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	final Timestamp now = new Timestamp(new Date().getTime());
	final Timestamp later = new Timestamp(now.getTime() + 1);
	Outage outage = new Outage(1, 1, now, null);
	final OutageRevision r2 = new OutageRevision(1, now, outage, new ParserRun(later, later), "test", CrewStatus.PENDING);
	final OutageRevision r1 = new OutageRevision(1, now, outage, new ParserRun(now, now), "test", CrewStatus.PENDING);
	session.save(outage);
	session.save(r1.getRun());
	session.save(r2.getRun());
	session.save(r1);
	session.save(r2);
	session.getTransaction().commit();
	session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	outage = (Outage) session.load(Outage.class, outage.getId());
	assertTrue(outage.getRevisions().first().getRun().getAsof().getTime() > outage.getRevisions().last().getRun().getAsof().getTime());
    }

    /**
     * Make sure Hibernate loads the outage revisions for a particular run.
     */
    public void testRunRelationship() {
	Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	final Timestamp now = new Timestamp(new Date().getTime());
	ParserRun run = new ParserRun(now, now);
	OutageRevision or1 = new OutageRevision(1, now, null, run, "test",
		CrewStatus.PENDING);
	OutageRevision or2 = new OutageRevision(1, now, null, run, "test",
		CrewStatus.PENDING);
	session.save(run);
	session.save(or1);
	session.save(or2);
	session.getTransaction().commit();
	session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	run = (ParserRun) session.load(ParserRun.class, run.getId());
	or1 = (OutageRevision) session.load(OutageRevision.class, or1.getId());
	or2 = (OutageRevision) session.load(OutageRevision.class, or2.getId());
	assertEquals(2, run.getOutageRevisions().size());
	assertTrue(run.getOutageRevisions().contains(or1));
	assertTrue(run.getOutageRevisions().contains(or2));
    }

    /**
     * Test that having a null restoration time will not crash everything.
     * Null restoration times can occur when Pepco is experiencing a lot of
     * outages (for example, during (or immediately after) a hurricane) and they
     * don't want to give out restoration estimates until they've assessed
     * the damage.
     */
    public void testNullRestoration() {
	assertEquals(
		new OutageRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING),
		new OutageRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING));
	assertEquals(
		new OutageRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING),
		new OutageRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING));
	assertFalse(
		new OutageRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING).equals(
		new OutageRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING)));
	assertFalse(
		new OutageRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING).equals(
		new OutageRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), "test", CrewStatus.PENDING)));

	assertEquals(
		new OutageClusterRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1),
		new OutageClusterRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1));
	assertEquals(
		new OutageClusterRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1),
		new OutageClusterRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1));
	assertFalse(
		new OutageClusterRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1).equals(
		new OutageClusterRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1)));
	assertFalse(
		new OutageClusterRevision(1, new Timestamp(0), null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1).equals(
		new OutageClusterRevision(1, null, null, new ParserRun(new Timestamp(0), new Timestamp(0)), 1)));
    }

    //Test that zoom levels are saved and controlled by the Outage.
    public void testSaveZoomLevel() {
	Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	final Timestamp now = new Timestamp(1);
	Outage o = new Outage(1, 1, now, now);
	o.getZoomLevels().addAll(Arrays.asList(new Integer[]{1, 2, 4, 5}));
	session.save(o);
	session.getTransaction().commit();

	session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	o = (Outage) session.get(Outage.class, o.getId());
	assertTrue(o.getZoomLevels().containsAll(Arrays.asList(new Integer[]{1, 2, 4, 5})));
    }

    //Test that zoom levels are kept separate between outages.
    public void testSeparateZoomLevels() {
	final Timestamp now = new Timestamp(1);
	Outage o1 = new Outage(1, 1, now, now);
	Outage o2 = new Outage(2, 2, now, now);
	o1.getZoomLevels().add(1);
	o2.getZoomLevels().add(2);

	Session session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	session.save(o1);
	session.save(o2);
	session.getTransaction().commit();

	session = this.sessionFactory.getCurrentSession();
	session.beginTransaction();
	o1 = (Outage) session.get(Outage.class, o1.getId());
	o2 = (Outage) session.get(Outage.class, o2.getId());
	assertEquals(1, o1.getZoomLevels().size());
	assertTrue(o1.getZoomLevels().contains(1));
	assertEquals(1, o2.getZoomLevels().size());
	assertTrue(o2.getZoomLevels().contains(2));
    }

    /**
     * Tests that equals and equalsIgnoreObservationDate works even when
     * used on child classes.
     */
    public void testRevisionChildClassEquality() {
	//Different causes
	assertFalse(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equals(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test2", CrewStatus.PENDING)));
	//Different statuses.
	assertFalse(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equals(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.EN_ROUTE)));
	//Different cause (ignore asof)
	assertFalse(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equalsIgnoreRun(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test2", CrewStatus.PENDING)));
	//Different status (ignore asof)
	assertFalse(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equalsIgnoreRun(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.EN_ROUTE)));
	//Make sure equals is still working
	assertTrue(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equals(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING)));
	//Make sure equals is still working (ignore asof).
	assertTrue(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equalsIgnoreRun(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING)));
	//Make sure asof is still ignored.
	assertTrue(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), "test", CrewStatus.PENDING).equalsIgnoreRun(
		new OutageRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(2)), "test", CrewStatus.PENDING)));

	//Different number of outages.
	assertFalse(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).equals(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 2)));
	//Different number of outages (ignore asof).
	assertFalse(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).equalsIgnoreRun(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 2)));
	//Make sure equals is still working
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).equals(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1)));
	//Make sure equals is still working (ignore asof).
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).equalsIgnoreRun(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1)));
	//Make sure asof is still ignored.
	assertTrue(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(1)), 1).equalsIgnoreRun(
		new OutageClusterRevision(1, new Timestamp(1), null, new ParserRun(new Timestamp(1), new Timestamp(2)), 1)));
    }
    
    // Make sure Outage can deal with a null observedEnd.
	public void testHashCode() {
		assertEquals(new HashCodeBuilder().append((double)1).append((double)1)
				.append(new Timestamp(1)).append((Timestamp)null)
				.toHashCode(), new Outage(1, 1, new Timestamp(1), null).hashCode());
	}
}
