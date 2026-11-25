FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B --no-transfer-progress package

FROM eclipse-temurin:21-jdk-jammy

ARG WILDFLY_VERSION=42.0.0.Final
ARG POSTGRESQL_JDBC_VERSION=42.7.14

ENV JBOSS_HOME=/opt/wildfly

RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl tar \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p "$JBOSS_HOME" /opt/pgdriver \
    && curl -fsSL "https://github.com/wildfly/wildfly/releases/download/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz" -o /tmp/wildfly.tar.gz \
    && tar -xzf /tmp/wildfly.tar.gz --strip-components=1 -C "$JBOSS_HOME" \
    && curl -fsSL "https://repo.maven.apache.org/maven2/org/postgresql/postgresql/${POSTGRESQL_JDBC_VERSION}/postgresql-${POSTGRESQL_JDBC_VERSION}.jar" -o /opt/pgdriver/postgresql.jar \
    && rm /tmp/wildfly.tar.gz

COPY --from=build /workspace/target/EduSuivi.war /opt/wildfly/standalone/deployments/ROOT.war
COPY wildfly/configure-wildfly.cli /opt/wildfly/edusuivi-config/configure-wildfly.cli
COPY wildfly/configure-elytron-jaas.cli /opt/wildfly/edusuivi-config/configure-elytron-jaas.cli
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN groupadd --system --gid 10001 wildfly \
    && useradd --system --uid 10001 --gid wildfly --home-dir /opt/wildfly wildfly \
    && chmod 755 /usr/local/bin/docker-entrypoint.sh \
    && chown -R wildfly:wildfly /opt/wildfly /opt/pgdriver

USER wildfly

EXPOSE 8080
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
