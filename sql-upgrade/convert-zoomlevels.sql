--Now, instead of having a table with all the zoomlevels at which we find the outage/outage cluster, we store the first and last zoom level at which the outage was seen.  This SQL file makes the conversion.
alter table outagerevisions add column firstseenzoomlevel integer;
alter table outagerevisions add column lastseenzoomlevel integer;
update outagerevisions
  set firstseenzoomlevel = source_firstseenzoomlevel, lastseenzoomlevel = source_lastseenzoomlevel
  from (select id, min(zoomlevel) as source_firstseenzoomlevel, max(zoomlevel) as source_lastseenzoomlevel
      from zoomlevels group by id) zoom
  where zoom.id = outage;
update outagerevisions set lastseenzoomlevel = null where outagetype='OutageRevision';
drop table zoomlevels;
