#!/bin/bash

# Descobre quantas CPUs o host possui
TOTAL_CPUS=$(nproc)

containers=$(docker ps -q)

for c in $containers; do
    name=$(docker inspect -f '{{.Name}}' $c | sed 's/^\/\?//')
    cpus=$(docker inspect -f '{{.HostConfig.CpusetCpus}}' $c)

    if [ -z "$cpus" ]; then
        cpus="0-$((TOTAL_CPUS-1))"
    fi

    echo "$name | $cpus"
done
