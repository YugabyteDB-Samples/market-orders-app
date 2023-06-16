#!/usr/bin/env bash

. ../demo-magic.sh

# Creating a folder and custom network for the demo
# The commands fail silently if the folder and network already exist
mkdir ~/yb_docker_data 2> /dev/null || true
docker network create custom-network 2> /dev/null || true

DEMO_PROMPT="${GREEN}➜ ${CYAN}\W ${COLOR_RESET}"

# text color
# DEMO_CMD_COLOR=$BLACK

# hide the evidence
clear

TYPE_SPEED=25

p "psql -h 127.0.0.1 -p 5433 -U yugabyte"

set +e

until psql -h 127.0.0.1 -p 5433 -U yugabyte
do
    echo waiting for YugabyteDB to start...
    sleep 5
done

set -e

pei "clear"

pe "open -a Safari http://localhost:7001"

pe "cat ../../properties/yugabyte-docker.properties"

echo -e

set +e

pe "docker container stop market-orders-instance"
pe "docker container rm market-orders-instance"
TYPE_SPEED=

pe "docker run --name market-orders-instance --net custom-network \
    market-orders-app:latest \
    java -jar /home/target/market-orders-app.jar \
    connectionProps=/home/yugabyte-docker.properties \
    loadScript=/home/schema_postgres.sql \
    refreshView=false \
    tradeStatsInterval=5000"

set -e

TYPE_SPEED=25

pei "clear"

pe "cat ../../properties/yugabyte-docker.properties"
echo -e

pe "docker container start market-orders-instance -a"

TYPE_SPEED=25
