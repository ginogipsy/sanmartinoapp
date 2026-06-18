package com.ginogipsy.sanmartinoapp.network.auth

/**
 * Fornisce il bearer token corrente per chiamare il backend.
 *
 * Step 1 = solo scaffold: in produzione lo implementera' il modulo Keycloak
 * (refresh, persistenza sicura, ecc.). Per ora le API pubbliche di lettura
 * (`/v1/events`, `/v1/stands`) non richiedono auth.
 */
fun interface TokenProvider {
    /** Token corrente, oppure null se non autenticato. */
    fun currentToken(): String?
}

/** Implementazione di default finche' Keycloak non e' integrato. */
object AnonymousTokenProvider : TokenProvider {
    override fun currentToken(): String? = null
}
