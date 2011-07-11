package com.pocketcookies.pepco.model;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import sun.security.util.PendingException;

import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.dao.OutageDAO;

public class TestOutage extends TestCase {
	private SessionFactory sessionFactory;

	public void setUp() {
		this.sessionFactory = new Configuration().configure()
				.buildSessionFactory();
	}

	public void testOutageRevisionDiscriminator() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		final Timestamp now = new Timestamp(new Date().getTime());
		Outage outage = new Outage(1, 1, now, now);
		OutageRevision r1 = new OutageRevision(1, now, now, outage, null,
				"test", CrewStatus.PENDING);
		OutageClusterRevision r2 = new OutageClusterRevision(1, now, now,
				outage, null, 1);
		session.save(outage);
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
		Outage o1 = new Outage(1, 1, now, now);
		Outage o2 = new Outage(1, 1, now, now);

		session.save(o1);
		session.save(o2);

		OutageRevision or1 = new OutageRevision(1, now, now, o1, null, "test",
				CrewStatus.PENDING);
		OutageRevision or2 = new OutageRevision(2, now, now, o2, null, "test",
				CrewStatus.PENDING);
		OutageClusterRevision cr1 = new OutageClusterRevision(1, now, now, o1,
				null, 1);
		OutageClusterRevision cr2 = new OutageClusterRevision(2, now, now, o2,
				null, 2);

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
		final OutageRevision or1 = new OutageRevision(1, now, now, null, null,
				"test", CrewStatus.ASSIGNED);
		final OutageRevision or2 = new OutageRevision(1, now2, now2, null,
				null, "test", CrewStatus.ASSIGNED);
		final OutageClusterRevision cr1 = new OutageClusterRevision(1, now,
				now, null, null, 1);
		final OutageClusterRevision cr2 = new OutageClusterRevision(1, now2,
				now2, null, null, 1);
		assertEquals(or1, or2);
		assertEquals(cr1, cr2);
		assertEquals(or1.hashCode(), or2.hashCode());
		assertEquals(cr1.hashCode(), cr2.hashCode());

		assertFalse(new OutageRevision(1, now, now, null, null, "test",
				CrewStatus.PENDING).equals(new OutageRevision(1, now, then,
				null, null, "test", CrewStatus.PENDING)));
		assertTrue(new OutageRevision(1, now, now, null, null, "test",
				CrewStatus.PENDING)
				.equalsIgnoreObservationDate(new OutageRevision(1, now, then,
						null, null, "test", CrewStatus.PENDING)));

		assertFalse(new OutageRevision(1, now, now, null, null, "test",
				CrewStatus.ASSIGNED).equals(new OutageRevision(2, now, now,
				null, null, "test", CrewStatus.ASSIGNED)));
		assertFalse(new OutageRevision(1, now, now, null, null, "test",
				CrewStatus.ASSIGNED).equals(new OutageRevision(1, then, then,
				null, null, "test", CrewStatus.ASSIGNED)));
		assertFalse(new OutageRevision(1, now, now, null, null, "test",
				CrewStatus.ASSIGNED).equals(new OutageRevision(1, now, now,
				null, null, "test2", CrewStatus.ASSIGNED)));
		assertFalse(new OutageRevision(1, now, now, null, null, "test",
				CrewStatus.ASSIGNED).equals(new OutageRevision(1, now, now,
				null, null, "test", CrewStatus.PENDING)));

		assertFalse(new OutageClusterRevision(1, now, now, null, null, 1)
				.equals(new OutageClusterRevision(2, now, now, null, null, 1)));
		assertFalse(new OutageClusterRevision(1, now, now, null, null, 1)
				.equals(new OutageClusterRevision(1, then, then, null, null, 1)));
		assertFalse(new OutageClusterRevision(1, now, now, null, null, 1)
				.equals(new OutageClusterRevision(1, now, now, null, null, 2)));
	}

	public void testRevisionOrdering() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		final Timestamp now = new Timestamp(new Date().getTime());
		final Timestamp later = new Timestamp(now.getTime() + 1);
		Outage outage = new Outage(1, 1, now, null);
		final OutageRevision r2 = new OutageRevision(1, now, later, outage,
				null, "test", CrewStatus.PENDING);
		final OutageRevision r1 = new OutageRevision(1, now, now, outage, null,
				"test", CrewStatus.PENDING);
		session.save(outage);
		session.save(r1);
		session.save(r2);
		session.getTransaction().commit();
		session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		outage = (Outage) session.load(Outage.class, outage.getId());
		assertTrue(outage.getRevisions().get(0).getObservationDate().getTime() > outage
				.getRevisions().get(1).getObservationDate().getTime());
	}

	public void testRunRelationship() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		final Timestamp now = new Timestamp(new Date().getTime());
		ParserRun run = new ParserRun();
		OutageRevision or1 = new OutageRevision(1, now, now, null, run, "test",
				CrewStatus.PENDING);
		OutageRevision or2 = new OutageRevision(1, now, now, null, run, "test",
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
		assertEquals(2, run.getRevisions().size());
		assertTrue(run.getRevisions().contains(or1));
		assertTrue(run.getRevisions().contains(or2));
	}
}
