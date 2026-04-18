package com.christelldev.easyreferplus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.christelldev.easyreferplus.data.model.*
import com.christelldev.easyreferplus.data.network.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

// UI State
sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(val message: String) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
    data class ProductsLoaded(val products: List<Product>) : ProductUiState()
    data class CategoriesLoaded(val categories: List<ProductCategory>) : ProductUiState()
    data class CartLoaded(val items: List<CartItem>) : ProductUiState()
    data class CheckoutReady(
        val referenceCode: String,
        val paymentUrl: String,
        val items: List<CheckoutItem>,
        val total: Double,
        val instructions: String
    ) : ProductUiState()
}

class ProductViewModel(
    private val repository: ProductRepository,
    private val getAccessToken: () -> String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<ProductCategory>>(emptyList())
    val categories: StateFlow<List<ProductCategory>> = _categories.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartCount = MutableStateFlow(0)
    val cartCount: StateFlow<Int> = _cartCount.asStateFlow()

    private val _aliases = MutableStateFlow<List<com.christelldev.easyreferplus.data.model.SearchAlias>>(emptyList())
    val aliases: StateFlow<List<com.christelldev.easyreferplus.data.model.SearchAlias>> = _aliases.asStateFlow()

    // Mensajes puntuales del carrito (éxito o error de add/remove)
    private val _cartMessage = MutableStateFlow<String?>(null)
    val cartMessage: StateFlow<String?> = _cartMessage.asStateFlow()

    fun clearCartMessage() { _cartMessage.value = null }

    // Estado de checkout
    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    sealed class CheckoutState {
        data object Idle : CheckoutState()
        data object Processing : CheckoutState()
        data class Success(
            val message: String,
            val orderId: String?,
            val qrCodes: List<com.christelldev.easyreferplus.data.model.CheckoutQRCode>,
            val totalItems: Int,
            val totalAmount: Double,
            val companyCount: Int
        ) : CheckoutState()
        data class Error(val message: String) : CheckoutState()
    }

    private val authorization: String get() = "Bearer ${getAccessToken()}"

    // =====================================================
    // PRODUCT CATEGORIES
    // =====================================================

    fun loadProductCategories(activeOnly: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.getProductCategories(authorization, activeOnly)) {
                is ProductCategoryResult.Success -> {
                    _categories.value = result.categories
                    _uiState.value = ProductUiState.CategoriesLoaded(result.categories)
                }
                is ProductCategoryResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    // =====================================================
    // PRODUCTS
    // =====================================================

    fun loadMyCompanyProducts() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.getMyCompanyProducts(authorization)) {
                is ProductListResult.Success -> {
                    _products.value = result.products
                    _uiState.value = ProductUiState.ProductsLoaded(result.products)
                }
                is ProductListResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun loadCompanyProducts(companyId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.getCompanyProducts(authorization, companyId)) {
                is ProductListResult.Success -> {
                    _products.value = result.products
                    _uiState.value = ProductUiState.ProductsLoaded(result.products)
                }
                is ProductListResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun createProduct(
        productName: String,
        productDescription: String?,
        productCategoryId: Int?,
        size: String?,
        weight: String?,
        dimensions: String?,
        quantity: Int,
        price: Double,
        offerPrice: Double?,
        specificCommissionPercentage: Double?,
        useCompanyDefault: Boolean = true,
        status: String = "active",
        keywords: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading

            val request = CreateProductRequest(
                productName = productName,
                productDescription = productDescription,
                productCategoryId = productCategoryId,
                size = size,
                weight = weight,
                dimensions = dimensions,
                keywords = keywords,
                quantity = quantity,
                price = price,
                offerPrice = offerPrice,
                specificCommissionPercentage = specificCommissionPercentage,
                useCompanyDefault = useCompanyDefault,
                status = status
            )

            when (val result = repository.createProduct(authorization, request)) {
                is ProductResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                    loadMyCompanyProducts() // Refresh list
                }
                is ProductResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun updateProduct(
        productId: Int,
        productName: String?,
        productDescription: String?,
        productCategoryId: Int?,
        size: String?,
        weight: String?,
        dimensions: String?,
        quantity: Int?,
        price: Double?,
        offerPrice: Double?,
        specificCommissionPercentage: Double?,
        useCompanyDefault: Boolean?,
        status: String?,
        keywords: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading

            val request = UpdateProductRequest(
                productName = productName,
                productDescription = productDescription,
                productCategoryId = productCategoryId,
                size = size,
                weight = weight,
                dimensions = dimensions,
                keywords = keywords,
                quantity = quantity,
                price = price,
                offerPrice = offerPrice,
                specificCommissionPercentage = specificCommissionPercentage,
                useCompanyDefault = useCompanyDefault,
                status = status
            )

            when (val result = repository.updateProduct(authorization, productId, request)) {
                is ProductResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                    loadMyCompanyProducts() // Refresh list
                }
                is ProductResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun deactivateProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.deactivateProduct(authorization, productId)) {
                is ProductResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                    loadMyCompanyProducts()
                }
                is ProductResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.deleteProduct(authorization, productId)) {
                is ProductResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                    loadMyCompanyProducts()
                }
                is ProductResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    // =====================================================
    // PRODUCT IMAGES
    // =====================================================

    fun uploadProductImage(productId: Int, imageFile: File, isPrimary: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.uploadProductImage(authorization, productId, imageFile, isPrimary)) {
                is ProductImageResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                }
                is ProductImageResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun deleteProductImage(imageId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.deleteProductImage(authorization, imageId)) {
                is ProductImageResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                }
                is ProductImageResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun setPrimaryProductImage(imageId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.setPrimaryProductImage(authorization, imageId)) {
                is ProductImageResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                }
                is ProductImageResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    // =====================================================
    // CART
    // =====================================================

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.getCart(authorization)) {
                is CartListResult.Success -> {
                    _cartItems.value = result.items
                    _cartCount.value = result.items.sumOf { it.quantity }
                    _uiState.value = ProductUiState.CartLoaded(result.items)
                }
                is CartListResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun addToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            when (val result = repository.addToCart(authorization, productId, quantity)) {
                is CartActionResult.Success -> {
                    _cartCount.value = result.cartCount ?: _cartCount.value
                    _cartMessage.value = result.message
                }
                is CartActionResult.Error -> {
                    _cartMessage.value = result.message
                }
            }
        }
    }

    fun updateCartItem(productId: Int, quantity: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.updateCartItem(authorization, productId, quantity)) {
                is CartActionResult.Success -> {
                    _cartCount.value = result.cartCount ?: _cartCount.value
                    loadCart() // Refresh cart
                }
                is CartActionResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.removeFromCart(authorization, productId)) {
                is CartActionResult.Success -> {
                    _cartCount.value = result.cartCount ?: 0
                    loadCart() // Refresh cart
                }
                is CartActionResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.clearCart(authorization)) {
                is CartActionResult.Success -> {
                    _cartItems.value = emptyList()
                    _cartCount.value = 0
                    _uiState.value = ProductUiState.Success(result.message)
                }
                is CartActionResult.Error -> {
                    _uiState.value = ProductUiState.Error(result.message)
                }
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _checkoutState.value = CheckoutState.Processing
            when (val result = repository.checkout(authorization)) {
                is CheckoutResult.Success -> {
                    // No limpiamos el carrito inmediatamente
                    // Los productos se eliminan cuando cada empresa registra su venta
                    _checkoutState.value = CheckoutState.Success(
                        message = result.instructions.ifBlank { "Tu pedido ha sido creado exitosamente" },
                        orderId = result.referenceCode,
                        qrCodes = result.qrCodes,
                        totalItems = result.totalItems,
                        totalAmount = result.totalAmount,
                        companyCount = result.companyCount
                    )
                }
                is CheckoutResult.Error -> {
                    _checkoutState.value = CheckoutState.Error(result.message)
                }
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }

    fun loadCartCount() {
        viewModelScope.launch {
            when (val result = repository.getCartCount(authorization)) {
                is CartCountResult.Success -> {
                    _cartCount.value = result.count
                }
                is CartCountResult.Error -> {
                    // Silently fail for cart count
                }
            }
        }
    }

    // =====================================================
    // SEARCH ALIASES
    // =====================================================

    fun loadAliases() {
        viewModelScope.launch {
            when (val result = repository.getSearchAliases(authorization)) {
                is com.christelldev.easyreferplus.data.network.AliasListResult.Success -> _aliases.value = result.aliases
                is com.christelldev.easyreferplus.data.network.AliasListResult.Error -> _uiState.value = ProductUiState.Error(result.message)
            }
        }
    }

    fun createAlias(alias: String, term: String) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.createSearchAlias(authorization, alias, term)) {
                is com.christelldev.easyreferplus.data.network.AliasActionResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                    loadAliases()
                }
                is com.christelldev.easyreferplus.data.network.AliasActionResult.Error -> _uiState.value = ProductUiState.Error(result.message)
            }
        }
    }

    fun deleteAlias(aliasId: Int) {
        viewModelScope.launch {
            _uiState.value = ProductUiState.Loading
            when (val result = repository.deleteSearchAlias(authorization, aliasId)) {
                is com.christelldev.easyreferplus.data.network.AliasActionResult.Success -> {
                    _uiState.value = ProductUiState.Success(result.message)
                    loadAliases()
                }
                is com.christelldev.easyreferplus.data.network.AliasActionResult.Error -> _uiState.value = ProductUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = ProductUiState.Idle
    }

    class Factory(
        private val repository: ProductRepository,
        private val getAccessToken: () -> String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                return ProductViewModel(repository, getAccessToken) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
