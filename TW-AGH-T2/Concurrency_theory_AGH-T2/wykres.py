import pandas as pd
import matplotlib.pyplot as plt

CPU_TIME = True
USE_LOCAL_THREAD = True
NUMBER_OF_PRODUCERS = 30

global_measures = f'/home/dominiq/Pulpit/Semestr_V/TW/TW-praca-grp-2/measures_Global_Dominik_{NUMBER_OF_PRODUCERS}.CSV'
local_measures = f'/home/dominiq/Pulpit/Semestr_V/TW/TW-praca-grp-2/measures_Thread-Local_Dominik_{NUMBER_OF_PRODUCERS}.CSV'

fig_save_path = f"/home/dominiq/Pulpit/Semestr_V/TW/TW-praca-grp-2/wyniki/cons{NUMBER_OF_PRODUCERS}prod{NUMBER_OF_PRODUCERS}"

group_by_label = "procesora" if  CPU_TIME else "rzeczywisty"

df_global, df_local = pd.read_csv(global_measures), pd.read_csv(local_measures)
df_local.sort_values(by=['maksymalna porcja', f'średni czas {group_by_label}'], axis=0, ascending=True, inplace=True, na_position='first')
df_global.sort_values(by=['maksymalna porcja', f'średni czas {group_by_label}'], axis=0, ascending=True, inplace=True, na_position='first')

grouped_data_local = df_local.groupby('typ bufora')
grouped_data_global = df_global.groupby('typ bufora')

plt.figure(figsize=(10, 7))
colors = [(0.1, 0.3, 0.5), (0.6, 0.3, 0.15)]
faded_colors = [(0.1, 0.3, 0.5, 0.5), (0.6, 0.3, 0.15, 0.5)]

for id, (name, group) in enumerate(grouped_data_local):
    plt.plot(group['maksymalna porcja'], group[f'średni czas {group_by_label}'], 'o-',  label=f"{name} - local RNG", color = colors[id])

for id, (name, group) in enumerate(grouped_data_global):
    plt.plot(group['maksymalna porcja'], group[f'średni czas {group_by_label}'], 'o-',  label=f"{name} - global RNG", color = faded_colors[id])

header = "procesora" if CPU_TIME else "rzeczywistego"

plt.title(f'Zależność czasu {header} (50 pomiarów na punkt) od maksymalnej porcji. Bufor=1000')
plt.xlabel('Maksymalna porcja')
plt.ylabel(f'Średni czas {group_by_label} [ns]')
plt.legend()

save_fig_label = "CPU" if CPU_TIME else "System"

# Wyświetl wykres
plt.savefig(fig_save_path + f"/{save_fig_label}_time.png")