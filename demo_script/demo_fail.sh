#!/usr/bin/env bash

. demo-magic.sh

DEMO_PROMPT="${GREEN}➜ ${CYAN}\W ${COLOR_RESET}"

# text color
# DEMO_CMD_COLOR=$BLACK

# hide the evidence
clear

TYPE_SPEED=25

pe "docker stop yugabytedb_node2 "

pe "docker start yugabytedb_node2 "