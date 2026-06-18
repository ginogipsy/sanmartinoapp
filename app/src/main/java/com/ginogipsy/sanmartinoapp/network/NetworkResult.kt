package com.ginogipsy.sanmartinoapp.network

import retrofit2.Response
import java.io.IOException

/**
 * Wrapper esplicito per il risultato di una chiamata HTTP, gestito al livello del
 * Repository. La sealed class separa i casi di errore (rete, HTTP, sconosciuto) in
 * modo che i ViewModel possano deciderne il trattamento senza riallocare try/catch
 * a ogni chiamata.
 */
sealed interface NetworkResult<out T> {

    data class Success<T>(val data: T) : NetworkResult<T>

    sealed interface Error : NetworkResult<Nothing> {
        val message: String

        /** Connessione assente o interrotta (IOException). */
        data class Network(
            override val message: String = "Nessuna connessione di rete",
        ) : Error

        /** Risposta non 2xx dal server (4xx / 5xx). */
        data class Http(
            val code: Int,
            val body: String?,
            override val message: String = "Errore HTTP $code",
        ) : Error

        /** Tutto il resto: bug, parsing, eccezioni inattese. */
        data class Unknown(
            val throwable: Throwable,
            override val message: String = throwable.message ?: "Errore sconosciuto",
        ) : Error
    }
}

/** Trasforma il payload mantenendo intatto il ramo Error. */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> = when (this) {
    is NetworkResult.Success -> NetworkResult.Success(transform(data))
    is NetworkResult.Error -> this
}

/**
 * Esegue una chiamata Retrofit `suspend` e la incapsula in un [NetworkResult].
 *
 * Il client generato da OpenAPI Generator ritorna `Response<T>` (non `T` diretto):
 * qui distinguiamo `isSuccessful` + body non-null dal resto e mappiamo le eccezioni
 * piu' comuni.
 */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error.Http(
                    code = response.code(),
                    body = null,
                    message = "Risposta vuota dal server (HTTP ${response.code()})",
                )
            }
        } else {
            NetworkResult.Error.Http(
                code = response.code(),
                body = runCatching { response.errorBody()?.string() }.getOrNull(),
            )
        }
    } catch (e: IOException) {
        NetworkResult.Error.Network(
            message = "Nessuna connessione: ${e.localizedMessage ?: "verifica la rete"}",
        )
    } catch (e: Exception) {
        NetworkResult.Error.Unknown(throwable = e)
    }
}
