#!/bin/bash

SRC_FILE="soot-analysis-0.2.1-SNAPSHOT-jar-with-dependencies.jar"
DEST_PATH="/home/oav2/miningframework/dependencies"

# Verifica se o arquivo existe
if [ ! -f "$SRC_FILE" ]; then
    echo "Erro: arquivo $SRC_FILE não encontrado."
    exit 1
fi

# Lista containers ativos com prefixo mbo2-tse-exp
CONTAINERS=$(docker ps --format '{{.Names}}' | grep '^mbo2-tse')

if [ -z "$CONTAINERS" ]; then
    echo "Nenhum container ativo encontrado com o prefixo mbo2-tse-exp"
    exit 1
fi

echo "Containers detectados:"
echo "$CONTAINERS"
echo ""

# Copia para cada container encontrado
for C in $CONTAINERS; do
    echo "Copiando para $C ..."
    docker exec "$C" mkdir -p "$DEST_PATH"
    docker cp "$SRC_FILE" "$C:$DEST_PATH/" 
done

echo ""
echo "Concluído!"
