#!/bin/sh
set -eu

: "${EDUSUIVI_DB_URL:?Set the JDBC URL for the existing edusuivi_db database}"
: "${EDUSUIVI_DB_USER:?Set the PostgreSQL application user}"
: "${EDUSUIVI_DB_PASSWORD:?Set the PostgreSQL application password}"

export PG_DRIVER_JAR=/opt/pgdriver/postgresql.jar

"$JBOSS_HOME/bin/jboss-cli.sh" --file="$JBOSS_HOME/edusuivi-config/configure-wildfly.cli"
"$JBOSS_HOME/bin/jboss-cli.sh" --file="$JBOSS_HOME/edusuivi-config/configure-elytron-jaas.cli"

exec "$JBOSS_HOME/bin/standalone.sh" \
    --server-config=standalone-full.xml \
    -b 0.0.0.0 \
    -Djboss.http.port="${PORT:-8080}"
