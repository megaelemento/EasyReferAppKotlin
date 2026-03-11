package com.christelldev.easyreferplus.data.network

import com.christelldev.easyreferplus.data.model.ProductCategory
import com.christelldev.easyreferplus.data.network.RetrofitClient
import com.christelldev.easyreferplus.data.model.Product
import com.christelldev.easyreferplus.data.model.ProductImage
import com.christelldev.easyreferplus.data.model.CartItem
import com.christelldev.easyreferplus.data.model.CreateProductRequest
import com.christelldev.easyreferplus.data.model.UpdateProductRequest
import com.christelldev.easyreferplus.data.model.CartResponse
import com.christelldev.easyreferplus.data.model.CheckoutResponse
import com.christelldev.easyreferplus.data.model.CartCountResponse
import com.christelldev.easyreferplus.data.model.ImageUploadResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

// ==================== PRODUCT CATEGORIES ====================

sealed class ProductCategoryResult {
    data class Success(val categories: List<ProductCategory>) : ProductCategoryResult()
    data class Error(val message: String) : ProductCategoryResult()
}

// ==================== PRODUCTS ====================

sealed class ProductListResult {
    data class Success(val products: List<Product>) : ProductListResult()
    data class Error(val message: String) : ProductListResult()
}

sealed class ProductResult {
    data class Success(val message: String, val productId: Int? = null) : ProductResult()
    data class Error(val message: String) : ProductResult()
}

// ==================== PRODUCT IMAGES ====================

sealed class ProductImageResult {
    data class Success(val message: String, val imageUrl: String? = null, val imageId: Int? = null) : ProductImageResult()
    data class Error(val message: String) : ProductImageResult()
}

sealed class ProductImageListResult {
    data class Success(val images: List<ProductImage>) : ProductImageListResult()
    data class Error(val message: String) : ProductImageListResult()
}

// ==================== CART ====================

sealed class CartListResult {
    data class Success(val items: List<CartItem>) : CartListResult()
    data class Error(val message: String) : CartListResult()
}

sealed class CartActionResult {
    data class Success(val message: String, val cartCount: Int? = null) : CartActionResult()
    data class Error(val message: String) : CartActionResult()
}

sealed class CheckoutResult {
    data class Success(
        val referenceCode: String,
        val qrCodes: List<com.christelldev.easyreferplus.data.model.CheckoutQRCode>,
        val totalItems: Int,
        val totalAmount: Double,
        val companyCount: Int,
        val paymentUrl: String,
        val items: List<com.christelldev.easyreferplus.data.model.CheckoutItem>,
        val total: Double,
        val instructions: String
    ) : CheckoutResult()
    data class Error(val message: String) : CheckoutResult()
}

sealed class CartCountResult {
    data class Success(val count: Int) : CartCountResult()
    data class Error(val message: String) : CartCountResult()
}

// ==================== REPOSITORY ====================

class ProductRepository(
    private val apiService: ApiService
) {
    // =====================================================
    // PRODUCT CATEGORIES
    // =====================================================

    suspend fun getProductCategories(authorization: String, activeOnly: Boolean = true): ProductCategoryResult {
        return try {
            val response = apiService.getProductCategories(authorization, activeOnly)
            if (response.isSuccessful && response.body() != null) {
                ProductCategoryResult.Success(response.body()!!)
            } else {
                ProductCategoryResult.Error("Error al obtener categorías: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductCategoryResult.Error("Error de conexión: ${e.message}")
        }
    }

    // =====================================================
    // PRODUCTS
    // =====================================================

    suspend fun getMyCompanyProducts(authorization: String): ProductListResult {
        return try {
            val response = apiService.getMyCompanyProducts(authorization)
            if (response.isSuccessful && response.body() != null) {
                ProductListResult.Success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                if (errorBody?.contains("No tienes empresa") == true) {
                    ProductListResult.Error("No tienes empresa registrada. Crea una empresa primero.")
                } else {
                    ProductListResult.Error("Error al obtener productos: ${response.message()}")
                }
            }
        } catch (e: Exception) {
            ProductListResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getCompanyProducts(authorization: String, companyId: Int): ProductListResult {
        return try {
            val response = apiService.getCompanyProducts(authorization, companyId)
            if (response.isSuccessful && response.body() != null) {
                ProductListResult.Success(response.body()!!)
            } else {
                ProductListResult.Error("Error al obtener productos: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductListResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun createProduct(authorization: String, request: CreateProductRequest): ProductResult {
        return try {
            val response = apiService.createProduct(authorization, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductResult.Success(body.message ?: "Success", body.productId)
                } else {
                    ProductResult.Error(body.message ?: "Error")
                }
            } else {
                ProductResult.Error("Error al crear producto: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun updateProduct(authorization: String, productId: Int, request: UpdateProductRequest): ProductResult {
        return try {
            val response = apiService.updateProduct(authorization, productId, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductResult.Success(body.message ?: "Success", body.productId)
                } else {
                    ProductResult.Error(body.message ?: "Error")
                }
            } else {
                ProductResult.Error("Error al actualizar producto: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun deactivateProduct(authorization: String, productId: Int): ProductResult {
        return try {
            val response = apiService.deactivateProduct(authorization, productId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductResult.Success(body.message ?: "Success", body.productId)
                } else {
                    ProductResult.Error(body.message ?: "Error")
                }
            } else {
                ProductResult.Error("Error al desactivar producto: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun deleteProduct(authorization: String, productId: Int): ProductResult {
        return try {
            val response = apiService.deleteProduct(authorization, productId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductResult.Success(body.message ?: "Success", body.productId)
                } else {
                    ProductResult.Error(body.message ?: "Error")
                }
            } else {
                ProductResult.Error("Error al eliminar producto: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductResult.Error("Error de conexión: ${e.message}")
        }
    }

    // =====================================================
    // PRODUCT IMAGES
    // =====================================================

    suspend fun uploadProductImage(
        authorization: String,
        productId: Int,
        imageFile: File,
        isPrimary: Boolean = false
    ): ProductImageResult {
        return try {
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)
            val isPrimaryPart = okhttp3.RequestBody.create(
                "text/plain".toMediaTypeOrNull(),
                isPrimary.toString()
            )

            val response = apiService.uploadProductImage(authorization, productId, imagePart, isPrimaryPart)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductImageResult.Success(body.message ?: "Success", body.imageUrl, body.imageId)
                } else {
                    ProductImageResult.Error(body.message ?: "Error")
                }
            } else {
                ProductImageResult.Error("Error al subir imagen: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductImageResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getProductImages(authorization: String, productId: Int): ProductImageListResult {
        return try {
            val response = apiService.getProductImages(authorization, productId)
            if (response.isSuccessful && response.body() != null) {
                ProductImageListResult.Success(response.body()!!)
            } else {
                ProductImageListResult.Error("Error al obtener imágenes: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductImageListResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun deleteProductImage(authorization: String, imageId: Int): ProductImageResult {
        return try {
            val response = apiService.deleteProductImage(authorization, imageId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductImageResult.Success(body.message ?: "Success")
                } else {
                    ProductImageResult.Error(body.message ?: "Error")
                }
            } else {
                ProductImageResult.Error("Error al eliminar imagen: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductImageResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun setPrimaryProductImage(authorization: String, imageId: Int): ProductImageResult {
        return try {
            val response = apiService.setPrimaryProductImage(authorization, imageId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    ProductImageResult.Success(body.message ?: "Success")
                } else {
                    ProductImageResult.Error(body.message ?: "Error")
                }
            } else {
                ProductImageResult.Error("Error al establecer imagen principal: ${response.message()}")
            }
        } catch (e: Exception) {
            ProductImageResult.Error("Error de conexión: ${e.message}")
        }
    }

    // =====================================================
    // CART
    // =====================================================

    suspend fun getCart(authorization: String): CartListResult {
        return try {
            val response = apiService.getCart(authorization)
            if (response.isSuccessful && response.body() != null) {
                CartListResult.Success(response.body()!!)
            } else {
                CartListResult.Error("Error al obtener carrito: ${response.message()}")
            }
        } catch (e: Exception) {
            CartListResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun addToCart(authorization: String, productId: Int, quantity: Int = 1): CartActionResult {
        return try {
            val response = apiService.addToCart(authorization, productId, quantity)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    CartActionResult.Success(body.message ?: "Success", body.cartCount)
                } else {
                    CartActionResult.Error(body.message ?: "Error")
                }
            } else {
                CartActionResult.Error("Error al agregar al carrito: ${response.message()}")
            }
        } catch (e: Exception) {
            CartActionResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun updateCartItem(authorization: String, productId: Int, quantity: Int): CartActionResult {
        return try {
            val response = apiService.updateCartItem(authorization, productId, quantity)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    CartActionResult.Success(body.message ?: "Success", body.cartCount)
                } else {
                    CartActionResult.Error(body.message ?: "Error")
                }
            } else {
                CartActionResult.Error("Error al actualizar carrito: ${response.message()}")
            }
        } catch (e: Exception) {
            CartActionResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun removeFromCart(authorization: String, productId: Int): CartActionResult {
        return try {
            val response = apiService.removeFromCart(authorization, productId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    CartActionResult.Success(body.message ?: "Success", body.cartCount)
                } else {
                    CartActionResult.Error(body.message ?: "Error")
                }
            } else {
                CartActionResult.Error("Error al eliminar del carrito: ${response.message()}")
            }
        } catch (e: Exception) {
            CartActionResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun clearCart(authorization: String): CartActionResult {
        return try {
            val response = apiService.clearCart(authorization)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    CartActionResult.Success(body.message ?: "Success", body.cartCount)
                } else {
                    CartActionResult.Error(body.message ?: "Error")
                }
            } else {
                CartActionResult.Error("Error al vaciar carrito: ${response.message()}")
            }
        } catch (e: Exception) {
            CartActionResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun checkout(authorization: String): CheckoutResult {
        return try {
            val response = apiService.checkout(authorization)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success) {
                    CheckoutResult.Success(
                        referenceCode = body.referenceCode ?: "",
                        qrCodes = body.qrCodes ?: emptyList(),
                        totalItems = body.totalItems ?: 0,
                        totalAmount = body.totalAmount ?: 0.0,
                        companyCount = body.companyCount ?: 0,
                        paymentUrl = body.paymentUrl ?: "",
                        items = body.items ?: emptyList(),
                        total = body.total ?: 0.0,
                        instructions = body.instructions ?: ""
                    )
                } else {
                    CheckoutResult.Error(body.message ?: "Error en checkout")
                }
            } else {
                CheckoutResult.Error("Error en checkout: ${response.message()}")
            }
        } catch (e: Exception) {
            CheckoutResult.Error("Error de conexión: ${e.message}")
        }
    }

    suspend fun getCartCount(authorization: String): CartCountResult {
        return try {
            val response = apiService.getCartCount(authorization)
            if (response.isSuccessful && response.body() != null) {
                CartCountResult.Success(response.body()!!.cartCount)
            } else {
                CartCountResult.Error("Error al obtener cantidad: ${response.message()}")
            }
        } catch (e: Exception) {
            CartCountResult.Error("Error de conexión: ${e.message}")
        }
    }

    class Factory {
        fun create(): ProductRepository {
            val retrofit = RetrofitClient.getInstance()
            val apiService = retrofit.create(ApiService::class.java)
            return ProductRepository(apiService)
        }
    }
}
