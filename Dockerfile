FROM azul/zulu-openjdk:11

COPY target/ /home/target/
COPY properties/postgres-docker.properties /home/postgres-docker.properties
COPY properties/yugabyte-docker.properties /home/yugabyte-docker.properties
COPY properties/yugabyte-docker-pg-driver.properties /home/yugabyte-docker-pg-driver.properties
COPY schema/schema_postgres.sql /home/schema_postgres.sql