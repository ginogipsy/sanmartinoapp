# San Martino App - Claude Documentation

## Project Overview
San Martino is a distributed ecosystem for local event management (Arce, FR).
- **Android App**: Native Kotlin app using Jetpack Compose (this repository).
- **Backend**: Microservices (Java 26 / Spring Boot 3.x) with API-First approach.
- **Admin Panel**: Angular 19 web application.

## Backend Architecture & Services
- **Spring Cloud Gateway**: Routing + Resilience4j (circuit breaker).
- **Saga Orchestrator**: Centralized management for distributed transactions (stats, voting, sales).
- **Keycloak**: Integrated as a module for Identity & Access Management.
- **Notifications Service**: Event-driven (Kafka) + Firebase Cloud Messaging (FCM) for Android push.
- **Core Services**: `event-service` (events management), `stands-service` (cantine/food stands).
- **Data**: PostgreSQL with Flyway migrations.

## Build & Run Commands (Android)
- Build app: `./gradlew assembleDebug`
- Run unit tests: `./gradlew test`
- Run instrumentation tests: `./gradlew connectedAndroidTest`
- Sync OpenAPI specs: `./gradlew :app:syncOpenApiSpecs` (Copies YAML from `../../san-martino-services/api`)
- Generate OpenAPI clients: `./gradlew :app:openApiGenerateEvents :app:openApiGenerateStands`

## Current Status & Next Steps
- **Notifications Implementation**: Currently configuring `notifications-service` with Kafka and FCM.
- **Testing**: Performing smoke tests for the Saga Orchestrator end-to-end flow.
- **Security**: FCM private keys are managed via environment variables (not committed). Ensure `.gitignore` is updated for sensitive files.

## Technology Stack (Android App)
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose (Material 3).
- **Networking**: Retrofit 2, OkHttp 4, Kotlinx Serialization.
- **Architecture**: Clean-ish Architecture with manual Dependency Injection (`AppContainer`).
- **API**: Generated via OpenAPI Generator from YAML specs.

## Coding Guidelines
- **API-First**: Always update YAML specs in the backend first, then run `syncOpenApiSpecs` and `openApiGenerate...`.
- **Network Calls**: Wrap in `safeApiCall` (returns `NetworkResult`).
- **Localization**: Multi-language support (IT/EN) via `LocalizedText` and `LocalLanguage`.
- **UI**: Functional Composables, reusable components in `ui/components`.
- **Search**: Currently implemented UI-side; future move to Backend planned.
