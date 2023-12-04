import pandas as pd
import matplotlib.pyplot as plt

# Wczytaj dane z pliku CSV
df = pd.read_csv('C:\\Users\\barte\\IdeaProjects\\Concurrency_theory_AGHcourse_groupTask\\measures_Thread-Local_Bartek.CSV')

df.sort_values(by=['maksymalna porcja', 'średni czas rzeczywisty'], axis=0, ascending=True, inplace=True, na_position='first')
print(df)

# Grupuj dane według typu bufora
grouped_data = df.groupby('typ bufora')

# Stwórz wykres dla każdego typu bufora
plt.figure(figsize=(10, 7))

for name, group in grouped_data:
    plt.plot(group['maksymalna porcja'], group['średni czas rzeczywisty'], 'o-',  label=name)

# plt.yticks(df['średni czas rzeczywisty'].sort_values().unique())

# Dodaj etykiety i legendę
plt.title('Zależność czasu rzeczywistego (50 pomiarów na punkt) od maksymalnej porcji. Bufor=1000, Random=Local')
plt.xlabel('Maksymalna porcja')
plt.ylabel('Średni czas rzeczywisty [ns]')
plt.legend()

# Wyświetl wykres
plt.show()
