for c in $(docker ps --format '{{.Names}}' | grep -v mbo2-tse-exp); do
    docker update --cpuset-cpus="40-127" "$c"
done
