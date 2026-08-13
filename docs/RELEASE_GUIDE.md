# Guida ai Rilasci e Versionamento (Android App)

Questa guida spiega come gestire il ciclo di vita dell'app, i rilasci su GitHub e il sistema di versionamento automatico.

## 📌 Regole del Versionamento

L'app utilizza un sistema **Smart Versioning** basato su `version.properties`.

- **Major (X.0.0)**: Cambiamenti radicali o redesign completi.
- **Minor (0.X.0)**: Nuove funzionalità (es. ricerca, notifiche, nuove sezioni).
- **Patch (0.0.X)**: Bug fix, ottimizzazioni, correzioni di testi.

## 🚀 Come effettuare un Rilascio

Il processo segue il modello **Git Flow**. Hai tre modi per decidere la versione del prossimo rilascio:

### 1. Metodo Standard (Patch automatica)
Se devi solo rilasciare dei bug fix:
1. Crea una Pull Request da `develop` a `master`.
2. Fai il Merge.
3. **Risultato**: Il bot incrementerà automaticamente la **Patch** (es. `1.0.1` -> `1.0.2`).

### 2. Tramite Etichette PR (Consigliato per Minor/Major)
Se stai rilasciando nuove funzionalità:
1. Crea la Pull Request verso `master`.
2. Su GitHub, aggiungi l'etichetta (label) **`minor`** o **`major`**.
3. Fai il Merge.
4. **Risultato**: Il bot incrementerà la versione in base all'etichetta (es. `1.0.1` -> `1.1.0`).

### 3. Tramite Nome del Branch (Git Flow rigido)
Se preferisci specificare la versione nel branch:
1. Crea un branch chiamato `release/v1.2.0` (o `release/1.2.0`).
2. Apri la PR verso `master` e fai il merge.
3. **Risultato**: Il bot forzerà esattamente la versione `1.2.0`.

### 4. Metodo Manuale (GitHub UI)
Per il massimo controllo:
1. Vai nella tab **Actions** su GitHub.
2. Seleziona il workflow **"Release & Smart Versioning"**.
3. Clicca su **"Run workflow"**.
4. Inserisci la versione desiderata nel campo `manual_version` (es: `1.5.0`).
5. **Risultato**: Il bot ignorerà ogni altra logica e userà esattamente quella versione.

---

## 🤖 Cosa succede dietro le quinte?

Ad ogni merge su `master`, il workflow di GitHub:
1. Determina la versione corretta.
2. Crea un **Tag Git** (es. `v1.2.0`).
3. Crea una **GitHub Release** con il changelog automatico.
4. **Aggiorna `develop`**: Incrementa `versionCode` e prepara la prossima patch in `version.properties` su `develop` (back-merge).

> [!IMPORTANT]
> Dopo ogni rilascio, ricordati di fare `git pull` sul tuo branch `develop` locale per ricevere l'aggiornamento della versione fatto dal bot.
