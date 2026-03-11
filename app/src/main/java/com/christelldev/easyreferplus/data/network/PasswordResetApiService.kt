package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.PasswordResetRequest
import com.christelldev.easyreferplus.data.model.PasswordResetResponse
import com.christelldev.easyreferplus.data.model.ResetPasswordRequest
import com.christelldev.easyreferplus.data.model.VerifyResetCodeRequest
import com.christelldev.easyreferplus.data.model.VerifyResetCodeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PasswordResetApiService {
    @POST("api/auth/request-password-reset")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest): Response<PasswordResetResponse>

    @POST("api/auth/verify-reset-code")
    suspend fun verifyResetCode(@Body request: VerifyResetCodeRequest): Response<VerifyResetCodeResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<PasswordResetResponse>

    @POST("api/auth/resend-reset-code")
    suspend fun resendResetCode(@Body request: PasswordResetRequest): Response<PasswordResetResponse>
}
