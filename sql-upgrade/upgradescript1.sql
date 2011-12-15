begin transaction;
alter table parserrun add column asof timestamp;

create temp table something as
    select p.id as id, max(r.observationdate) as asof
    from parserrun p
    join outagerevisions r
        on p.id=r.run
    group by p.id;
create index idx_id on something(id);

update parserrun set asof = something.asof
from something
where something.id=parserrun.id;

alter table outagerevisions drop column observationdate;

commit;

vacuum analyze;
