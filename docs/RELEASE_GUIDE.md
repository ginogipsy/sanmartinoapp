# Guida ai Rilasci e Versionamento (Android App)

> [!IMPORTANT]
> **Pulsante "Approve" (Terzo Pallino)**
> Se sui branch di sviluppo la pipeline si ferma senza mostrare il tasto "Review deployments", significa che l'ambiente non è configurato.
> **Assicurati di aver creato l'environment `san-martino-registry`** in *Settings -> Environments* con la regola *Required reviewers* attiva (aggiungendo il tuo nome utente). Senza questo passaggio, il deploy manuale non può funzionare.

Questa guida spiega come gestire il ciclo di vita dell'app, i rilasci su GitHub e il sistema di versionamento automatico.

## 📌 Regole del Versionamento

L'app utilizza un sistema **Smart Versioning** basato su `version.properties`.

- **Major (X.0.0)**: Cambiamenti radicali o redesign completi.
- **Minor (0.X.0)**: Nuove funzionalità (es. ricerca, notifiche, nuove sezioni).
- **Patch (0.0.X)**: Bug fix, ottimizzazioni, correzioni di testi.

## 🚀 Come effettuare un Rilascio

Il processo segue il modello **Git Flow**. Hai quattro modi per decidere la versione del prossimo rilascio:

### 1. Metodo Standard (Patch automatica)
1. Crea una Pull Request da `develop` a `master`.
2. Fai il Merge.
3. **Risultato**: Il bot incrementerà automaticamente la **Patch** (es. `1.0.1` -> `1.0.2`).

### 2. Tramite Etichette PR (Consigliato per Minor/Major)
1. Crea la Pull Request verso `master`.
2. Su GitHub, aggiungi l'etichetta (label) **`minor`** o **`major`**.
3. Fai il Merge.
4. **Risultato**: Il bot incrementerà la versione in base all'etichetta.

### 3. Tramite Nome del Branch (Git Flow rigido)
1. Crea un branch chiamato `release/v1.2.0` (o `release/1.2.0`).
2. Apri la PR verso `master` e fai il merge.
3. **Risultato**: Il bot forzerà esattamente la versione `1.2.0`.

### 4. Metodo Manuale (GitHub UI)
1. Vai nella tab **Actions** su GitHub.
2. Seleziona il workflow **"Release & Smart Versioning"**.
3. Clicca su **"Run workflow"**.
4. Inserisci la versione desiderata nel campo `manual_version` (es: `1.5.0`).

---

## 📦 Distribuzione dell'APK

La pubblicazione degli APK segue una logica ibrida per massimizzare il controllo.

### 🚀 Pubblicazione Automatica (Continuous Deployment)
L'APK viene creato e caricato nelle GitHub Releases **automaticamente** in due casi:
- **Merge su `master`**: L'APK viene taggato come `latest`.
- **Creazione di un Tag Git (`v*`)**: L'APK viene taggata con la versione corrispondente.

### ✋ Pubblicazione Manuale (Il "Terzo Pallino")
Sui branch di sviluppo (**`develop`**, **`feature/*`**), la pipeline si ferma dopo i test:
1. Vai nella tab **Actions** su GitHub e clicca sulla run corrente.
2. Vedrai un pulsante **"Review deployments"**.
3. Clicca su **Approve** (per l'environment `san-martino-registry`).
4. **Risultato**: L'APK verrà caricata su GitHub Releases con il **nome del branch**, pronta per i test.

---

## 🤖 Cosa succede dietro le quinte?

Ad ogni merge su `master`, il workflow di GitHub:
1. Determina la versione corretta tramite **Smart Versioning**.
2. Crea un **Tag Git**.
3. Crea una **GitHub Release**.
4. **Aggiorna `develop`**: Incrementa `versionCode` e prepara la prossima patch in `version.properties`.

> [!IMPORTANT]
> Dopo ogni rilascio, ricordati di fare `git pull` su `develop` locale.
