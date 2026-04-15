#!/bin/bash
set -euo pipefail

BASE_DIR="/home/oav2/miningframework"

# Lista somente containers ativos com prefixo mbo2-tse-exp
CONTAINERS=$(docker ps --format '{{.Names}}' | grep '^mbo2-tse' || true)

if [ -z "$CONTAINERS" ]; then
    echo "Nenhum container ativo encontrado com prefixo mbo2-tse-exp"
    exit 1
fi

echo "Containers detectados:"
echo "$CONTAINERS"
echo "----------------------------------------"

for C in $CONTAINERS; do
    echo "Limpando arquivos no container: $C"

    docker exec "$C" bash -c "
        set -e
        cd '$BASE_DIR'

        rm -f \
            conflicts_log.txt \
            execution_err.txt \
            execution_log.txt \
            logs.log \
            out.json \
            out.txt \
            outConsole.txt \
            performance_summary.json \
            resource_usage_series.csv \
            time.txt \
            visited_methods.txt

        rm -rf results
    "

    echo "Container $C limpo com sucesso"
    echo "----------------------------------------"
done

echo "Limpeza finalizada em todos os containers."
