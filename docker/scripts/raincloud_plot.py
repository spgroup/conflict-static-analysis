import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns

# Leitura do CSV
df = pd.read_csv("merged_results.csv", delimiter=';')

# Conversão para float
df["oa_inter_mean"] = pd.to_numeric(df["oa_inter_mean"], errors='coerce')
df["oa_inter_no_pa_mean"] = pd.to_numeric(df["oa_inter_no_pa_mean"], errors='coerce')

# Remover valores ausentes
df = df.dropna(subset=["oa_inter_mean", "oa_inter_no_pa_mean"])

# Reorganizar para formato longo
df_melted = pd.melt(
    df,
    value_vars=["oa_inter_mean", "oa_inter_no_pa_mean"],
    var_name="Configuration",
    value_name="Mean Time"
)

# Renomear valores para o gráfico
df_melted["Configuration"] = df_melted["Configuration"].map({
    "oa_inter_mean": "PA",
    "oa_inter_no_pa_mean": "noPA"
})

# Definir paleta de cores
palette = {"PA": "#1f77b4", "noPA": "#ff7f0e"}

# Estilo do gráfico
sns.set(style="whitegrid")
plt.figure(figsize=(10, 6))

# Violin plot (sem preenchimento interno)
sns.violinplot(
    data=df_melted,
    x="Mean Time",
    y="Configuration",
    inner=None,
    linewidth=1.2,
    palette={k: "#DDDDDD" for k in palette}  # tom cinza claro para violins
)

# Overlay dos pontos (stripplot) com cores específicas
for config, color in palette.items():
    sns.stripplot(
        data=df_melted[df_melted["Configuration"] == config],
        x="Mean Time",
        y="Configuration",
        jitter=0.2,
        size=4,
        alpha=0.7,
        color=color
    )

# Remover moldura superior e direita
sns.despine()

# Ajustes finais
plt.xlabel("Mean Time (s)", fontsize=12)
plt.ylabel("Configuration", fontsize=12)
plt.tight_layout()
plt.savefig("raincloud_plot.png", dpi=300)
plt.show()
