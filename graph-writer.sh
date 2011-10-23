#!/bin/sh
PEPCO_DATABASE_NAME=$1
USERNAME=$2
OUTPUT_DIR=$3
$POSTGRES_HOME/bin/psql --no-align --field-separator "," -d $PEPCO_DATABASE_NAME -U $USERNAME --file summary-query.sql --pset footer | Rscript reliability.r $OUTPUT_DIR/test.svg

