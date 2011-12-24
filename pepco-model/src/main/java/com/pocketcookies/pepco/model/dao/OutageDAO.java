package com.pocketcookies.pepco.model.dao;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;

import com.pocketcookies.pepco.model.AbstractOutageRevision;
import com.pocketcookies.pepco.model.Outage;
import java.util.ArrayList;

public class OutageDAO {

    private final SessionFactory sessionFactory;

    public OutageDAO(SessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
    }

    protected OutageDAO() {
        this.sessionFactory=null;
    }

    @SuppressWarnings("unchecked")
    public Outage getActiveOutage(final double lat, final double lon) {

        final List<Outage> outages;
        outages = this.sessionFactory.getCurrentSession().createQuery(
                "from Outage where lat=:lat and lon=:lon and observedEnd=null").setDouble("lat", lat).setDouble("lon", lon).list();
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
     * We also add all zoom levels from the revision's outage to the existing outage's list of zoom levels.
     * 
     * @param revision
     * @return True if the revision is new and is successfully added to the
     *         database, false otherwise.
     */
    public boolean updateOutage(final AbstractOutageRevision revision) {
        this.sessionFactory.getCurrentSession().flush();
        final Outage existingOutage = getActiveOutage(revision.getOutage().getLat(), revision.getOutage().getLon());
        //Check whether this is a new outage (existingOutage should be null).
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
            existingOutage.getZoomLevels().addAll(revision.getOutage().getZoomLevels());
            revision.setOutage(existingOutage);
            //Test that no updates need to be made to the revision.
            if (revision.getOutage().getRevisions().first().equalsIgnoreRun(revision)) {
                return false;
            }
            this.sessionFactory.getCurrentSession().save(revision);
            return true;
        }
    }

    /**
     * Any outages with ids missing from the given list of outages will be
     * assumed closed with the given close time. If the outage is not in the
     * list but is already closed, it will not be updated (i.e. its closing time
     * will remain the same).
     * 
     * @param outages
     */
    public void closeMissingOutages(final Collection<Integer> outages,
            Timestamp closeTime) {
        this.sessionFactory.getCurrentSession().createQuery(
                "update Outage set observedEnd=:closeTime where id not in (:ids) and observedEnd is null").setTimestamp("closeTime", closeTime).setParameterList("ids", outages).executeUpdate();
    }

    /**
     * Retrieves a collection of outages as of the given date of the given type.
     * 
     * Important: Outages are returned that existed at the as-of date.  Nothing is done to the states of the outages.
     * That means changes to the outage (such as # of customers affected, estimated restoration, cause, etc.) will be included in the outage even if they happened after the as-of date.
     * @param asof The as-of date for the collection of outages to retrieve.
     * @param clazz The type of outage to retrieve.  If the type is AbstractOutageRevision, the type will be disregarded (outages of all types will be returned).
     * @return A list of outages of the specified type as of the specified time.
     */
    @SuppressWarnings("unchecked")
    public Collection<AbstractOutageRevision> getOutagesAtZoomLevelAsOf(
            final Timestamp asof,
            final Integer zoomLevel,
            final Class<? extends AbstractOutageRevision> clazz) {
        int parameterIndex = 0;
        final SQLQuery q = this.sessionFactory.getCurrentSession().createSQLQuery(
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
                + (zoomLevel == null ? "" : "    and ? in (select zoomLevel from zoomlevels where id = orev.outage)")
                + (clazz.equals(AbstractOutageRevision.class) ? ""
                : "    and orev.outagetype = ?"));
        q.setTimestamp(parameterIndex++, asof).setTimestamp(parameterIndex++, asof).setTimestamp(parameterIndex++, asof);
        if (zoomLevel != null) {
            q.setInteger(parameterIndex++, zoomLevel);
        }
        if (!clazz.equals(AbstractOutageRevision.class)) {
            q.setString(parameterIndex++, clazz.getSimpleName());
        }
        q.addEntity("outagerevision", AbstractOutageRevision.class);
        q.addJoin("outage", "outagerevision.outage");
        final Collection<Object[]> temp=q.list();
        final Collection<AbstractOutageRevision> ret = new ArrayList<AbstractOutageRevision>(temp.size());
        for(final Object[] o : temp){
            ret.add((AbstractOutageRevision) o[0]);
        }
        return ret;
    }
}
