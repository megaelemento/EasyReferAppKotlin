package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.CompleteRegistrationRequest
import com.christelldev.easyreferplus.data.model.CompleteRegistrationResponse
import com.christelldev.easyreferplus.data.model.ConfirmCodeRequest
import com.christelldev.easyreferplus.data.model.ConfirmCodeResponse
import com.christelldev.easyreferplus.data.model.PrivacyPolicyResponse
import com.christelldev.easyreferplus.data.model.ResendCodeRequest
import com.christelldev.easyreferplus.data.model.ResendCodeResponse
import com.christelldev.easyreferplus.data.model.VerifyPhoneRequest
import com.christelldev.easyreferplus.data.model.VerifyPhoneResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RegisterApiService {
    @POST("api/auth/verify-phone-number")
    suspend fun verifyPhone(@Body request: VerifyPhoneRequest): Response<VerifyPhoneResponse>

    @POST("api/auth/resend-phone-code")
    suspend fun resendCode(@Body request: ResendCodeRequest): Response<ResendCodeResponse>

    @POST("api/auth/confirm-phone-code")
    suspend fun confirmCode(@Body request: ConfirmCodeRequest): Response<ConfirmCodeResponse>

    @POST("api/auth/complete-registration")
    suspend fun completeRegistration(@Body request: CompleteRegistrationRequest): Response<CompleteRegistrationResponse>

    @GET("api/privacy/policy")
    suspend fun getPrivacyPolicy(): Response<PrivacyPolicyResponse>
}
