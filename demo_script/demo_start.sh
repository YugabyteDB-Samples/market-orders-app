#!/usr/bin/env bash

. demo-magic.sh

# Creating a folder and custom network for the demo
# The commands fail silently if the folder and network already exist
mkdir ~/yb_docker_data 2> /dev/null || true
docker network create custom-network 2> /dev/null || true

DEMO_PROMPT="${GREEN}➜ ${CYAN}\W ${COLOR_RESET}"

# text color
# DEMO_CMD_COLOR=$BLACK

# hide the evidence
clear

TYPE_SPEED=

pe "docker run -d --name yugabytedb_node1 --net custom-network \
  -p 7001:7000 -p 9001:9000 -p 5433:5433 \
  -v ~/yb_docker_data/node1:/home/yugabyte/yb_data --restart unless-stopped \
  yugabytedb/yugabyte:2.15.1.0-b175 \
  bin/yugabyted start \
  --master_flags="ysql_num_shards_per_tserver=3" --tserver_flags="ysql_num_shards_per_tserver=3" \
  --base_dir=/home/yugabyte/yb_data --daemon=false"

pe "docker run -d --name yugabytedb_node2 --net custom-network \
  -p 7002:7000 -p 9002:9000 -p 5434:5433 \
  -v ~/yb_docker_data/node2:/home/yugabyte/yb_data --restart unless-stopped \
  yugabytedb/yugabyte:2.15.1.0-b175 \
  bin/yugabyted start --join=yugabytedb_node1 \
  --master_flags="ysql_num_shards_per_tserver=3" --tserver_flags="ysql_num_shards_per_tserver=3" \
  --base_dir=/home/yugabyte/yb_data --daemon=false"

pe "docker run -d --name yugabytedb_node3 --net custom-network \
  -p 7003:7000 -p 9003:9000 -p 5435:5433 \
  -v ~/yb_docker_data/node3:/home/yugabyte/yb_data --restart unless-stopped \
  yugabytedb/yugabyte:2.15.1.0-b175 \
  bin/yugabyted start --join=yugabytedb_node1 \
  --master_flags="ysql_num_shards_per_tserver=3" --tserver_flags="ysql_num_shards_per_tserver=3" \
  --base_dir=/home/yugabyte/yb_data --daemon=false"

TYPE_SPEED=25

pei "clear"

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

pe "cat ../properties/yugabyte-docker.properties"

echo -e

pei "cd .."

pe "mvn clean package" 
pe "docker rmi market-orders-app"
pe "docker build -t market-orders-app ."

TYPE_SPEED=

pe "docker run --name market-orders-instance --net custom-network \
    market-orders-app:latest \
    java -jar /home/target/market-orders-app.jar \
    connectionProps=/home/yugabyte-docker.properties \
    loadScript=/home/schema_postgres.sql \
    refreshView=false \
    tradeStatsInterval=5000"

TYPE_SPEED=25
