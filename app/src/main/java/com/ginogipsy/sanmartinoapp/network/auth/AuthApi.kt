package com.ginogipsy.sanmartinoapp.network.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
data class LoginResponseDto(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
    @SerialName("token_type") val tokenType: String? = null,
)

@Serializable
data class RefreshRequestDto(
    val refreshToken: String,
)

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<LoginResponseDto>

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequestDto): Response<LoginResponseDto>
}
