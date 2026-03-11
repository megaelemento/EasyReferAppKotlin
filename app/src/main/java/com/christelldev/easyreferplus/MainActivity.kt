package com.christelldev.easyreferplus

import android.content.Intent
import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.christelldev.easyreferplus.data.network.AuthRepository
import com.christelldev.easyreferplus.data.network.ApiService
import com.christelldev.easyreferplus.data.network.RetrofitClient
import com.christelldev.easyreferplus.data.network.CompanyRepository
import com.christelldev.easyreferplus.data.network.ProductRepository
import com.christelldev.easyreferplus.data.network.PasswordResetRepository
import com.christelldev.easyreferplus.data.network.ReferralRepository
import com.christelldev.easyreferplus.data.network.RegisterRepository
import com.christelldev.easyreferplus.data.network.QRRepository
import com.christelldev.easyreferplus.data.network.ProfileRepository
import com.christelldev.easyreferplus.data.network.SessionRepository
import com.christelldev.easyreferplus.data.network.AdminEarningsRepository
import com.christelldev.easyreferplus.data.network.EarningsRepository
import com.christelldev.easyreferplus.data.network.WithdrawalRepository
import com.christelldev.easyreferplus.data.network.NetworkMonitor
import com.christelldev.easyreferplus.data.network.WebSocketManager
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.ui.screens.companies.CompanyScreen
import com.christelldev.easyreferplus.ui.screens.companies.CompaniesListScreen
import com.christelldev.easyreferplus.ui.screens.companies.CompanyDetailScreen
import com.christelldev.easyreferplus.ui.screens.companies.PublicCompaniesScreen
import com.christelldev.easyreferplus.ui.screens.companies.EditCompanyScreen
import com.christelldev.easyreferplus.ui.screens.cart.CartScreen
import com.christelldev.easyreferplus.ui.screens.products.MyProductsScreen
import com.christelldev.easyreferplus.ui.screens.products.ProductFormScreen
import com.christelldev.easyreferplus.ui.screens.products.CompanyProductsScreen
import com.christelldev.easyreferplus.ui.screens.products.ProductDetailScreen
import com.christelldev.easyreferplus.ui.screens.profile.ProfileScreen
import com.christelldev.easyreferplus.ui.screens.home.HomeScreen
import com.christelldev.easyreferplus.ui.screens.login.LoginScreen
import com.christelldev.easyreferplus.ui.screens.maintenance.MaintenanceScreen
import com.christelldev.easyreferplus.ui.screens.passwordreset.PasswordResetScreen
import com.christelldev.easyreferplus.ui.screens.qr.QRScreen
import com.christelldev.easyreferplus.ui.screens.referrals.ReferralScreen
import com.christelldev.easyreferplus.ui.screens.register.RegisterScreen
import com.christelldev.easyreferplus.ui.screens.sessions.SessionManagementScreen
import com.christelldev.easyreferplus.ui.screens.admin.AdminEarningsScreen
import com.christelldev.easyreferplus.ui.screens.admin.AdminWithdrawalsScreen
import com.christelldev.easyreferplus.ui.screens.history.HistoryScreen
import com.christelldev.easyreferplus.ui.screens.history.TransactionDetailScreen
import com.christelldev.easyreferplus.ui.screens.payments.CompanyPaymentsScreen
import com.christelldev.easyreferplus.ui.screens.earnings.EarningsScreen
import com.christelldev.easyreferplus.ui.screens.withdrawal.WithdrawalScreen
import com.christelldev.easyreferplus.ui.theme.EasyReferPlusTheme
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.ui.viewmodel.CompanyViewModel
import com.christelldev.easyreferplus.ui.viewmodel.ProfileViewModel
import com.christelldev.easyreferplus.data.model.UserCompanyResponse
import com.christelldev.easyreferplus.ui.viewmodel.HomeViewModel
import com.christelldev.easyreferplus.ui.viewmodel.LoginViewModel
import com.christelldev.easyreferplus.ui.viewmodel.PasswordResetViewModel
import com.christelldev.easyreferplus.ui.viewmodel.QRViewModel
import com.christelldev.easyreferplus.ui.viewmodel.ReferralViewModel
import com.christelldev.easyreferplus.ui.viewmodel.RegisterViewModel
import com.christelldev.easyreferplus.ui.viewmodel.SessionViewModel
import com.christelldev.easyreferplus.ui.viewmodel.AdminEarningsViewModel
import com.christelldev.easyreferplus.ui.viewmodel.AdminWithdrawalsViewModel
import com.christelldev.easyreferplus.ui.viewmodel.EarningsViewModel
import com.christelldev.easyreferplus.ui.viewmodel.WithdrawalViewModel
import com.christelldev.easyreferplus.ui.viewmodel.HistoryViewModel
import com.christelldev.easyreferplus.ui.viewmodel.HistoryTab
import com.christelldev.easyreferplus.ui.viewmodel.PublicCompaniesViewModel
import com.christelldev.easyreferplus.ui.viewmodel.CompanyDetailViewModel
import com.christelldev.easyreferplus.ui.viewmodel.ProductViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicializar configuración de la app
        AppConfig.init(this)

        val authRepository = AuthRepository.Factory(this).create()

        // Configurar el interceptor de autenticación para manejar refresh de tokens
        RetrofitClient.setAuthRepository(authRepository, this)

        setContent {
            EasyReferPlusTheme {
                MainNavigation(
                    authRepository = authRepository,
                    registerRepository = RegisterRepository.Factory(this).create(),
                    onNavigateToLogin = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    },
                    onLogoutComplete = {
                        // Después de logout, reiniciar actividad para volver al login
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object PasswordReset : Screen("password_reset")
    data object Home : Screen("home")
    data object Referral : Screen("referral")
    data object Company : Screen("company")
    data object CompaniesList : Screen("companies_list")
    data object PublicCompanies : Screen("public_companies")
    data object QR : Screen("qr")
    data object EditCompany : Screen("edit_company/{companyId}") {
        fun createRoute(companyId: Int) = "edit_company/$companyId"
    }
    data object Profile : Screen("profile")
    data object Sessions : Screen("sessions")
    data object AdminEarnings : Screen("admin_earnings")
    data object AdminWithdrawals : Screen("admin_withdrawals")
    data object History : Screen("history")
    data object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
    data object CompanyPayments : Screen("company_payments/{companyId}") {
        fun createRoute(companyId: Int) = "company_payments/$companyId"
    }
    data object Earnings : Screen("earnings")
    data object Withdrawal : Screen("withdrawal")
    data object CompanyDetail : Screen("company_detail/{companyId}") {
        fun createRoute(companyId: Int) = "company_detail/$companyId"
    }
    // Nuevas pantallas de productos y carrito
    data object Cart : Screen("cart")
    data object MyProducts : Screen("my_products")
    data object ProductForm : Screen("product_form/{productId}") {
        fun createRoute(productId: Int?) = if (productId != null) "product_form/$productId" else "product_form/new"
    }
    // Pantallas de productos de empresa
    data object CompanyProducts : Screen("company_products/{companyId}/{companyName}") {
        fun createRoute(companyId: Int, companyName: String) = "company_products/$companyId/${companyName.replace(" ", "_")}"
    }
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: Int) = "product_detail/$productId"
    }
}

@Composable
fun MainNavigation(
    authRepository: AuthRepository,
    registerRepository: RegisterRepository,
    onNavigateToLogin: () -> Unit,
    onLogoutComplete: () -> Unit
) {
    var isServerAvailable by remember { mutableStateOf<Boolean?>(null) }
    var checkKey by remember { mutableStateOf(0) }
    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }
    var sessionValidated by remember { mutableStateOf(false) }

    // Verificar servidor al inicio
    LaunchedEffect(checkKey) {
        isServerAvailable = NetworkMonitor.isServerAvailable()
    }

    // Validar sesión con el servidor al inicio (solo si hay token)
    LaunchedEffect(isServerAvailable, sessionValidated) {
        if (isServerAvailable == true && !sessionValidated && authRepository.isLoggedIn()) {
            // Validar token con el servidor
            val isValid = authRepository.validateSessionWithServer()
            sessionValidated = true
            if (!isValid) {
                // La sesión fue invalidada - forzar logout
                isLoggedIn = false
            }
        } else {
            sessionValidated = true
        }
    }

    // Validación periódica de sesión cada 2 minutos mientras la app está activa
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            while (true) {
                kotlinx.coroutines.delay(120000) // 2 minutos
                if (authRepository.isLoggedIn()) {
                    val isValid = authRepository.validateSessionWithServer()
                    if (!isValid) {
                        // Sesión invalidada - cerrar sesión
                        authRepository.clearAllData()
                        isLoggedIn = false
                        // Reiniciar al login
                        onLogoutComplete()
                        break
                    }
                }
            }
        }
    }

    // Observar cambios en el estado de autenticación
    LaunchedEffect(authRepository.isLoggedIn()) {
        isLoggedIn = authRepository.isLoggedIn()
    }

    // Restaurar verificación de mantenimiento
    // Si el servidor no está disponible, mostrar pantalla de mantenimiento
    if (isServerAvailable == false) {
        com.christelldev.easyreferplus.ui.screens.maintenance.MaintenanceScreen(
            onRetry = {
                NetworkMonitor.resetCache()
                checkKey++
            }
        )
        return
    }

    // Mientras verifica el servidor o valida la sesión, mostrar pantalla de carga
    if (isServerAvailable == null || !sessionValidated) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModel.Factory(authRepository)
    )
    // registerRepository se pasa como parámetro
    val registerViewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModel.Factory(registerRepository)
    )
    val passwordResetRepository = PasswordResetRepository.Factory().create()
    val passwordResetViewModel: PasswordResetViewModel = viewModel(
        factory = PasswordResetViewModel.Factory(passwordResetRepository, authRepository)
    )
    val referralRepository = ReferralRepository.Factory().create()
    val referralViewModel: ReferralViewModel = viewModel(
        factory = ReferralViewModel.Factory(referralRepository) { authRepository.getAccessToken() ?: "" }
    )
    val companyRepository = CompanyRepository.Factory().create()
    val companyViewModel: CompanyViewModel = viewModel(
        factory = CompanyViewModel.Factory(companyRepository) { authRepository.getAccessToken() ?: "" }
    )
    val qrRepository = QRRepository.Factory().create()
    val qrViewModel: QRViewModel = viewModel(
        factory = QRViewModel.Factory(qrRepository) { authRepository.getAccessToken() ?: "" }
    )
    val profileRepository = ProfileRepository.Factory().create()
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.Factory(profileRepository) { authRepository.getAccessToken() ?: "" }
    )

    // Home ViewModel
    val apiService = RetrofitClient.getInstance().create(ApiService::class.java)
    val homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(apiService) { authRepository.getAccessToken() ?: "" }
    )

    // Session ViewModel
    val sessionRepository = SessionRepository.Factory()
    val sessionViewModel: SessionViewModel = viewModel(
        factory = SessionViewModel.Factory(sessionRepository) { authRepository.getAccessToken() ?: "" }
    )

    // Admin Earnings ViewModel
    val adminEarningsRepository = AdminEarningsRepository.Factory()
    val adminEarningsViewModel: AdminEarningsViewModel = viewModel(
        factory = AdminEarningsViewModel.Factory(adminEarningsRepository) { authRepository.getAccessToken() ?: "" }
    )

    // Earnings ViewModel (para usuario normal)
    val earningsRepository = EarningsRepository.Factory()
    val earningsViewModel: EarningsViewModel = viewModel(
        factory = EarningsViewModel.Factory(earningsRepository) { authRepository.getAccessToken() ?: "" }
    )

    // Withdrawal ViewModel
    val withdrawalRepository = WithdrawalRepository.Factory()
    val withdrawalViewModel: WithdrawalViewModel = viewModel(
        factory = WithdrawalViewModel.Factory(withdrawalRepository) { authRepository.getAccessToken() ?: "" }
    )

    // History ViewModel
    val historyViewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(qrRepository) { authRepository.getAccessToken() ?: "" }
    )

    // Product & Cart ViewModel
    val productRepository = ProductRepository.Factory().create()
    val productViewModel: ProductViewModel = viewModel(
        factory = ProductViewModel.Factory(productRepository) { authRepository.getAccessToken() ?: "" }
    )

    // WebSocket Manager para notificaciones en tiempo real
    // Obtener URL dinámicamente de AppConfig para soportar múltiples entornos
    var webSocketManager by remember { mutableStateOf<WebSocketManager?>(null) }

    // Función para iniciar WebSocket - obtener URL en el momento de conectar
    fun connectWebSocket() {
        if (webSocketManager == null && authRepository.isLoggedIn()) {
            // Obtener URL dinámica de AppConfig
            val wsUrl = com.christelldev.easyreferplus.data.network.AppConfig.WS_URL
            webSocketManager = WebSocketManager(wsUrl) { authRepository.getAccessToken() ?: "" }
            webSocketManager?.connect()
        }
    }

    // Contador para forzar recomposición cuando se activa el WebSocket
    var webSocketConnectionKey by remember { mutableIntStateOf(0) }

    // Función para cerrar WebSocket
    fun disconnectWebSocket() {
        webSocketManager?.disconnect()
        webSocketManager = null
        webSocketConnectionKey++ // Incrementar para forzar recomposición
    }

    // Estado para las empresas del usuario
    var userCompanies by remember { mutableStateOf<List<UserCompanyResponse>>(emptyList()) }
    var userCompaniesForQR by remember { mutableStateOf<List<com.christelldev.easyreferplus.ui.viewmodel.UserCompany>>(emptyList()) }
    // Estado para empresas públicas (validadas)
    var publicCompanies by remember { mutableStateOf<List<UserCompanyResponse>>(emptyList()) }
    // Empresa seleccionada para mostrar detalles
    var selectedCompany by remember { mutableStateOf<UserCompanyResponse?>(null) }

    // Estado para picker de imagen de producto
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadingProductId by remember { mutableStateOf<Int?>(null) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var imageUploadSuccessMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Limpiar mensaje de éxito después de navegar
    LaunchedEffect(imageUploadSuccessMessage) {
        if (imageUploadSuccessMessage != null) {
            kotlinx.coroutines.delay(1500)
            imageUploadSuccessMessage = null
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedImageUri = selectedUri
            // Automatically upload when image is selected
            uploadingProductId?.let { productId ->
                isUploadingImage = true
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // Copy URI content to a temp file
                        val tempFile = java.io.File.createTempFile("product_img_", ".jpg", context.cacheDir)
                        context.contentResolver.openInputStream(selectedUri)?.use { input ->
                            tempFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }

                        val token = authRepository.getAccessToken() ?: ""
                        val result = productRepository.uploadProductImage(
                            authorization = "Bearer $token",
                            productId = productId,
                            imageFile = tempFile,
                            isPrimary = false
                        )
                        when (result) {
                            is com.christelldev.easyreferplus.data.network.ProductImageResult.Success -> {
                                android.util.Log.d("ProductForm", "Imagen subida exitosamente: ${result.imageUrl}")
                                // Refresh products to show new image
                                productViewModel.loadMyCompanyProducts()
                                // Show success message and navigate back
                                imageUploadSuccessMessage = "Imagen subida exitosamente"
                            }
                            is com.christelldev.easyreferplus.data.network.ProductImageResult.Error -> {
                                android.util.Log.e("ProductForm", "Error al subir imagen: ${result.message}")
                            }
                        }
                        // Clean up temp file
                        tempFile.delete()
                    } catch (e: Exception) {
                        android.util.Log.e("ProductForm", "Excepcion al subir imagen: ${e.message}")
                    } finally {
                        isUploadingImage = false
                        uploadingProductId = null
                        selectedImageUri = null
                    }
                }
            }
        }
    }

    // Función para cargar empresas públicas
    fun loadPublicCompanies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = companyRepository.searchCompanies("Bearer ${authRepository.getAccessToken()}")
                when (result) {
                    is com.christelldev.easyreferplus.data.network.CompanySearchResult.Success -> {
                        publicCompanies = result.companies
                    }
                    is com.christelldev.easyreferplus.data.network.CompanySearchResult.Error -> {
                        android.util.Log.e("MainActivity", "Error loading public companies: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Exception loading public companies: ${e.message}")
            }
        }
    }

    // Función para cargar empresas del usuario
    fun loadUserCompanies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = companyRepository.getMyCompany("Bearer ${authRepository.getAccessToken()}")
                when (result) {
                    is com.christelldev.easyreferplus.data.network.UserCompaniesResult.Success -> {
                        userCompanies = result.companies
                        userCompaniesForQR = result.companies.map { company ->
                            com.christelldev.easyreferplus.ui.viewmodel.UserCompany(
                                id = company.id,
                                name = company.companyName ?: "",
                                isValidated = company.isValidated
                            )
                        }
                    }
                    is com.christelldev.easyreferplus.data.network.UserCompaniesResult.Error -> {
                        android.util.Log.e("MainActivity", "Error loading user companies: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Exception loading user companies: ${e.message}")
            }
        }
    }

    // Función para recargar empresas
    fun refreshCompanies() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = companyRepository.getMyCompany("Bearer ${authRepository.getAccessToken()}")
                when (result) {
                    is com.christelldev.easyreferplus.data.network.UserCompaniesResult.Success -> {
                        userCompanies = result.companies
                        // Convertir para QR
                        userCompaniesForQR = result.companies.map { company ->
                            com.christelldev.easyreferplus.ui.viewmodel.UserCompany(
                                id = company.id,
                                name = company.companyName,
                                isValidated = company.isValidated
                            )
                        }
                    }
                    is com.christelldev.easyreferplus.data.network.UserCompaniesResult.Error -> {
                        android.util.Log.e("MainActivity", "Error loading company: ${result.message}")
                        // Si hay error de conexión o token inválido, verificar si sigue logueado
                        if (result.message.contains("closed") || result.message.contains("401") ||
                            result.message.contains("conexión") || result.message.contains("Connection")) {
                            // Token inválido, cerrar sesión y redirigir al login
                            CoroutineScope(Dispatchers.Main).launch {
                                authRepository.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Exception loading company: ${e.message}")
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                // No limpiar datos aquí - la limpieza se hace en logout
                // Esto evita llamadas no deseadas al servidor

                LoginScreen(
                    viewModel = loginViewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.PasswordReset.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = registerViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PasswordReset.route) {
                // Limpiar estado al entrar a la pantalla de password reset
                LaunchedEffect(Unit) {
                    passwordResetViewModel.goToPhoneStep()
                }
                PasswordResetScreen(
                    viewModel = passwordResetViewModel,
                    onBackToLogin = {
                        passwordResetViewModel.goToPhoneStep()
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Home.route) {
                // Cargar carrito cuando se muestra el Home
                LaunchedEffect(Unit) {
                    productViewModel.loadCart()
                }

                val cartCount by productViewModel.cartCount.collectAsState()

                HomeScreen(
                    viewModel = homeViewModel,
                    cartCount = cartCount,
                    onLogout = {
                        // Desconectar WebSocket
                        disconnectWebSocket()
                        // Limpiar TODOS los datos del usuario (tokens, caché, etc.)
                        authRepository.clearAllData()
                        // Llamar callback para reiniciar actividad
                        onLogoutComplete()
                    },
                    onNavigateToReferrals = {
                        navController.navigate(Screen.Referral.route)
                    },
                    onNavigateToCompany = {
                        navController.navigate(Screen.Company.route)
                    },
                    onNavigateToCompaniesList = {
                        // Navegar a mis empresas (propias del usuario)
                        loadUserCompanies()
                        navController.navigate(Screen.CompaniesList.route)
                    },
                    onNavigateToPublicCompanies = {
                        // Navegar a empresas públicas
                        loadPublicCompanies()
                        navController.navigate(Screen.PublicCompanies.route)
                    },
                    onNavigateToQR = {
                        refreshCompanies()
                        navController.navigate(Screen.QR.route)
                    },
                    onNavigateToAdminEarnings = {
                        navController.navigate(Screen.AdminEarnings.route)
                    },
                    onNavigateToAdminWithdrawals = {
                        navController.navigate(Screen.AdminWithdrawals.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToPayments = {
                        // Obtener datos de pagos del ViewModel
                        val paymentCompanyId = homeViewModel.uiState.value.paymentCompanyId ?: 0
                        val paymentCompanyName = homeViewModel.uiState.value.paymentCompanyName ?: "Mi Empresa"
                        val pendingAmount = homeViewModel.uiState.value.pendingPaymentAmount

                        // Navegar con los parámetros
                        navController.navigate("company_payments/$paymentCompanyId?companyName=$paymentCompanyName&pendingAmount=$pendingAmount")
                    },
                    onNavigateToEarnings = {
                        navController.navigate(Screen.Earnings.route)
                    },
                    onNavigateToWithdrawal = {
                        navController.navigate(Screen.Withdrawal.route)
                    },
                    onNavigateToCart = {
                        productViewModel.loadCart()
                        navController.navigate(Screen.Cart.route)
                    },
                    onNavigateToMyProducts = {
                        productViewModel.loadMyCompanyProducts()
                        productViewModel.loadProductCategories()
                        navController.navigate(Screen.MyProducts.route)
                    }
                )
            }

            composable(Screen.Referral.route) {
                ReferralScreen(
                    viewModel = referralViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Company.route) {
                CompanyScreen(
                    viewModel = companyViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.CompaniesList.route) {
                CompaniesListScreen(
                    userCompanies = userCompanies,
                    onBack = {
                        navController.popBackStack()
                    },
                    // onRegisterCompany = null - Ya no se permite registrar nuevas empresas
                    // since only one company per RUC is allowed
                    onRegisterCompany = null,
                    onEditCompany = { companyId ->
                        navController.navigate(Screen.EditCompany.createRoute(companyId))
                    }
                )
            }

            // Pantalla de empresas públicas (validadas)
            composable(Screen.PublicCompanies.route) {
                val companyRepository = remember { CompanyRepository.Factory().create() }
                val publicCompaniesViewModel: PublicCompaniesViewModel = viewModel(
                    factory = PublicCompaniesViewModel.Factory(companyRepository) { authRepository.getAccessToken() ?: "" }
                )

                PublicCompaniesScreen(
                    viewModel = publicCompaniesViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onCompanyClick = { companyId ->
                        // Navegar a detalle de empresa
                        navController.navigate(Screen.CompanyDetail.createRoute(companyId))
                    }
                )
            }

            // Pantalla de detalle de empresa pública
            composable(
                route = Screen.CompanyDetail.route,
                arguments = listOf(
                    navArgument("companyId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getInt("companyId") ?: return@composable
                val companyRepository = remember { CompanyRepository.Factory().create() }
                val companyDetailViewModel: CompanyDetailViewModel = viewModel(
                    factory = CompanyDetailViewModel.Factory(companyRepository) { authRepository.getAccessToken() ?: "" }
                )
                val detailUiState by companyDetailViewModel.uiState.collectAsState()

                CompanyDetailScreen(
                    companyId = companyId,
                    companyName = detailUiState.company?.companyName ?: "",
                    viewModel = companyDetailViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProducts = {
                        navController.navigate(Screen.CompanyProducts.createRoute(companyId, detailUiState.company?.companyName ?: ""))
                    },
                    onProductClick = { product ->
                        navController.navigate(Screen.ProductDetail.createRoute(product.id ?: 0))
                    }
                )
            }

            // Company Products Screen
            composable(
                route = Screen.CompanyProducts.route,
                arguments = listOf(
                    navArgument("companyId") { type = NavType.IntType },
                    navArgument("companyName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getInt("companyId") ?: return@composable
                val companyName = backStackEntry.arguments?.getString("companyName")?.replace("_", " ") ?: ""
                val cartCount by productViewModel.cartCount.collectAsState()

                // Obtener el companyId del usuario actual (si tiene empresa)
                val userCompanyId = homeViewModel.uiState.value.paymentCompanyId
                val isCompanyOwner = userCompanyId != null && userCompanyId == companyId

                CompanyProductsScreen(
                    companyId = companyId,
                    companyName = companyName,
                    products = publicCompanies.find { it.id == companyId }?.products ?: emptyList(),
                    isLoading = false,
                    cartCount = cartCount,
                    isCompanyOwner = isCompanyOwner,
                    onProductClick = { product ->
                        navController.navigate(Screen.ProductDetail.createRoute(product.id ?: 0))
                    },
                    onAddToCart = { product ->
                        // Agregar al carrito usando el ViewModel
                        productViewModel.addToCart(product.id ?: 0, 1)
                    },
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Product Detail Screen
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
                val product = publicCompanies.flatMap { it.products ?: emptyList() }.find { it.id == productId }
                val cartCount by productViewModel.cartCount.collectAsState()

                if (product != null) {
                    // Obtener el companyId del usuario actual
                    val userCompanyId = homeViewModel.uiState.value.paymentCompanyId
                    // Verificar si el producto pertenece a la empresa del usuario
                    val isProductOwner = userCompanyId != null && product.companyId == userCompanyId

                    ProductDetailScreen(
                        product = product,
                        cartCount = cartCount,
                        isProductOwner = isProductOwner,
                        onAddToCart = { quantity ->
                            // Agregar al carrito usando el ViewModel
                            productViewModel.addToCart(product.id ?: 0, quantity)
                            navController.popBackStack()
                        },
                        onNavigateToCart = {
                            navController.navigate(Screen.Cart.route)
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(
                route = Screen.EditCompany.route,
                arguments = listOf(
                    navArgument("companyId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getInt("companyId") ?: return@composable
                EditCompanyScreen(
                    viewModel = companyViewModel,
                    companyId = companyId,
                    onBack = {
                        refreshCompanies()
                        navController.popBackStack()
                    },
                    onSuccess = {
                        refreshCompanies()
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla del Carrito
            composable(Screen.Cart.route) {
                LaunchedEffect(Unit) {
                    productViewModel.loadCart()
                }

                val cartItems by productViewModel.cartItems.collectAsState()
                val uiState by productViewModel.uiState.collectAsState()
                val checkoutState by productViewModel.checkoutState.collectAsState()

                // Convertir el estado del ViewModel al estado del CartScreen
                val cartCheckoutState = when (val state = checkoutState) {
                    is com.christelldev.easyreferplus.ui.viewmodel.ProductViewModel.CheckoutState.Idle ->
                        com.christelldev.easyreferplus.ui.screens.cart.CheckoutState.Idle
                    is com.christelldev.easyreferplus.ui.viewmodel.ProductViewModel.CheckoutState.Processing ->
                        com.christelldev.easyreferplus.ui.screens.cart.CheckoutState.Processing
                    is com.christelldev.easyreferplus.ui.viewmodel.ProductViewModel.CheckoutState.Success ->
                        com.christelldev.easyreferplus.ui.screens.cart.CheckoutState.Success(
                            message = state.message,
                            orderId = state.orderId,
                            qrCodes = state.qrCodes,
                            totalItems = state.totalItems,
                            totalAmount = state.totalAmount,
                            companyCount = state.companyCount
                        )
                    is com.christelldev.easyreferplus.ui.viewmodel.ProductViewModel.CheckoutState.Error ->
                        com.christelldev.easyreferplus.ui.screens.cart.CheckoutState.Error(state.message)
                }

                CartScreen(
                    cartItems = cartItems,
                    isLoading = uiState is com.christelldev.easyreferplus.ui.viewmodel.ProductUiState.Loading,
                    checkoutState = cartCheckoutState,
                    onAddToCart = { productId, quantity ->
                        productViewModel.addToCart(productId, quantity)
                    },
                    onRemoveFromCart = { productId ->
                        productViewModel.removeFromCart(productId)
                    },
                    onUpdateQuantity = { productId, quantity ->
                        productViewModel.updateCartItem(productId, quantity)
                    },
                    onCheckout = {
                        productViewModel.checkout()
                    },
                    onClearCart = {
                        productViewModel.clearCart()
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onCheckoutDismiss = {
                        productViewModel.resetCheckoutState()
                    },
                    onRefreshCart = {
                        productViewModel.loadCart()
                    }
                )
            }

            // Pantalla Mis Productos (para empresa)
            composable(Screen.MyProducts.route) {
                LaunchedEffect(Unit) {
                    // Cargar empresas del usuario si no están cargadas
                    if (userCompanies.isEmpty()) {
                        refreshCompanies()
                    }
                    productViewModel.loadMyCompanyProducts()
                    productViewModel.loadProductCategories()
                }

                val products by productViewModel.products.collectAsState()
                val categories by productViewModel.categories.collectAsState()
                val uiState by productViewModel.uiState.collectAsState()

                // Determinar si el usuario es propietario de empresa
                // Si userCompanies está vacío, intentamos cargar - el botón se mostrará si hay empresa
                val isCompanyOwner = userCompanies.isNotEmpty()

                MyProductsScreen(
                    products = products,
                    categories = categories,
                    isLoading = uiState is com.christelldev.easyreferplus.ui.viewmodel.ProductUiState.Loading,
                    isCompanyOwner = true, // Siempre mostrar el botón, el backend maneja el permiso
                    onAddProduct = {
                        navController.navigate(Screen.ProductForm.createRoute(null))
                    },
                    onEditProduct = { product ->
                        navController.navigate(Screen.ProductForm.createRoute(product.id))
                    },
                    onDeleteProduct = { productId ->
                        productViewModel.deleteProduct(productId)
                    },
                    onViewProduct = { product ->
                        // Could navigate to product detail
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Pantalla Formulario de Producto
            composable(
                route = Screen.ProductForm.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productIdStr = backStackEntry.arguments?.getString("productId")
                val productId = if (productIdStr == "new") null else productIdStr?.toIntOrNull()

                LaunchedEffect(productId) {
                    if (productId != null) {
                        productViewModel.loadMyCompanyProducts()
                    }
                    productViewModel.loadProductCategories()
                }

                val products by productViewModel.products.collectAsState()
                val categories by productViewModel.categories.collectAsState()
                val uiState by productViewModel.uiState.collectAsState()
                val currentProduct = products.find { it.id == productId }

                ProductFormScreen(
                    product = currentProduct,
                    categories = categories,
                    isLoading = uiState is com.christelldev.easyreferplus.ui.viewmodel.ProductUiState.Loading,
                    successMessage = imageUploadSuccessMessage,
                    onSave = { name, description, categoryId, size, weight, dimensions, quantity, price, offerPrice, commission, useCompanyDefault, status ->
                        if (productId != null) {
                            productViewModel.updateProduct(
                                productId = productId,
                                productName = name,
                                productDescription = description,
                                productCategoryId = categoryId,
                                size = size,
                                weight = weight,
                                dimensions = dimensions,
                                quantity = quantity,
                                price = price,
                                offerPrice = offerPrice,
                                specificCommissionPercentage = commission,
                                useCompanyDefault = useCompanyDefault,
                                status = status
                            )
                        } else {
                            productViewModel.createProduct(
                                productName = name,
                                productDescription = description,
                                productCategoryId = categoryId,
                                size = size,
                                weight = weight,
                                dimensions = dimensions,
                                quantity = quantity,
                                price = price,
                                offerPrice = offerPrice,
                                specificCommissionPercentage = commission,
                                useCompanyDefault = useCompanyDefault,
                                status = status
                            )
                            // Navegar de vuelta a la lista de productos
                            navController.popBackStack()
                        }
                    },
                    onUploadImage = { productIdParam ->
                        // Guardar el ID del producto y lanzar el picker
                        uploadingProductId = productIdParam
                        android.util.Log.d("ProductForm", "Subir imagen para producto: $productIdParam")
                        imagePickerLauncher.launch("image/*")
                    },
                    onDeleteImage = { imageIdParam ->
                        // Eliminar imagen
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val token = authRepository.getAccessToken() ?: ""
                                val result = productRepository.deleteProductImage(
                                    authorization = "Bearer $token",
                                    imageId = imageIdParam
                                )
                                when (result) {
                                    is com.christelldev.easyreferplus.data.network.ProductImageResult.Success -> {
                                        android.util.Log.d("ProductForm", "Imagen eliminada exitosamente")
                                        // Refresh products to update list
                                        productViewModel.loadMyCompanyProducts()
                                    }
                                    is com.christelldev.easyreferplus.data.network.ProductImageResult.Error -> {
                                        android.util.Log.e("ProductForm", "Error al eliminar imagen: ${result.message}")
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("ProductForm", "Excepcion al eliminar imagen: ${e.message}")
                            }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.QR.route) {
                // Función para activar WebSocket (cuando se genera QR)
                fun activateWebSocket() {
                    // Solo conectar si no está ya conectado
                    if (webSocketManager == null && authRepository.isLoggedIn()) {
                        // Obtener URL dinámica de AppConfig
                        val wsUrl = com.christelldev.easyreferplus.data.network.AppConfig.WS_URL
                        webSocketManager = WebSocketManager(wsUrl) { authRepository.getAccessToken() ?: "" }
                        webSocketManager?.connect()
                    } else if (webSocketManager != null && !webSocketManager!!.isConnected()) {
                        webSocketManager?.connect()
                    }
                    // Incrementar para forzar recomposición
                    webSocketConnectionKey++
                }

                // Escuchar notificaciones de venta - usar callback directo
                val mainScope = remember { CoroutineScope(Dispatchers.Main) }
                LaunchedEffect(webSocketConnectionKey, webSocketManager) {
                    val manager = webSocketManager
                    if (manager != null) {
                        // Configurar callback para notificaciones (se ejecuta en hilo del WebSocket)
                        manager.onNotificationReceived = { notification ->
                            mainScope.launch(Dispatchers.Main) {
                                // Notificar al QR screen para mostrar el recibo
                                qrViewModel.showSaleReceiptFromNotification(notification)
                                // Limpiar el QR generado cuando llega una venta
                                qrViewModel.clearGeneratedQR()
                                // Desconectar después de recibir la notificación
                                manager.disconnect()
                                webSocketManager = null
                                webSocketConnectionKey++
                            }
                        }
                    }
                }

                // Desconectar WebSocket al salir de la pantalla QR
                DisposableEffect(Unit) {
                    onDispose {
                        // Desconectar al salir de la pantalla
                        webSocketManager?.disconnect()
                        webSocketManager = null
                        webSocketConnectionKey++
                    }
                }

                QRScreen(
                    viewModel = qrViewModel,
                    hasCompany = userCompanies.isNotEmpty(),
                    hasActiveCompany = userCompanies.any { it.status == "validated" && it.isActive },
                    companies = userCompaniesForQR,
                    onGenerateQRSuccess = {
                        // Conectar WebSocket cuando se genera QR exitosamente
                        activateWebSocket()
                    },
                    onQRScannedByBuyer = {
                        // Limpiar el QR cuando un cliente lo escanea
                        qrViewModel.clearGeneratedQR()
                    },
                    onBack = {
                        // Desconectar al salir
                        webSocketManager?.disconnect()
                        webSocketManager = null
                        webSocketConnectionKey++
                        navController.popBackStack()
                    },
                    onNavigateToCompany = {
                        navController.navigate(Screen.Company.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSessions = {
                        navController.navigate(Screen.Sessions.route)
                    },
                    onLogout = {
                        // Desconectar WebSocket
                        disconnectWebSocket()
                        // Limpiar TODOS los datos del usuario (tokens, caché, etc.)
                        authRepository.clearAllData()
                        onLogoutComplete()
                    }
                )
            }

            composable(Screen.Sessions.route) {
                SessionManagementScreen(
                    viewModel = sessionViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AdminEarnings.route) {
                AdminEarningsScreen(
                    viewModel = adminEarningsViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.AdminWithdrawals.route) {
                val apiService = remember { RetrofitClient.getInstance().create(ApiService::class.java) }
                val adminWithdrawalsViewModel: AdminWithdrawalsViewModel = viewModel(
                    factory = AdminWithdrawalsViewModel.Factory(apiService) { authRepository.getAccessToken() ?: "" }
                )
                AdminWithdrawalsScreen(
                    viewModel = adminWithdrawalsViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onTransactionClick = { transactionId ->
                        navController.navigate(Screen.TransactionDetail.createRoute(transactionId))
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.TransactionDetail.route) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
                val qrRepository = remember { com.christelldev.easyreferplus.data.network.QRRepository.Factory().create() }
                val accessToken: String = remember { authRepository.getAccessToken() ?: "" }

                val detailViewModel: com.christelldev.easyreferplus.ui.viewmodel.TransactionDetailViewModel = remember {
                    com.christelldev.easyreferplus.ui.viewmodel.TransactionDetailViewModel.Factory(
                        qrRepository
                    ) { accessToken }.create(com.christelldev.easyreferplus.ui.viewmodel.TransactionDetailViewModel::class.java)
                }

                TransactionDetailScreen(
                    transactionId = transactionId,
                    viewModel = detailViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = "company_payments/{companyId}?companyName={companyName}&pendingAmount={pendingAmount}"
            ) { backStackEntry ->
                val companyId = backStackEntry.arguments?.getString("companyId")?.toIntOrNull() ?: 0
                val companyName = backStackEntry.arguments?.getString("companyName") ?: "Mi Empresa"
                val pendingAmount = backStackEntry.arguments?.getString("pendingAmount")?.toDoubleOrNull() ?: 0.0

                // Get shared dependencies
                val companyRepository = remember { com.christelldev.easyreferplus.data.network.CompanyRepository.Factory().create() }
                val accessToken: String = remember { authRepository.getAccessToken() ?: "" }

                val paymentsViewModel: com.christelldev.easyreferplus.ui.viewmodel.PaymentsViewModel = remember {
                    com.christelldev.easyreferplus.ui.viewmodel.PaymentsViewModel.Factory(
                        companyRepository
                    ) { accessToken }.create(com.christelldev.easyreferplus.ui.viewmodel.PaymentsViewModel::class.java)
                }

                val uiState by paymentsViewModel.uiState.collectAsState()

                // Initialize with company info passed from Home
                LaunchedEffect(companyId) {
                    paymentsViewModel.initialize(
                        companyId = companyId,
                        companyName = companyName,
                        pendingAmount = pendingAmount
                    )
                }

                CompanyPaymentsScreen(
                    companyName = uiState.companyName.ifEmpty { companyName },
                    pendingAmount = if (uiState.pendingAmount > 0) uiState.pendingAmount else pendingAmount,
                    payments = uiState.payments,
                    isLoading = uiState.isLoading,
                    isRegistering = uiState.isRegistering,
                    errorMessage = uiState.errorMessage,
                    successMessage = uiState.successMessage,
                    onRegisterPayment = { docNum, bank, amount, notes ->
                        paymentsViewModel.registerPayment(docNum, bank, amount, notes)
                    },
                    onRefresh = { paymentsViewModel.loadPayments() },
                    onBack = { navController.popBackStack() },
                    onClearMessages = { paymentsViewModel.clearMessages() }
                )
            }

            // Pantalla de Ganancias del usuario
            composable(Screen.Earnings.route) {
                val uiState by earningsViewModel.uiState.collectAsState()

                // Cargar ganancias al entrar
                LaunchedEffect(Unit) {
                    earningsViewModel.loadEarnings()
                    earningsViewModel.loadCommissions()
                }

                EarningsScreen(
                    totalEarned = uiState.totalEarned,
                    totalPaid = uiState.totalPaid,
                    totalPending = uiState.totalPending,
                    totalCommissions = uiState.totalCommissions,
                    pendingCount = uiState.pendingCount,
                    paidCount = uiState.paidCount,
                    scheduledCount = uiState.scheduledCount,
                    level1Earnings = uiState.earningsByLevel?.level1 ?: 0.0,
                    level2Earnings = uiState.earningsByLevel?.level2 ?: 0.0,
                    level3Earnings = uiState.earningsByLevel?.level3 ?: 0.0,
                    topCompanies = uiState.topCompanies.map { it.companyName to it.totalEarned },
                    commissions = uiState.commissions,
                    isLoadingCommissions = uiState.isLoadingCommissions,
                    hasMoreCommissions = uiState.hasMoreCommissions,
                    selectedFilter = uiState.selectedFilter,
                    isLoading = uiState.isLoading,
                    isEmpty = uiState.isEmpty,
                    errorMessage = uiState.errorMessage,
                    onRefresh = {
                        earningsViewModel.refresh()
                        earningsViewModel.loadCommissions()
                    },
                    onBack = { navController.popBackStack() },
                    onClearError = { earningsViewModel.clearError() },
                    onLoadMoreCommissions = { earningsViewModel.loadMoreCommissions() },
                    onFilterChange = { filter -> earningsViewModel.setFilter(filter) }
                )
            }

            // Pantalla de Retiros
            composable(Screen.Withdrawal.route) {
                val uiState by withdrawalViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    withdrawalViewModel.loadData()
                }

                WithdrawalScreen(
                    availableBalance = uiState.availableBalance,
                    totalEarned = uiState.totalEarned,
                    totalWithdrawn = uiState.totalWithdrawn,
                    pendingWithdrawal = uiState.pendingWithdrawal,
                    minimumWithdrawalAmount = uiState.minimumWithdrawalAmount,
                    canRequestWithdrawal = uiState.canRequestWithdrawal,
                    bankAccounts = uiState.bankAccounts,
                    selectedAccountId = uiState.selectedAccountId,
                    withdrawalRequests = uiState.withdrawalRequests,
                    isLoading = uiState.isLoading,
                    isCreatingAccount = uiState.isCreatingAccount,
                    isRequestingWithdrawal = uiState.isRequestingWithdrawal,
                    successMessage = uiState.successMessage,
                    errorMessage = uiState.errorMessage,
                    onSelectAccount = { withdrawalViewModel.selectAccount(it) },
                    onCreateAccount = { bankName, accountType, accountNumber, holderName, document ->
                        withdrawalViewModel.createBankAccount(bankName, accountType, accountNumber, holderName, document)
                    },
                    onRequestWithdrawal = { amount -> withdrawalViewModel.requestWithdrawal(amount) },
                    onRefresh = { withdrawalViewModel.loadData() },
                    onBack = { navController.popBackStack() },
                    onClearMessages = { withdrawalViewModel.clearMessages() }
                )
            }
        }
    }

    // Dialog para mostrar detalles de empresa pública
    if (selectedCompany != null) {
        AlertDialog(
            onDismissRequest = { selectedCompany = null },
            title = {
                Text(text = selectedCompany!!.companyName)
            },
            text = {
                Column {
                    if (selectedCompany!!.logoUrl != null && selectedCompany!!.hasLogo) {
                        AsyncImage(
                            model = selectedCompany!!.logoUrl,
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    selectedCompany!!.companyDescription?.let {
                        Text(text = it, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    selectedCompany!!.address?.let {
                        Text(text = "Dirección: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    selectedCompany!!.city?.let {
                        Text(text = "Ciudad: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    selectedCompany!!.province?.let {
                        Text(text = "Provincia: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    selectedCompany!!.website?.let {
                        Text(text = "Web: $it", style = MaterialTheme.typography.bodySmall)
                    }
                    selectedCompany!!.whatsappNumber?.let {
                        Text(text = "WhatsApp: $it", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCompany = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}
