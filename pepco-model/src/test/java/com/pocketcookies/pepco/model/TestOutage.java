package com.pocketcookies.pepco.model;

import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.pocketcookies.pepco.model.OutageRevision.CrewStatus;
import com.pocketcookies.pepco.model.dao.OutageDAO;

public class TestOutage extends TestCase {
	private SessionFactory sessionFactory;

	public void setUp() {
		this.sessionFactory = new Configuration().configure()
				.buildSessionFactory();
	}

	public void testOutage1() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();

		Date now = new Date();
		Outage outage = new Outage(1, 1, 1, now, now);
		OutageCluster cluster = new OutageCluster(2, 2, 2, now, now);
		session.save(outage);
		session.save(cluster);
		session.getTransaction().commit();
		session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		final List<AbstractOutage> outages = session.createQuery(
				"from AbstractOutage order by id").list();
		assertTrue(outages.get(1) instanceof OutageCluster);
		assertTrue(outages.get(0) instanceof AbstractOutage);
	}

	public void testOutage2() {
		final Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		final Date now = new Date();
		session.save(new OutageCluster(1, 1, 1, now, now));
		session.save(new Outage(2, 1, 1, now, now));
		assertEquals(1, session.createQuery("from OutageCluster").list().size());
		assertEquals(1, session.createQuery("from Outage").list().size());
	}

	public void testRevisions() {
		Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();
		Date now = new Date();
		Outage o1 = new Outage(1, 1, 1, now, now);
		Outage o2 = new Outage(2, 1, 1, now, now);
		OutageCluster c1 = new OutageCluster(3, 1, 1, now, now);
		OutageCluster c2 = new OutageCluster(4, 1, 1, now, now);

		session.save(o1);
		session.save(o2);
		session.save(c1);
		session.save(c2);

		OutageRevision or1 = new OutageRevision(1, "test", CrewStatus.PENDING,
				o1);
		OutageRevision or2 = new OutageRevision(2, "test", CrewStatus.PENDING,
				o2);
		OutageClusterRevision cr1 = new OutageClusterRevision(1, 1, c1);
		OutageClusterRevision cr2 = new OutageClusterRevision(2, 2, c2);

		session.save(or1);
		session.save(or2);
		session.save(cr1);
		session.save(cr2);

		session.getTransaction().commit();
		session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();

		o1 = (Outage) session.load(Outage.class, 1);
		o2 = (Outage) session.load(Outage.class, 2);
		c1 = (OutageCluster) session.load(OutageCluster.class, 3);
		c2 = (OutageCluster) session.load(OutageCluster.class, 4);
		or1 = (OutageRevision) session.load(OutageRevision.class, 1);
		or2 = (OutageRevision) session.load(OutageRevision.class, 2);
		cr1 = (OutageClusterRevision) session.load(OutageClusterRevision.class,
				1);
		cr2 = (OutageClusterRevision) session.load(OutageClusterRevision.class,
				2);
		assertEquals(1, o1.getRevisions().size());
		assertEquals(1, o2.getRevisions().size());
		assertEquals(1, c1.getRevisions().size());
		assertEquals(1, c2.getRevisions().size());
		assertEquals(or1, o1.getRevisions().iterator().next());
		assertEquals(or2, o2.getRevisions().iterator().next());
		assertEquals(cr1, c1.getRevisions().iterator().next());
		assertEquals(cr2, c2.getRevisions().iterator().next());

	}

	public void testActiveOutage() {
		final OutageDAO dao = new OutageDAO(sessionFactory);
		final Session session = this.sessionFactory.getCurrentSession();
		session.beginTransaction();

		assertNull(dao.getActiveOutage(1, 1, null));
		assertNull(dao.getActiveOutage(1, 1, AbstractOutage.class));
		assertNull(dao.getActiveOutage(1, 1, AbstractOutage.class));
		assertNull(dao.getActiveOutage(1, 1, OutageCluster.class));

		final Date now = new Date();

		session.save(new OutageCluster(1, 1, 1, now, now));
		session.save(new Outage(2, 1, 1, now, now));

		assertNull(dao.getActiveOutage(1, 1, null));
		assertNull(dao.getActiveOutage(1, 1, AbstractOutage.class));
		assertNull(dao.getActiveOutage(1, 1, AbstractOutage.class));
		assertNull(dao.getActiveOutage(1, 1, OutageCluster.class));

		final OutageCluster cluster = new OutageCluster(3, 1, 1, now, null);
		session.save(cluster);
		final AbstractOutage outage = new Outage(4, 1, 1, now, null);
		session.save(outage);

		assertEquals(
				cluster,
				dao.getActiveOutage(cluster.getLat(), cluster.getLon(),
						cluster.getClass()));

		assertEquals(
				outage,
				dao.getActiveOutage(outage.getLat(), outage.getLon(),
						outage.getClass()));

		assertNull(dao.getActiveOutage(cluster.getLat(), cluster.getLon(),
				AbstractOutage.class));
	}

	public void testOutageEquals() {
		final Date now = new Date();
		final Date now2 = new Date(now.getTime());
		assertEquals(new Outage(1, 1, 1, now, now), new Outage(1, 1, 1, now2,
				now2));
		// Make sure we don't crash due to nulls
		// Note that we don't need to test some columns being null because we
		// set not-null=true on them in Hibernate. I know this doesn't truly
		// prevent them from being null but I believe it is acceptable to crash
		// hard if these expectations are broken.
		assertEquals(new Outage(1, 1, 1, now, null), new Outage(1, 1, 1, now,
				null));
		assertFalse(new Outage(1, 1, 1, now, null).equals(new Outage(1, 1, 1,
				now, now)));
		assertFalse(new Outage(1, 1, 1, now, now).equals(new Outage(1, 1, 1,
				now, null)));
	}
}
