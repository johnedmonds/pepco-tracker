#!/bin/sh
PEPCO_DATABASE_NAME="pepco"
$POSTGRES_HOME/bin/psql --no-align --field-separator "," -d $PEPCO_DATABASE_NAME --file summary-query.sql --pset footer | Rscript reliability.r test.svg

