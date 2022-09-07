FROM azul/zulu-openjdk:11

COPY target/ /home/target/
COPY properties/yugabyte-docker-template.properties /home/yugabyte-docker.properties
COPY schema/schema_postgres.sql /home/schema_postgres.sql