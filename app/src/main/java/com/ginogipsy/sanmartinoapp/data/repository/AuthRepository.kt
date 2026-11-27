package com.ginogipsy.sanmartinoapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.ginogipsy.sanmartinoapp.data.model.AuthUser
import com.ginogipsy.sanmartinoapp.network.NetworkResult
import com.ginogipsy.sanmartinoapp.network.auth.AuthApi
import com.ginogipsy.sanmartinoapp.network.auth.LoginRequestDto
import com.ginogipsy.sanmartinoapp.network.auth.LoginResponseDto
import com.ginogipsy.sanmartinoapp.network.auth.RegisterRequestDto
import com.ginogipsy.sanmartinoapp.network.auth.TokenProvider
import com.ginogipsy.sanmartinoapp.network.map
import com.ginogipsy.sanmartinoapp.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

interface AuthRepository : TokenProvider {
    val currentUser: StateFlow<AuthUser?>

    suspend fun login(username: String, password: String): NetworkResult<AuthUser>

    suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String? = null,
        lastName: String? = null,
    ): NetworkResult<AuthUser>

    fun logout()
}

class DefaultAuthRepository(
    context: Context,
    private val authApiProvider: () -> AuthApi,
) : AuthRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Volatile
    private var cachedToken: String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    private val _currentUser = MutableStateFlow<AuthUser?>(loadStoredUser())
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    override fun currentToken(): String? = cachedToken

    override suspend fun login(username: String, password: String): NetworkResult<AuthUser> {
        val result = safeApiCall {
            authApiProvider().login(
                LoginRequestDto(
                    username = username.trim(),
                    password = password,
                )
            )
        }
        return result.map { dto ->
            persistSession(dto, fallbackUsername = username.trim())
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String?,
        lastName: String?,
    ): NetworkResult<AuthUser> {
        val result = safeApiCall {
            authApiProvider().register(
                RegisterRequestDto(
                    username = username.trim(),
                    email = email.trim(),
                    password = password,
                    confirmPassword = confirmPassword,
                    firstName = firstName?.trim()?.takeIf { it.isNotEmpty() },
                    lastName = lastName?.trim()?.takeIf { it.isNotEmpty() },
                )
            )
        }
        return result.map { dto ->
            persistSession(dto, fallbackUsername = username.trim(), fallbackEmail = email.trim())
        }
    }

    override fun logout() {
        cachedToken = null
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_EMAIL)
            .remove(KEY_DISPLAY_NAME)
            .apply()
        _currentUser.value = null
    }

    private fun persistSession(
        dto: LoginResponseDto,
        fallbackUsername: String,
        fallbackEmail: String = "",
    ): AuthUser {
        cachedToken = dto.accessToken
        val user = decodeJwtUser(dto.accessToken, fallbackUsername, fallbackEmail)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, dto.accessToken)
            .putString(KEY_REFRESH_TOKEN, dto.refreshToken)
            .putString(KEY_USER_ID, user.userId)
            .putString(KEY_USERNAME, user.username)
            .putString(KEY_EMAIL, user.email)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .apply()
        _currentUser.value = user
        return user
    }

    private fun loadStoredUser(): AuthUser? {
        val token = cachedToken ?: return null
        val storedUsername = prefs.getString(KEY_USERNAME, null)
        val storedEmail = prefs.getString(KEY_EMAIL, "") ?: ""
        return if (!storedUsername.isNullOrBlank()) {
            AuthUser(
                userId = prefs.getString(KEY_USER_ID, storedUsername) ?: storedUsername,
                username = storedUsername,
                email = storedEmail,
                displayName = prefs.getString(KEY_DISPLAY_NAME, storedUsername) ?: storedUsername,
            )
        } else {
            decodeJwtUser(token, "Utente", "")
        }
    }

    private fun decodeJwtUser(
        jwt: String,
        fallbackUsername: String,
        fallbackEmail: String,
    ): AuthUser {
        return runCatching {
            val parts = jwt.split(".")
            if (parts.size < 2) return@runCatching null
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP))
            val obj = JSONObject(payload)
            val sub = obj.optString("sub").ifBlank { fallbackUsername }
            val username = obj.optString("preferred_username").ifBlank { fallbackUsername }
            val email = obj.optString("email").ifBlank { fallbackEmail }
            val name = obj.optString("name").ifBlank { username }
            AuthUser(
                userId = sub,
                username = username,
                email = email,
                displayName = name,
            )
        }.getOrNull() ?: AuthUser(
            userId = fallbackUsername,
            username = fallbackUsername,
            email = fallbackEmail,
            displayName = fallbackUsername,
        )
    }

    companion object {
        private const val PREFS_NAME = "sanmartino_auth_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"
    }
}
