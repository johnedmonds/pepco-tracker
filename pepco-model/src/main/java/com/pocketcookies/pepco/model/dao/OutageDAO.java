package com.pocketcookies.pepco.model.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.type.StandardBasicTypes;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import com.pocketcookies.pepco.model.ParserRun;

/**
 * DAO for updating {@link Outage}s and {@link AbstractOutageRevision}s.
 * 
 * @author John "Jack" Edmonds (john.a.edmonds@gmail.com)
 */
public class OutageDAO {

	private final SessionFactory sessionFactory;

	public OutageDAO(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * Used by CGLIB.
	 */
	protected OutageDAO() {
		this.sessionFactory = null;
	}

	@SuppressWarnings("unchecked")
	public Outage getActiveOutage(final double lat, final double lon) {

		final List<Outage> outages;
		outages = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Outage where lat=:lat and lon=:lon and observedEnd=null")
				.setDouble("lat", lat).setDouble("lon", lon).list();
		if (outages.isEmpty()) {
			return null;
		}
		return outages.get(0);
	}

	public Outage getOutage(final int outageId) {
		@SuppressWarnings("unchecked")
		final List<Outage> outages = this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"from Outage o left outer join fetch o.revisions r left outer join fetch r.run where o.id=:outageId")
				.setInteger("outageId", outageId).list();
		if (outages.isEmpty()) {
			return null;
		}
		return outages.get(0);
	}

	/**
	 * If the revision's outage is not already in the database, we save the
	 * outage. Otherwise, we update revision's outage with the outage from the
	 * database. If the revision is different from the most recent revision for
	 * the outage, we save the revision and return true. Otherwise, we do
	 * nothing and return false.
	 * 
	 * We also add all zoom levels from the revision's outage to the existing
	 * outage's list of zoom levels.
	 * 
	 * @param revision
	 * @return True if the revision is new and is successfully added to the
	 *         database, false otherwise.
	 * 
	 * @deprecated You should never need to update a single outage. Instead, you
	 *             should use updateOutages to more quickly update a batch of
	 *             outages using {@link #updateOutages}.
	 */
	@Deprecated
	public boolean updateOutage(final AbstractOutageRevision revision) {
		this.sessionFactory.getCurrentSession().flush();
		final Outage existingOutage = getActiveOutage(revision.getOutage()
				.getLat(), revision.getOutage().getLon());
		// Check whether this is a new outage (existingOutage should be null).
		if (!revision.getOutage().equals(existingOutage)) {
			this.sessionFactory.getCurrentSession().save(revision.getOutage());
			this.sessionFactory.getCurrentSession().save(revision);
			this.sessionFactory.getCurrentSession().flush();
			return true;
		} else {
			// Sometimes we load from the database and Hibernate pulls from its
			// cache. We rarely add outage revisions to the list of outages so
			// this could very well be out of date. We need the most up-to-date
			// version for when we are checking whether the revision is
			// different from the current revision.

			this.sessionFactory.getCurrentSession().refresh(existingOutage);
			revision.setOutage(existingOutage);
			// Test that no updates need to be made to the revision.
			if (revision.getOutage().getRevisions().first()
					.equalsIgnoreRun(revision)) {
				return false;
			}
			this.sessionFactory.getCurrentSession().save(revision);
			return true;
		}
	}

	/**
	 * Updates each outage in the set of outages by adding the revision's outage
	 * if the outage doesn't exist and adding the revision if necessary.
	 * 
	 * Note that this function does not refresh data from the database. So if
	 * you pass in an instance of an AbstractOutageRevision you got from the
	 * database, it should stay consistent with the database (i.e. if you add an
	 * outage revision, add the revision to the outage's list of revisions.
	 * Don't add the revision you intend to add with this function to the
	 * outage's list of revisions.)
	 * 
	 * If you're just passing in an AbstractOutageRevision with an outage you
	 * didn't retrieve from the database, you can use this function as expected.
	 * 
	 * @param outages
	 *            The outages to update.
	 * @return A list of all the outages that were updated or added.
	 */
	public Set<AbstractOutageRevision> updateOutages(
			final Set<AbstractOutageRevision> outages) {
		class RevisionAndOutage {
			final AbstractOutageRevision revision;
			final Optional<Outage> outage;
			public RevisionAndOutage(final AbstractOutageRevision revision, final Optional<Outage> outage) {
				this.revision = revision;
				this.outage = outage;
			}
		}
		final Iterable<RevisionAndOutage> loadedOutages = Iterables.transform(outages, new Function<AbstractOutageRevision, RevisionAndOutage>(){
			@Override public RevisionAndOutage apply(AbstractOutageRevision revision) {
				return new RevisionAndOutage(revision, Optional.fromNullable(getActiveOutage(
					revision.getOutage().getLat(), revision.getOutage()
							.getLon())));
			}
		});
		final Set<AbstractOutageRevision> ret = new HashSet<AbstractOutageRevision>();
		for (RevisionAndOutage entry : loadedOutages) {
			AbstractOutageRevision revision = entry.revision;
			Outage outage = revision.getOutage();
			// If there was already an outage.
			if (entry.outage.isPresent()) {
				Outage existingOutage = entry.outage.get();
				revision.setOutage(existingOutage);
				// Test that no updates need to be made to the revision.
				if (existingOutage.getRevisions().first()
						.equalsIgnoreRun(revision)) {
					// Ignore it.
				} else {
					ret.add(revision);
					sessionFactory.getCurrentSession().save(revision);
				}
			} else {
				sessionFactory.getCurrentSession().save(outage);
				sessionFactory.getCurrentSession().save(revision);
				ret.add(revision);
			}
		}
		return ret;
	}

	/**
	 * Any outages that are "active" in the database but whose ids are missing
	 * from the given list of outages will be assumed closed with the given
	 * close time. If the outage is not in the list but is already closed, it
	 * will not be updated (i.e. its closing time will remain the same).
	 * 
	 * @param outages
	 *            The outages *NOT* to close.
	 */
	public void closeMissingOutages(final Collection<Integer> outages,
			Timestamp closeTime) {
		this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"update Outage set observedEnd=:closeTime where id not in (:ids) and observedEnd is null")
				.setTimestamp("closeTime", closeTime)
				.setParameterList("ids", outages).executeUpdate();
	}

	/**
	 * Retrieves a collection of outages as of the given date of the given type.
	 * 
	 * Important: Outages are returned that existed at the as-of date. Nothing
	 * is done to the states of the outages. That means changes to the outage
	 * (such as # of customers affected, estimated restoration, cause, etc.)
	 * will be included in the outage even if they happened after the as-of
	 * date.
	 * 
	 * @param asof
	 *            The as-of date for the collection of outages to retrieve.
	 * @param clazz
	 *            The type of outage to retrieve. If the type is
	 *            AbstractOutageRevision, the type will be disregarded (outages
	 *            of all types will be returned).
	 * @return A list of outages of the specified type as of the specified time.
	 */
	@SuppressWarnings("unchecked")
	public Collection<AbstractOutageRevision> getOutagesAtZoomLevelAsOf(
			final Timestamp asof, final Integer zoomLevel,
			final Class<? extends AbstractOutageRevision> clazz) {
		int parameterIndex = 0;
		final SQLQuery q = this.sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"select orev.*, o.* "
								+ "from outagerevisions orev "
								+ "join outages o on orev.outage = o.id "
								+ "join parserrun r on orev.run = r.id "
								+ "join (select max(r2.asof) as observationdate, outage "
								+ "     from outagerevisions orev2"
								+ "	join parserrun r2 on orev2.run = r2.id"
								+ "	where r2.asof <= ?"
								+ "	group by outage) sq "
								+ "on orev.outage = sq.outage and r.asof = sq.observationdate "
								+ "where o.earliestReport <= ? and "
								+ "    (o.observedEnd is null or o.observedEnd >= ?)"
								+ (zoomLevel == null ? ""
										: "    and (? >= orev.first_Seen_Zoom_Level and (orev.last_Seen_Zoom_Level is NULL or ? <= orev.last_seen_zoom_level))")
								+ (clazz.equals(AbstractOutageRevision.class) ? ""
										: "    and orev.outagetype = ?"));
		q.setTimestamp(parameterIndex++, asof)
				.setTimestamp(parameterIndex++, asof)
				.setTimestamp(parameterIndex++, asof);
		if (zoomLevel != null) {
			q.setInteger(parameterIndex++, zoomLevel);
			q.setInteger(parameterIndex++, zoomLevel);
		}
		if (!clazz.equals(AbstractOutageRevision.class)) {
			q.setString(parameterIndex++, clazz.getSimpleName());
		}
		q.addEntity("outagerevision", AbstractOutageRevision.class);
		q.addJoin("outage", "outagerevision.outage");
		final Collection<Object[]> temp = q.list();
		final Collection<AbstractOutageRevision> ret = new ArrayList<AbstractOutageRevision>(
				temp.size());
		for (final Object[] o : temp) {
			ret.add((AbstractOutageRevision) o[0]);
		}
		return ret;
	}

	/**
	 * Summarizes what happened during a ParserRun.
	 * 
	 * @param run
	 * @return
	 */
	public ParserRunSummary getParserRunSummary(final ParserRun run) {
		@SuppressWarnings("unchecked")
		final List<Object[]> openUpdated = this.sessionFactory
				.getCurrentSession()
				.createSQLQuery(
						"select count(*) as num_outages, revision_count > 1 as is_updated "
								// Get the outages that were created or updated
								// for this run. That is, the outage has a
								// revision that was obtained during this run.
								+ "    from (select o2.id from"
								+ "            Outages o2"
								+ "            join OutageRevisions r2"
								+ "            on o2.id = r2.outage"
								+ "            where r2.run = ?) o"
								// Get the number of revisions per outage as of
								// this run. If there's more than one revision
								// for this outage as of this run, the outage
								// was updated. Otherwise, it's brand new.
								+ "    join (select r3.outage, count(*) as revision_count from OutageRevisions r3"
								+ "            join ParserRun pr"
								+ "            on r3.run = pr.id"
								+ "            where pr.runTime <= ?"
								+ "            group by r3.outage) r"
								+ "    on o.id = r.outage"
								+ " group by is_updated")
				.addScalar("num_outages", StandardBasicTypes.INTEGER)
				.addScalar("is_updated", StandardBasicTypes.BOOLEAN)
				.setInteger(0, run.getId()).setTimestamp(1, run.getRunTime())
				.list();
		final int newOutages;
		final int updatedOutages;
		if (openUpdated.isEmpty()) {
			newOutages = updatedOutages = 0;
		} else {
			// If the first entry is the list of updated outages, then the
			// second entry (if it exists) must be the list of new outages.
			// Note that if there are no updated outages or no new outages, we
			// will not get 0, instead the row will be missing.
			// Therefore, you'll find some code below to handle that case.
			if (((Boolean) openUpdated.get(0)[1]).booleanValue() == true) {
				updatedOutages = ((Integer) openUpdated.get(0)[0]).intValue();
				if (openUpdated.size() > 1) {
					newOutages = ((Integer) openUpdated.get(1)[0]).intValue();
				} else {
					newOutages = 0;
				}
			} else {
				newOutages = ((Integer) openUpdated.get(0)[0]).intValue();
				if (openUpdated.size() > 1) {
					updatedOutages = ((Integer) openUpdated.get(1)[0])
							.intValue();
				} else {
					updatedOutages = 0;
				}
			}
		}
		// Find the runTime of the next ParserRun (if it exists).
		// This will let us find all the outages that were closed in this parser
		// run.
		// We can tell that an outage was closed for this parser run if it
		// happened after this parser run but before the next one.
		final Timestamp nextParserRuntime = (Timestamp) this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select min(runTime) from ParserRun where runTime > :currentRunTime")
				.setTimestamp("currentRunTime", run.getRunTime())
				.uniqueResult();

		final int closedOutages = ((Number) this.sessionFactory
				.getCurrentSession()
				.createQuery(
						"select count(*) from Outage o where o.observedEnd >= :currentRunTime and o.observedEnd < :nextRunTime")
				.setTimestamp("currentRunTime", run.getRunTime())
				.setTimestamp(
						"nextRunTime",
						nextParserRuntime == null ? new Timestamp(new Date()
								.getTime()) : nextParserRuntime).uniqueResult())
				.intValue();
		return new ParserRunSummary(newOutages, updatedOutages, closedOutages,
				run);
	}
}
