# Scripts de Execução em Containers

Roteiros para executar experimentos em múltiplos containers Docker com controle de CPU, monitoramento de recursos e
coleta de resultados.

## Pré-requisitos

- Docker instalado e funcionando
- Imagem Docker `tse-base-image` criada
- Arquivo JAR: `soot-analysis-0.2.1-SNAPSHOT-jar-with-dependencies.jar`
- Biblioteca Python: `psutil`

## Fluxo de Uso (Ordem)

Execute os scripts nesta ordem:

1. **create_containers.sh** - Cria 10 containers
2. **clear_containers.sh** - Limpa estado anterior
3. **update_cpus.sh** - Configura CPUs para cada container
4. **show_cpus.sh** - Verifica configuração de CPUs
5. **install_psutil_all_containers.sh** - Instala psutil (se necessário)
6. **copy_to_containers.sh** - Copia arquivos para containers
7. **run_in_all_containers.sh** - Executa os experimentos
8. **copy_results.sh** - Copia resultados dos containers

## Scripts Principais

### create_containers.sh

Cria 10 containers Docker com 4 CPUs e 20GB RAM cada.

### clear_containers.sh

Remove resíduos de execuções anteriores nos containers.

### update_cpus.sh

Aloca CPUs específicas para cada container (sem sobreposição).

### show_cpus.sh

Mostra quais CPUs estão atribuídas a cada container.

### install_psutil_all_containers.sh

Instala biblioteca Python psutil em todos os containers.

- Necessário: pip ou apt-get disponível nos containers

### copy_to_containers.sh

Copia JAR e script Python para todos os containers.

- Arquivos copiados: `run_organize_monit_exp.py` e JAR

> Verifique se os caminhos dos arquivos estão corretos no script.

### run_in_all_containers.sh

Executa os experimentos em paralelo nos containers.

- Executa: `run_organize_monit_exp.py` em cada container
- Monitoramento automático: CPU, memória, tempo

### copy_results.sh

Copia para o host os resultados gerados dentro dos containers após o término da execução.
