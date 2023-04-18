#! /bin/bash

docker stop yugabytedb_node1
docker rm yugabytedb_node1

docker stop yugabytedb_node2
docker rm yugabytedb_node2

docker stop yugabytedb_node3
docker rm yugabytedb_node3

docker stop market-orders-instance
docker rm market-orders-instance

rm -r ~/yb_docker_data

