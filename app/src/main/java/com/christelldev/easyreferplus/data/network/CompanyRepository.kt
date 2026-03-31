package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.RegisterCompanyRequest
import com.christelldev.easyreferplus.data.model.RegisterCompanyResponse
import com.christelldev.easyreferplus.data.model.UpdateCompanyRequest
import com.christelldev.easyreferplus.data.model.CompanyValidationError
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.data.model.UserCompaniesResponse
import com.christelldev.easyreferplus.data.model.CompaniesSearchResponse
import com.christelldev.easyreferplus.data.model.LogoUploadResponse
import com.christelldev.easyreferplus.data.model.CategoryInfo
import com.christelldev.easyreferplus.data.model.ServiceInfo
import com.christelldev.easyreferplus.data.model.MyStoreResponse
import com.christelldev.easyreferplus.data.model.StoreSetupResponse
import com.christelldev.easyreferplus.data.model.StoreToggleResponse
import com.christelldev.easyreferplus.data.model.SlugCheckResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

sealed class CompanyResult {
    data class RegisterSuccess(val response: RegisterCompanyResponse) : CompanyResult()
    data class UpdateSuccess(val message: String, val company: com.christelldev.easyreferplus.data.model.UserCompanyResponse?) : CompanyResult()
    data class Error(val message: String, val code: Int? = null, val errors: List<CompanyValidationError>? = null) : CompanyResult()
}

sealed class CityResult {
    data class Success(val cities: List<String>) : CityResult()
    data class Error(val message: String) : CityResult()
}

sealed class ProvinceResult {
    data class Success(val provinces: List<String>) : ProvinceResult()
    data class Error(val message: String) : ProvinceResult()
}

sealed class UserCompaniesResult {
    data class Success(val companies: List<UserCompanyResponse>) : UserCompaniesResult()
    data class Error(val message: String) : UserCompaniesResult()
}

sealed class CompanySearchResult {
    data class Success(
        val companies: List<UserCompanyResponse>,
        val total: Int = 0,
        val page: Int = 1,
        val totalPages: Int = 1
    ) : CompanySearchResult()
    data class Error(val message: String) : CompanySearchResult()
}

sealed class CompanyDetailResult {
    data class Success(val company: UserCompanyResponse) : CompanyDetailResult()
    data class Error(val message: String) : CompanyDetailResult()
}

sealed class LogoResult {
    data class Success(val message: String, val logoUrl: String?) : LogoResult()
    data class Error(val message: String) : LogoResult()
}

sealed class CategoryResult {
    data class Success(val categories: List<CategoryInfo>) : CategoryResult()
    data class Error(val message: String) : CategoryResult()
}

sealed class ServiceResult {
    data class Success(val services: List<ServiceInfo>) : ServiceResult()
    data class Error(val message: String) : ServiceResult()
}

class CompanyRepository(
    private val apiService: ApiService
) {
    // Buscar empresas públicas
    suspend fun searchCompanies(
        authorization: String,
        query: String? = null,
        city: String? = null,
        province: String? = null,
        categoryId: Int? = null,
        serviceId: Int? = null,
        page: Int = 1,
        perPage: Int = 20
    ): CompanySearchResult {
        return try {
            val response = apiService.searchCompanies(authorization, query, city, province, categoryId, serviceId, page, perPage)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    CompanySearchResult.Success(
                        companies = body.companies ?: emptyList(),
                        total = body.total,
                        page = body.page,
                        totalPages = body.totalPages
                    )
                } else {
                    CompanySearchResult.Error("Error en la búsqueda")
                }
            } else {
                CompanySearchResult.Error("Error al buscar empresas: ${response.code()}")
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> "Error de conexión: ${e.message}"
            }
            CompanySearchResult.Error(message)
        }
    }

    // Obtener empresa por ID
    suspend fun getCompanyById(authorization: String, companyId: Int): CompanyDetailResult {
        return try {
            val response = apiService.getCompanyById(authorization, companyId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    CompanyDetailResult.Success(body)
                } else {
                    CompanyDetailResult.Error("Empresa no encontrada")
                }
            } else if (response.code() == 404) {
                CompanyDetailResult.Error("Empresa no encontrada")
            } else {
                CompanyDetailResult.Error("Error al obtener empresa: ${response.code()}")
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> "Error de conexión: ${e.message}"
            }
            CompanyDetailResult.Error(message)
        }
    }

    suspend fun registerCompany(authorization: String, request: RegisterCompanyRequest): CompanyResult {
        return try {
            val response = apiService.registerCompany(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        CompanyResult.RegisterSuccess(body)
                    } else {
                        CompanyResult.Error(body.message)
                    }
                } ?: CompanyResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            val message = when (e) {
                is java.net.UnknownHostException,
                is java.net.ConnectException,
                is java.io.IOException -> "Sin conexión al servidor"
                else -> "Error de conexión"
            }
            CompanyResult.Error(message)
        }
    }

    suspend fun updateCompany(authorization: String, request: UpdateCompanyRequest): CompanyResult {
        return try {
            val response = apiService.updateCompany(authorization, request)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.success) {
                        CompanyResult.UpdateSuccess(body.message, body.company)
                    } else {
                        CompanyResult.Error(body.message)
                    }
                } ?: CompanyResult.Error("Respuesta vacía")
            } else {
                parseError(response.code(), response.errorBody()?.string())
            }
        } catch (e: Exception) {
            val message = when (e) {
                is java.net.UnknownHostException,
                is java.net.ConnectException,
                is java.io.IOException -> "Sin conexión al servidor"
                else -> "Error de conexión"
            }
            CompanyResult.Error(message)
        }
    }

    private fun parseError(code: Int, errorBody: String?): CompanyResult {
        return try {
            if (code == 422 && errorBody != null) {
                val gson = Gson()
                val errorMap = gson.fromJson(errorBody, Map::class.java)

                // Verificar el formato del error
                // Formato nuevo: {"detail": [{"type": "...", "loc": ["body", "field"], "msg": "..."}]}
                // Formato antiguo: {"success": false, "message": "...", "errors": [...]}

                val detail = errorMap["detail"]
                if (detail is List<*>) {
                    // Nuevo formato con "detail"
                    val errors = mutableListOf<CompanyValidationError>()
                    var message = "Error de validación"

                    detail.forEach { item ->
                        if (item is Map<*, *>) {
                            val loc = item["loc"] as? List<*>
                            val msg = item["msg"] as? String
                            val type = item["type"] as? String

                            if (loc != null && loc.size >= 2) {
                                val field = loc[1].toString()
                                errors.add(CompanyValidationError(field, msg ?: "Error de validación"))
                            }
                            // Acumular mensaje para mostrar
                            if (msg != null && message == "Error de validación") {
                                message = msg
                            }
                        }
                    }

                    CompanyResult.Error(
                        message = message,
                        code = code,
                        errors = errors
                    )
                } else {
                    // Intentar formato antiguo
                    val errorResponse = gson.fromJson(errorBody, com.christelldev.easyreferplus.data.model.RegisterCompanyErrorResponse::class.java)
                    CompanyResult.Error(
                        message = errorResponse.message,
                        code = code,
                        errors = errorResponse.errors
                    )
                }
            } else {
                when (code) {
                    400 -> CompanyResult.Error("Ya tienes una empresa registrada o datos inválidos", code)
                    401 -> CompanyResult.Error("Sesión expirada", code)
                    500 -> CompanyResult.Error("Error del servidor", code)
                    else -> CompanyResult.Error("Error al registrar empresa", code)
                }
            }
        } catch (e: Exception) {
            when (code) {
                400 -> CompanyResult.Error("Ya tienes una empresa registrada o datos inválidos", code)
                401 -> CompanyResult.Error("Sesión expirada", code)
                500 -> CompanyResult.Error("Error del servidor", code)
                else -> CompanyResult.Error("Error al registrar empresa", code)
            }
        }
    }

    suspend fun getProvinces(): ProvinceResult {
        return try {
            val response = apiService.getProvinces()
            if (response.isSuccessful) {
                response.body()?.let { provinces ->
                    ProvinceResult.Success(provinces)
                } ?: ProvinceResult.Error("Respuesta vacía")
            } else {
                ProvinceResult.Error("Error al cargar provincias")
            }
        } catch (e: Exception) {
            ProvinceResult.Error("Error de conexión")
        }
    }

    suspend fun getCities(province: String? = null): CityResult {
        return try {
            val response = apiService.getAllCities(province)
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    // El body puede ser una lista directa o un objeto con "cities"
                    val cities = if (body is List<*>) {
                        @Suppress("UNCHECKED_CAST")
                        body as List<String>
                    } else {
                        emptyList()
                    }
                    CityResult.Success(cities)
                } ?: CityResult.Error("Respuesta vacía")
            } else {
                CityResult.Error("Error al cargar ciudades")
            }
        } catch (e: Exception) {
            CityResult.Error("Error de conexión")
        }
    }

    suspend fun getCitiesByProvince(province: String): CityResult {
        return try {
            val response = apiService.getCitiesByProvince(province)
            if (response.isSuccessful) {
                response.body()?.let { cities ->
                    CityResult.Success(cities)
                } ?: CityResult.Error("Respuesta vacía")
            } else {
                CityResult.Error("Error al cargar ciudades")
            }
        } catch (e: Exception) {
            CityResult.Error("Error de conexión")
        }
    }

    // Obtener categorías
    suspend fun getCategories(): CategoryResult {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                response.body()?.let { categories ->
                    CategoryResult.Success(categories)
                } ?: CategoryResult.Error("Respuesta vacía")
            } else {
                CategoryResult.Error("Error al cargar categorías")
            }
        } catch (e: Exception) {
            CategoryResult.Error("Error de conexión")
        }
    }

    // Obtener servicios (opcionalmente filtrados por categoría)
    suspend fun getServices(categoryId: Int? = null): ServiceResult {
        return try {
            val response = apiService.getServices(categoryId)
            if (response.isSuccessful) {
                response.body()?.let { services ->
                    ServiceResult.Success(services)
                } ?: ServiceResult.Error("Respuesta vacía")
            } else {
                ServiceResult.Error("Error al cargar servicios")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Error de conexión")
        }
    }

    suspend fun getMyCompanies(authorization: String): UserCompaniesResult {
        return try {
            val response = apiService.getMyCompanies(authorization)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.success) {
                        UserCompaniesResult.Success(body.companies ?: emptyList())
                    } else {
                        UserCompaniesResult.Error("Error al obtener empresas")
                    }
                } else {
                    UserCompaniesResult.Error("Respuesta vacía")
                }
            } else {
                UserCompaniesResult.Error("Error al cargar empresas")
            }
        } catch (e: Exception) {
            UserCompaniesResult.Error("Error de conexión")
        }
    }

    // Obtener mi empresa (endpoint correcto según documentación)
    suspend fun getMyCompany(authorization: String): UserCompaniesResult {
        return try {
            val response = apiService.getMyCompany(authorization)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // La respuesta devuelve directamente UserCompanyResponse
                    UserCompaniesResult.Success(listOf(body))
                } else {
                    UserCompaniesResult.Error("Respuesta vacía")
                }
            } else if (response.code() == 404) {
                // Usuario no tiene empresa registrada
                UserCompaniesResult.Success(emptyList())
            } else {
                UserCompaniesResult.Error("Error al cargar empresa: ${response.code()}")
            }
        } catch (e: Exception) {
            val message = when {
                e is java.io.IOException && e.message?.contains("mantenimiento", ignoreCase = true) == true ->
                    "El servidor está en mantenimiento. Intente más tarde."
                else -> "Error de conexión: ${e.message}"
            }
            UserCompaniesResult.Error(message)
        }
    }

    // ==================== COMPANY PAYMENTS ====================

    sealed class PaymentResult {
        data class RegisterSuccess(val message: String, val paymentId: Int?) : PaymentResult()
        data class HistorySuccess(
            val payments: List<com.christelldev.easyreferplus.data.model.CompanyPayment>,
            val pendingCommissionsAmount: Double?,
            val companyName: String?
        ) : PaymentResult()
        data class Error(val message: String, val code: Int? = null) : PaymentResult()
    }

    suspend fun registerPayment(
        authorization: String,
        documentNumber: String,
        bankName: String,
        amount: Double,
        notes: String? = null
    ): PaymentResult {
        return try {
            val request = com.christelldev.easyreferplus.data.model.CompanyPaymentRequest(
                documentNumber = documentNumber,
                bankName = bankName,
                amount = amount,
                notes = notes
            )
            val response = apiService.registerCompanyPayment(authorization, request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    PaymentResult.RegisterSuccess(body.message, body.paymentId)
                } else {
                    PaymentResult.Error(body?.message ?: "Error al registrar pago")
                }
            } else {
                PaymentResult.Error("Error al registrar pago: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            PaymentResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getPaymentHistory(
        authorization: String
    ): PaymentResult {
        return try {
            val response = apiService.getPaymentHistory(authorization)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    PaymentResult.HistorySuccess(
                        payments = body.payments ?: emptyList(),
                        pendingCommissionsAmount = body.pendingCommissionsAmount,
                        companyName = body.company?.companyName
                    )
                } else {
                    PaymentResult.Error("Error al cargar historial de pagos")
                }
            } else {
                PaymentResult.Error("Error al cargar historial: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            PaymentResult.Error("Error de conexión: ${e.message}")
        }
    }

    data class PaymentAccessInfo(
        val canAccess: Boolean,
        val companyId: Int?,
        val companyName: String?,
        val companyStatus: String?,
        val pendingAmount: Double?,
        val paymentsCount: Int?
    )

    suspend fun checkPaymentAccess(
        authorization: String
    ): PaymentAccessInfo {
        return try {
            val response = apiService.checkPaymentAccess(authorization)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    PaymentAccessInfo(
                        canAccess = body.canAccess,
                        companyId = body.companyId,
                        companyName = body.companyName,
                        companyStatus = body.companyStatus,
                        pendingAmount = body.pendingAmount,
                        paymentsCount = body.paymentsCount
                    )
                } else {
                    PaymentAccessInfo(canAccess = false, companyId = null, companyName = null, companyStatus = null, pendingAmount = null, paymentsCount = null)
                }
            } else {
                PaymentAccessInfo(canAccess = false, companyId = null, companyName = null, companyStatus = null, pendingAmount = null, paymentsCount = null)
            }
        } catch (e: Exception) {
            PaymentAccessInfo(canAccess = false, companyId = null, companyName = null, companyStatus = null, pendingAmount = null, paymentsCount = null)
        }
    }

    // ==================== LOGO UPLOAD ====================

    suspend fun uploadLogo(authorization: String, logoFile: java.io.File): LogoResult {
        return try {
            val requestBody = logoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData("logo", logoFile.name, requestBody)

            val response = apiService.uploadCompanyLogo(authorization, multipartBody)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    //优先使用 logo_info.url，如果没有则使用 logo_url
                    val uploadedUrl = body.logoInfo?.url ?: body.logoUrl
                    LogoResult.Success(body.message, uploadedUrl)
                } else {
                    LogoResult.Error(body?.message ?: "Error al subir logo")
                }
            } else {
                when (response.code()) {
                    401 -> LogoResult.Error("Sesión expirada")
                    413 -> LogoResult.Error("El archivo es demasiado grande. Máximo 5MB")
                    415 -> LogoResult.Error("Formato de imagen no soportado")
                    500 -> LogoResult.Error("Error del servidor")
                    else -> LogoResult.Error("Error al subir logo: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            LogoResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun deleteLogo(authorization: String): LogoResult {
        return try {
            val response = apiService.deleteCompanyLogo(authorization)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    LogoResult.Success(body.message, null)
                } else {
                    LogoResult.Error(body?.message ?: "Error al eliminar logo")
                }
            } else {
                when (response.code()) {
                    401 -> LogoResult.Error("Sesión expirada")
                    404 -> LogoResult.Error("No hay logo para eliminar")
                    500 -> LogoResult.Error("Error del servidor")
                    else -> LogoResult.Error("Error al eliminar logo: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            LogoResult.Error("Error de conexión: ${e.message}")
        }
    }

    // ─── Tienda Online ────────────────────────────────────────────────────────

    suspend fun getMyStore(authorization: String): Result<MyStoreResponse> {
        return try {
            val response = apiService.getMyStore(authorization)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) Result.success(body)
                else Result.failure(Exception("Sin datos"))
            } else {
                Result.failure(Exception("Error ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setupStore(
        authorization: String,
        slug: String,
        templateId: Int,
        primaryColor: String,
        secondaryColor: String,
        tagline: String,
        font: String
    ): Result<StoreSetupResponse> {
        return try {
            val response = apiService.setupStore(authorization, slug, templateId, primaryColor, secondaryColor, tagline, font)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) Result.success(body)
                else Result.failure(Exception(body?.message ?: "Error al guardar"))
            } else {
                val msg = try { response.errorBody()?.string()?.let { org.json.JSONObject(it).optString("detail", "Error") } ?: "Error ${response.code()}" } catch (e: Exception) { "Error ${response.code()}" }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleStore(authorization: String): Result<StoreToggleResponse> {
        return try {
            val response = apiService.toggleStore(authorization)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) Result.success(body)
                else Result.failure(Exception(body?.message ?: "Error"))
            } else {
                val msg = try { response.errorBody()?.string()?.let { org.json.JSONObject(it).optString("detail", "Error") } ?: "Error ${response.code()}" } catch (e: Exception) { "Error ${response.code()}" }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkSlug(slug: String): SlugCheckResponse? {
        return try {
            val response = apiService.checkSlug(slug)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    class Factory {
        fun create(): CompanyRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return CompanyRepository(apiService)
        }
    }
}
