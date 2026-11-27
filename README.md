# San Martino — Android App

Applicazione Android nativa bilingue (IT/EN) per i visitatori della manifestazione enogastronomica **"Le Cantine di San Martino"** (Arce, FR).

## Funzionalità Principali

- **Esplorazione Edizioni & Archivio Storico Multi-Anno**:
  - Consultazione dell'edizione attiva (`UPCOMING`) con disponibilità porzioni in tempo reale.
  - Consultazione in **Modalità Sola Lettura (Archivio Storico)** di tutte le edizioni passate (`2025`, `2024`, `2018`, `2014`) con le cantine partecipanti, i menu storici e tutte le recensioni raccolte negli anni.
- **Cantine, Piatti, Vini & Ricerca Intelligente**:
  - Elenco cantine con mappa/navigazione, dettaglio piatti (`FOOD`) e bevande/vini (`DRINK`).
  - Motore di ricerca server-side e locale con tolleranza agli errori di battitura (fuzzy matching), sinonimi gastronomici e tracciamento analytics.
- **Autenticazione, Preferiti & Recensioni**:
  - Registrazione e login OAuth2/JWT integrati con `auth-service` / Keycloak.
  - Sincronizzazione delle cantine preferite dell'utente.
  - Valutazioni in stelle e commenti su cantine, piatti e vini (con moderazione e continuità storica automatica sui piatti/vini riproposti tra più edizioni).
- **Architettura Offline-First & Client API-First**:
  - Persistenza locale con **Room** per consultazione fluida anche in assenza di rete nei vicoli del centro storico.
  - Modelli e client generati automaticamente dai contratti OpenAPI (`events-api.yaml`, `stands-api.yaml`).

## Tech Stack

- **UI**: Kotlin + Jetpack Compose (Material 3)
- **Networking & Serializzazione**: Retrofit + OkHttp + OpenAPI Generator
- **Persistenza Locale**: Room (`OfflineFirstStandRepository`, `OfflineFirstEventRepository`)
- **CI/CD**: GitHub Actions (Android Lint, Unit Test, Debug APK Artifact, Smart Versioning & Release Automation)

## Comandi Principali

```bash
./gradlew :app:openApiGenerateEvents :app:openApiGenerateStands # Rigenera client OpenAPI
./gradlew testDebugUnitTest                                     # Esecuzione unit test
./gradlew assembleDebug                                         # Build APK debug
./gradlew adbReverse                                            # Port forwarding loopback emulatore -> porta 8080 host
```

## Documentazione

- [Guida ai Rilasci e Versionamento](docs/RELEASE_GUIDE.md)
- [Dettagli Pipeline CI/CD](docs/ci-cd.md)
