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

	private static final int DEFAULT_FIRST_SEEN_ZOOM_LEVEL = 1;
	private static final int DEFAULT_LAST_SEEN_ZOOM_LEVEL = 1;

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

	/**
	 * Make sure we can insert and retrieve both types of
	 * AbstractOutageRevision.
	 */
	public void testOutageRevisionDiscriminator() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		final Timestamp now = new Timestamp(new Date().getTime());
		Outage outage = new Outage(1, 1, now, now);
		OutageRevision r1 = new OutageRevision(1, now, outage, new ParserRun(
				now, now), "test", CrewStatus.PENDING,
				DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
		OutageClusterRevision r2 = new OutageClusterRevision(1, now, outage,
				new ParserRun(now, now), 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL,
				DEFAULT_LAST_SEEN_ZOOM_LEVEL);
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

		OutageRevision or1 = new OutageRevision(1, now, o1, nowRun, "test",
				CrewStatus.PENDING, DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
		OutageRevision or2 = new OutageRevision(2, now, o2, nowRun, "test",
				CrewStatus.PENDING, DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
		OutageClusterRevision cr1 = new OutageClusterRevision(1, now, o1,
				nowRun, 1, DEFAULT_FIRST_SEEN_ZOOM_LEVEL,
				DEFAULT_LAST_SEEN_ZOOM_LEVEL);
		OutageClusterRevision cr2 = new OutageClusterRevision(2, now, o2,
				nowRun, 2, DEFAULT_FIRST_SEEN_ZOOM_LEVEL,
				DEFAULT_LAST_SEEN_ZOOM_LEVEL);

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

	/**
	 * Checks that the order by annotation is working.
	 */
	public void testRevisionOrdering() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		final Timestamp now = new Timestamp(new Date().getTime());
		final Timestamp later = new Timestamp(now.getTime() + 1);
		Outage outage = new Outage(1, 1, now, null);
		final OutageRevision r2 = new OutageRevision(1, now, outage,
				new ParserRun(later, later), "test", CrewStatus.PENDING,
				DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
		final OutageRevision r1 = new OutageRevision(1, now, outage,
				new ParserRun(now, now), "test", CrewStatus.PENDING,
				DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
		session.save(outage);
		session.save(r1.getRun());
		session.save(r2.getRun());
		session.save(r1);
		session.save(r2);
		session.getTransaction().commit();
		session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		outage = (Outage) session.load(Outage.class, outage.getId());
		assertTrue(outage.getRevisions().first().getRun().getAsof().getTime() > outage
				.getRevisions().last().getRun().getAsof().getTime());
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
				CrewStatus.PENDING, DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
		OutageRevision or2 = new OutageRevision(1, now, null, run, "test",
				CrewStatus.PENDING, DEFAULT_FIRST_SEEN_ZOOM_LEVEL);
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

	// Make sure Outage can deal with a null observedEnd.
	public void testHashCode() {
		assertEquals(
				new HashCodeBuilder().append((double) 1).append((double) 1)
						.append(new Timestamp(1)).append((Timestamp) null)
						.toHashCode(),
				new Outage(1, 1, new Timestamp(1), null).hashCode());
	}
}
