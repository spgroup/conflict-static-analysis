#!/bin/bash

# Script para limpar resultados e arquivos residuais em todos os containers mbo2-tse-exp
# Uso: ./reset_results.sh

CONTAINERS_PREFIX="mbo2-tse-exp"
NUM_CONTAINERS=10
RESULTS_DIR="/home/oav2/miningframework/results"

# Arquivos grandes/residuais a remover (OOM, logs antigos, dumps)
EXTRA_PATHS=(
  "/home/oav2/miningframework/java_pid*.hprof"
  "/home/oav2/miningframework/logs.log"
  "/home/oav2/miningframework/logs.txt"
  "/home/oav2/miningframework/mbo2-tse-exp*_logs.log"
  "/home/oav2/miningframework/AnalysisRecords.csv"
  "/home/oav2/miningframework/conflicts_log.txt"
  "/home/oav2/miningframework/outConsole.txt"
  "/home/oav2/miningframework/out.txt"
  "/home/oav2/miningframework/out.json"
  "/home/oav2/miningframework/HasMainMethod.csv"
  "/home/oav2/miningframework/PANotResolve.csv"
  "/home/oav2/miningframework/visited_methods.txt"
  "/home/oav2/miningframework/time.txt"
  "/home/oav2/miningframework/output/data/soot-results.csv"
)

echo "== Limpando resultados e resíduos em ${NUM_CONTAINERS} containers =="

for ((i=1; i<=NUM_CONTAINERS; i++)); do
  CONTAINER="${CONTAINERS_PREFIX}${i}"
  echo "-> Limpando ${CONTAINER}"

  # Limpa diretório de resultados
  docker exec "$CONTAINER" sh -c "mkdir -p ${RESULTS_DIR} && rm -rf ${RESULTS_DIR}/*"

  # Remove arquivos residuais específicos
  for path in "${EXTRA_PATHS[@]}"; do
    docker exec "$CONTAINER" sh -c "rm -f ${path}" >/dev/null 2>&1
  done

  if [ $? -eq 0 ]; then
    echo "   OK: ${CONTAINER} limpo"
  else
    echo "   ERRO: falha ao limpar ${CONTAINER}"
  fi
done

echo "== Finalizado =="
