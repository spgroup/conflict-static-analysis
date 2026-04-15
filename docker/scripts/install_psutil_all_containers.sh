#!/bin/bash

set -e

PREFIX="^mbo2-tse-exp"

CONTAINERS=$(docker ps --format '{{.Names}}' | grep "$PREFIX" || true)

if [ -z "$CONTAINERS" ]; then
    echo "Nenhum container encontrado com prefixo mbo2-tse-exp"
    exit 0
fi

echo "Containers encontrados:"
echo "$CONTAINERS"
echo ""

for C in $CONTAINERS; do
    echo "----------------------------------------"
    echo "Instalando psutil no container: $C"

    docker exec "$C" sh -c "
        python3 - << 'EOF'
try:
    import psutil
    print('psutil já instalado')
except ImportError:
    print('psutil não encontrado, instalando...')
    import os
    os.system('pip3 install psutil || (apt update && apt install -y python3-pip && pip3 install psutil)')
EOF
    "
done

echo ""
echo "Instalação de psutil concluída em todos os containers."
