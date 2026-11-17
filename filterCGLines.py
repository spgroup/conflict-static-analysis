def filtrar_linhas(arquivo_entrada, termo, arquivo_saida):
    with open(arquivo_entrada, 'r', encoding='utf-8') as f:
        linhas = f.readlines()

    primeira_linha = linhas[0]
    ultima_linha = linhas[-1]
    linhas_filtradas = [linha for linha in linhas if termo in linha]

    with open(arquivo_saida, 'w', encoding='utf-8') as f:
        f.write(primeira_linha)
        f.writelines(linhas_filtradas)
        f.write(ultima_linha)

    print(f"Arquivo filtrado salvo como: {arquivo_saida}")


# Exemplo de uso
arquivo_entrada = "callgraph.dot"  # Nome do arquivo de entrada
arquivo_saida = "callgraph_filtrado.dot"  # Nome do arquivo de saída
termo = "LuceneVerifyingIndexOutput"

filtrar_linhas(arquivo_entrada, termo, arquivo_saida)
