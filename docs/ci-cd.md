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
