# CI/CD — GitHub Actions (san-martino-app)

La pipeline dell'app Android segue il modello `git flow` utilizzato per il backend, con automazione del versionamento e dei rilasci.

## Workflow

| File | Scopo | Trigger |
|---|---|---|
| `ci.yml` | Build, Lint, Unit Tests | PR → `develop`, `master`; Push su `develop` |
| `release.yml` | Tag, GitHub Release, Version Bump | Push su `master` |

## Allineamento a git flow

```
feature/*  ──PR──►  develop  ──►  release/*  ──PR──►  master  ──tag──►  Release
                       ▲                                  │
                       └────────── back-merge ────────────┘
```

### CI Pipeline (`ci.yml`)
Esegue i controlli di qualità su JDK 21:
- `./gradlew lintDebug`: Analisi statica del codice.
- `./gradlew testDebugUnitTest`: Esecuzione unit test.
- `./gradlew assembleDebug`: Verifica che l'APK sia compilabile.

### Release Pipeline (`release.yml`)
Quando un branch di release o hotfix viene mergiato su `master`:
1. **Tagging**: Crea un tag git `vX.Y.Z` basato sulla versione corrente.
2. **GitHub Release**: Crea una release su GitHub con changelog automatico.
3. **Version Bump**:
   - Legge `version.properties`.
   - Incrementa `versionPatch` e `versionCode`.
   - Esegue il commit e il push su `develop` (back-merge) per preparare il prossimo ciclo di sviluppo.

## Gestione della Versione

La versione non è hardcoded nel file Gradle, ma è gestita centralmente in:
**`version.properties`**

```properties
versionMajor=1
versionMinor=0
versionPatch=0
versionCode=1
```

Il file `app/build.gradle.kts` legge questi valori dinamicamente:
- `versionCode`: Preso direttamente dalla proprietà.
- `versionName`: Composto come `major.minor.patch`.

## Riproduzione locale

Per verificare la build completa localmente:
```bash
./gradlew assembleDebug
```
