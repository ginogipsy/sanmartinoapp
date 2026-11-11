# CI/CD — GitHub Actions (san-martino-app)

> [!IMPORTANT]
> **Configurazione Obbligatoria per il Deploy Manuale**
> Per abilitare il pulsante di approvazione (il "terzo pallino") sui branch feature, devi configurare l'ambiente su GitHub:
> 1. Vai in **Settings** -> **Environments**.
> 2. Clicca su **New environment** e chiamalo esattamente **`san-martino-registry`**.
> 3. Sotto **Deployment protection rules**, attiva **Required reviewers**.
> 4. Aggiungi il tuo account GitHub come revisore.
> 5. Clicca su **Save protection rules**.

La pipeline dell'app Android segue il modello `git flow` utilizzato per il backend, con automazione del versionamento e dei rilasci.

## Workflow

| File | Scopo | Trigger |
|---|---|---|
| `ci-cd.yml` | Build, Lint, Unit Tests | PR → `develop`, `master`; Push su branch/tag |
| `release.yml` | Tag, GitHub Release, Version Bump | Push su `master` |
| `deploy.yml` | Pubblicazione APK (Automatico/Manuale) | Chiamato da `ci-cd.yml` |
| `qodana_code_quality.yml` | Analisi statica della qualità del codice | Push su branch/tag, PR |

## Allineamento a git flow

| Evento | build & test | APK Build | Deploy APK | Release/Tag |
|---|---|---|---|---|
| PR → `develop` / `master` | ✅ | ✅ | ❌ | ❌ |
| push su `develop` / `feature/*` | ✅ | ✅ | ✋ **Manuale** (Approve) | ❌ |
| push su `master` | ✅ | ✅ | 🚀 **Automatico** | **Smart Versioning** |
| push tag `v*` | ✅ | ✅ | 🚀 **Automatico** | ❌ |

## Gestione della Versione

La versione è gestita centralmente in:
**`version.properties`**

Il file `app/build.gradle.kts` legge questi valori dinamicamente.

## Sviluppo con Backend in Kubernetes (Kind)

Nelle immagini con API 36+, le **Local Network Protections** di Android impediscono il traffico delle app verso la subnet dell'emulatore (`10.0.2.2`).

Il build type `debug` è pertanto configurato su `http://127.0.0.1:8080/`. Quando il backend gira sul cluster Kind locale:
1. Avviare l'emulatore Android.
2. Eseguire il task Gradle dedicato per inoltrare la porta al controller Ingress:
   ```bash
   ./gradlew adbReverse
   ```
3. L'applicazione comunicherà con l'Ingress Traefik del cluster locale tramite loopback `127.0.0.1:8080`.
