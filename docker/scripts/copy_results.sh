#!/bin/bash

# Pasta onde os resultados ser�o salvos (use "." para salvar na pasta atual)
DEST_BASE="./results"

# Lista todos containers que come�am com mbo2-tse-exp
containers=$(docker ps --format "{{.Names}}" | grep '^mbo2-tse-exp')

for container in $containers; do
    # extrai n�mero do container (ex: mbo2-tse-exp7 -> 7)
    num=$(echo "$container" | grep -o '[0-9]\+$')

    dest_dir="${DEST_BASE}/results${num}"

    echo "?? Copiando do container $container -> $dest_dir"

    # cria diret�rio local
    mkdir -p "$dest_dir"

    # copia apenas o conte�do da pasta results
    docker cp "${container}:/home/oav2/miningframework/results/." "$dest_dir/"
done

echo "? Copiado com sucesso!"
