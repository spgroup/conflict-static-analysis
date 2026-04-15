#!/bin/bash

BASE_DIR="/home/oav2/miningframework"
SCRIPT_NAME="run_organize_monit_exp.py"
LOG_FILE_NAME="logs.log"

# Lista somente containers ativos com prefixo mbo2-tse-exp
CONTAINERS=$(docker ps --format '{{.Names}}' | grep '^mbo2-tse-exp')

if [ -z "$CONTAINERS" ]; then
    echo "Nenhum container ativo encontrado com prefixo mbo2-tse-exp"
    exit 1
fi

echo "Containers detectados:"
echo "$CONTAINERS"
echo ""

for C in $CONTAINERS; do
    LOG_FILE="$BASE_DIR/${C}_${LOG_FILE_NAME}"

    echo "Executando script em background no container: $C"
    echo "Log: $LOG_FILE"

    docker exec -d "$C" sh -c "
        cd $BASE_DIR
        mkdir -p $BASE_DIR
        echo \"[START] \$(date) - Executando script\" >> $LOG_FILE
        nohup python3 $SCRIPT_NAME >> $LOG_FILE 2>&1 &
    "
done

echo ""
echo "Execuções iniciadas com nohup em todos os containers."
