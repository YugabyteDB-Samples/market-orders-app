#!/usr/bin/env bash

. demo-magic.sh

DEMO_PROMPT="${GREEN}➜ ${CYAN}\W ${COLOR_RESET}"

# text color
# DEMO_CMD_COLOR=$BLACK

# hide the evidence
clear

TYPE_SPEED=50

pe "java -jar target/market-orders-app.jar connectionProps=./properties/yugabytedb-managed.properties tradeStatsInterval=2000"