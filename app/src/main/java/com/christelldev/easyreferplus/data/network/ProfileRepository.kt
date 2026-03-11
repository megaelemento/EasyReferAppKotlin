package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.ConfirmCodeRequest
import com.christelldev.easyreferplus.data.model.ConfirmCodeResponse
import com.christelldev.easyreferplus.data.model.ResendCodeRequest
import com.christelldev.easyreferplus.data.model.ResendCodeResponse
import com.christelldev.easyreferplus.data.model.UpdateProfileRequest
import com.christelldev.easyreferplus.data.model.UserProfile
import com.christelldev.easyreferplus.data.model.VerifyPhoneRequest
import com.christelldev.easyreferplus.data.model.VerifyPhoneResponse
import com.christelldev.easyreferplus.data.network.RegisterApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull

sealed class ProfileResult {
    data class Success(val profile: UserProfile) : ProfileResult()
    data class UpdateSuccess(val message: String, val profile: UserProfile?, val updatedFields: List<String>?) : ProfileResult()
    data class Error(val message: String, val code: Int? = null) : ProfileResult()
}

sealed class SelfieResult {
    data class UploadSuccess(val message: String, val selfieUrl: String?) : SelfieResult()
    data class DeleteSuccess(val message: String) : SelfieResult()
    data class Error(val message: String, val code: Int? = null) : SelfieResult()
}

sealed class PhoneVerificationResult {
    data class CodeSent(val phone: String, val message: String) : PhoneVerificationResult()
    data class Verified(val message: String) : PhoneVerificationResult()
    data class Error(val message: String, val code: Int? = null) : PhoneVerificationResult()
}

class ProfileRepository(
    private val apiService: ApiService,
    private val registerApiService: RegisterApiService? = null
) {

    // Métodos para verificación de teléfono
    suspend fun sendPhoneVerificationCode(phone: String): PhoneVerificationResult {
        return try {
            val request = VerifyPhoneRequest(phone)
            val response = registerApiService?.verifyPhone(request)
                ?: return PhoneVerificationResult.Error("Servicio de verificación no disponible")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        PhoneVerificationResult.CodeSent(body.phone, body.message)
                    } else {
                        PhoneVerificationResult.Error(body.message)
                    }
                } ?: PhoneVerificationResult.Error("Respuesta vacía")
            } else {
                parsePhoneVerificationError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parsePhoneVerificationException(e)
        }
    }

    suspend fun confirmPhoneVerification(phone: String, code: String): PhoneVerificationResult {
        return try {
            val request = ConfirmCodeRequest(phone, code)
            val response = registerApiService?.confirmCode(request)
                ?: return PhoneVerificationResult.Error("Servicio de verificación no disponible")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.verified) {
                        PhoneVerificationResult.Verified(body.message)
                    } else {
                        PhoneVerificationResult.Error(body.message)
                    }
                } ?: PhoneVerificationResult.Error("Respuesta vacía")
            } else {
                parsePhoneVerificationError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parsePhoneVerificationException(e)
        }
    }

    suspend fun resendPhoneCode(phone: String): PhoneVerificationResult {
        return try {
            val request = ResendCodeRequest(phone)
            val response = registerApiService?.resendCode(request)
                ?: return PhoneVerificationResult.Error("Servicio de verificación no disponible")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        PhoneVerificationResult.CodeSent(phone, body.message)
                    } else {
                        PhoneVerificationResult.Error(body.message)
                    }
                } ?: PhoneVerificationResult.Error("Respuesta vacía")
            } else {
                parsePhoneVerificationError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            parsePhoneVerificationException(e)
        }
    }

    private fun parsePhoneVerificationError(code: Int, errorBody: String?): PhoneVerificationResult {
        return try {
            if (code == 400 && errorBody != null) {
                val gson = com.google.gson.Gson()
                val errorMap = gson.fromJson(errorBody, Map::class.java)
                val message = errorMap["message"] as? String ?: "Error de verificación"
                PhoneVerificationResult.Error(message, code)
            } else {
                when (code) {
                    400 -> PhoneVerificationResult.Error("Datos inválidos", code)
                    404 -> PhoneVerificationResult.Error("Teléfono no encontrado", code)
                    429 -> PhoneVerificationResult.Error("Demasiados intentos. Intente más tarde", code)
                    else -> PhoneVerificationResult.Error("Error: $code", code)
                }
            }
        } catch (e: Exception) {
            PhoneVerificationResult.Error("Error de verificación", code)
        }
    }

    private fun parsePhoneVerificationException(e: Exception): PhoneVerificationResult {
        return when (e) {
            is java.net.UnknownHostException,
            is java.net.ConnectException,
            is java.io.IOException -> PhoneVerificationResult.Error("Sin conexión al servidor")
            else -> PhoneVerificationResult.Error("Error: ${e.message}")
        }
    }
    suspend fun getProfile(authorization: String): ProfileResult {
        return try {
            val response = apiService.getProfile(authorization)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    ProfileResult.Success(
                        UserProfile(
                            userId = body.userId,
                            nombres = body.nombres,
                            apellidos = body.apellidos,
                            email = body.email,
                            phone = body.phone,
                            cedulaRuc = body.cedulaRuc,
                            isRuc = body.isRuc,
                            empresaNombre = body.empresaNombre,
                            empresaStatus = body.empresaStatus,
                            empresaActiva = body.empresaActiva,
                            referralCode = body.referralCode,
                            phoneVerified = body.phoneVerified,
                            isVerified = body.isVerified,
                            hasCompany = body.hasCompany,
                            role = body.role,
                            createdAt = body.createdAt,
                            selfieUrl = body.selfieUrl
                        )
                    )
                } ?: ProfileResult.Error("Respuesta vacía")
            } else {
                parseError(response.code())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    suspend fun updateProfile(authorization: String, request: UpdateProfileRequest): ProfileResult {
        return try {
            val response = apiService.updateProfile(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        ProfileResult.UpdateSuccess(
                            message = body.message,
                            profile = body.profile?.let {
                                UserProfile(
                                    userId = it.userId,
                                    nombres = it.nombres,
                                    apellidos = it.apellidos,
                                    email = it.email,
                                    phone = it.phone,
                                    cedulaRuc = it.cedulaRuc,
                                    isRuc = it.isRuc,
                                    empresaNombre = it.empresaNombre,
                                    empresaStatus = it.empresaStatus,
                                    empresaActiva = it.empresaActiva,
                                    referralCode = it.referralCode,
                                    phoneVerified = it.phoneVerified,
                                    isVerified = it.isVerified,
                                    hasCompany = it.hasCompany,
                                    role = it.role,
                                    createdAt = it.createdAt
                                )
                            },
                            updatedFields = body.updatedFields
                        )
                    } else {
                        ProfileResult.Error(body.message)
                    }
                } ?: ProfileResult.Error("Respuesta vacía")
            } else {
                parseError(response.code())
            }
        } catch (e: Exception) {
            parseException(e)
        }
    }

    private fun parseError(code: Int): ProfileResult {
        return when (code) {
            401 -> ProfileResult.Error("Sesión expirada", code)
            400 -> ProfileResult.Error("Datos inválidos", code)
            404 -> ProfileResult.Error("Usuario no encontrado", code)
            else -> ProfileResult.Error("Error: $code", code)
        }
    }

    private fun parseException(e: Exception): ProfileResult {
        return when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                ProfileResult.Error("El servidor está en mantenimiento. Intente más tarde.")
            e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.io.IOException -> ProfileResult.Error("Sin conexión al servidor")
            else -> ProfileResult.Error("Error: ${e.message}")
        }
    }

    // Métodos para selfie (foto de perfil)
    suspend fun uploadSelfie(authorization: String, imagePath: String): SelfieResult {
        return try {
            val file = java.io.File(imagePath)
            val requestFile = okhttp3.RequestBody.create(
                "image/*".toMediaTypeOrNull(),
                file
            )
            val body = okhttp3.MultipartBody.Part.createFormData("selfie", file.name, requestFile)

            val response = apiService.uploadSelfie(authorization, body)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        SelfieResult.UploadSuccess(
                            message = body.message,
                            selfieUrl = body.selfieInfo?.url
                        )
                    } else {
                        SelfieResult.Error(body.message)
                    }
                } ?: SelfieResult.Error("Respuesta vacía")
            } else {
                parseSelfieError(response.code())
            }
        } catch (e: Exception) {
            parseSelfieException(e)
        }
    }

    suspend fun deleteSelfie(authorization: String): SelfieResult {
        return try {
            val response = apiService.deleteSelfie(authorization)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        SelfieResult.DeleteSuccess(body.message)
                    } else {
                        SelfieResult.Error(body.message)
                    }
                } ?: SelfieResult.Error("Respuesta vacía")
            } else {
                parseSelfieError(response.code())
            }
        } catch (e: Exception) {
            parseSelfieException(e)
        }
    }

    private fun parseSelfieError(code: Int): SelfieResult {
        return when (code) {
            401 -> SelfieResult.Error("Sesión expirada", code)
            400 -> SelfieResult.Error("Datos inválidos", code)
            404 -> SelfieResult.Error("No se encontró la foto", code)
            413 -> SelfieResult.Error("Archivo muy grande. Máximo 5MB", code)
            else -> SelfieResult.Error("Error: $code", code)
        }
    }

    private fun parseSelfieException(e: Exception): SelfieResult {
        return when {
            e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                SelfieResult.Error("El servidor está en mantenimiento. Intente más tarde.")
            e is java.net.UnknownHostException ||
            e is java.net.ConnectException ||
            e is java.io.IOException -> SelfieResult.Error("Sin conexión al servidor")
            else -> SelfieResult.Error("Error: ${e.message}")
        }
    }

    class Factory {
        fun create(): ProfileRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            val registerApiService = retrofit.create(RegisterApiService::class.java)
            return ProfileRepository(apiService, registerApiService)
        }
    }
}
