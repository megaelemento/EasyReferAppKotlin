package com.christelldev.easyreferplus.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

// Constants for consistent styling
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp
private val SECTION_SPACING = 20.dp
private val CARD_MIN_HEIGHT = 100.dp

// Modern color palette
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSecondary = listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientWarning = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
private val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
private val GradientPink = listOf(Color(0xFFEC4899), Color(0xFFF472B6))
private val GradientBlue = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientGreen = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientOrange = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
private val GradientTeal = listOf(Color(0xFF14B8A6), Color(0xFF2DD4BF))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartCount: Int = 0,
    onNavigateToReferrals: () -> Unit = {},
    onNavigateToCompany: () -> Unit = {},
    onNavigateToCompaniesList: () -> Unit = {},
    onNavigateToPublicCompanies: () -> Unit = {},
    onNavigateToQR: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAdminEarnings: () -> Unit = {},
    onNavigateToAdminWithdrawals: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToEarnings: () -> Unit = {},
    onNavigateToWithdrawal: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToMyProducts: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToWalletTransfer: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showLogoutDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    LaunchedEffect(Unit) {
        viewModel.loadHomeData()
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                scope.launch { onLogout() }
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModernDrawerContent(
                userName = "${uiState.nombres} ${uiState.apellidos}".takeIf { it.isNotBlank() } ?: "Usuario",
                userEmail = uiState.email,
                isVerified = uiState.isVerified,
                isAdmin = uiState.isAdmin,
                hasCompany = uiState.hasCompany,
                hasEarningsAvailable = uiState.hasEarningsAvailable,
                hasWithdrawalsAvailable = uiState.hasWithdrawalsAvailable,
                selfieUrl = uiState.selfieUrl,
                onNavigateToProfile = {
                    scope.launch { drawerState.close() }
                    onNavigateToProfile()
                },
                onNavigateToReferrals = {
                    scope.launch { drawerState.close() }
                    onNavigateToReferrals()
                },
                onNavigateToCompany = {
                    scope.launch { drawerState.close() }
                    if (uiState.hasCompany) onNavigateToCompaniesList() else onNavigateToCompany()
                },
                onNavigateToQR = {
                    scope.launch { drawerState.close() }
                    onNavigateToQR()
                },
                onNavigateToAdminEarnings = {
                    scope.launch { drawerState.close() }
                    onNavigateToAdminEarnings()
                },
                onNavigateToAdminWithdrawals = {
                    scope.launch { drawerState.close() }
                    onNavigateToAdminWithdrawals()
                },
                onNavigateToMyProducts = {
                    scope.launch { drawerState.close() }
                    onNavigateToMyProducts()
                },
                onNavigateToCart = {
                    scope.launch { drawerState.close() }
                    onNavigateToCart()
                },
                onNavigateToHistory = {
                    scope.launch { drawerState.close() }
                    onNavigateToHistory()
                },
                onNavigateToPayments = {
                    scope.launch { drawerState.close() }
                    onNavigateToPayments()
                },
                onNavigateToEarnings = {
                    scope.launch { drawerState.close() }
                    onNavigateToEarnings()
                },
                onNavigateToWithdrawal = {
                    scope.launch { drawerState.close() }
                    onNavigateToWithdrawal()
                },
                onNavigateToWallet = {
                    scope.launch { drawerState.close() }
                    onNavigateToWallet()
                },
                onNavigateToWalletTransfer = {
                    scope.launch { drawerState.close() }
                    onNavigateToWalletTransfer()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    showLogoutDialog = true
                }
            )
        }
    ) {
        Scaffold { paddingValues ->
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).background(backgroundColor),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Header
                    item {
                        ModernHeader(
                            userName = "${uiState.nombres} ${uiState.apellidos}".takeIf { it.isNotBlank() } ?: "Usuario",
                            isVerified = uiState.isVerified,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }

                    // Cart notification banner
                    if (cartCount > 0) {
                        item {
                            CartNotificationBanner(
                                cartCount = cartCount,
                                onClick = onNavigateToCart
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(SECTION_SPACING)) }

                    // Generate QR Button (only if company is validated and active)
                    if (uiState.canGenerateQR) {
                        item {
                            GenerateQRCard(
                                companyName = uiState.empresaNombre ?: "",
                                onClick = onNavigateToQR
                            )
                        }
                        item { Spacer(modifier = Modifier.height(SECTION_SPACING)) }
                    }

                    // 1. Quick Actions Section
                    item {
                        QuickActionsSection(
                            hasCompany = uiState.hasCompany,
                            hasValidatedCompany = uiState.hasValidatedCompany,
                            canAccessPayments = uiState.canAccessPayments,
                            onQRClick = onNavigateToQR,
                            onCompanyClick = { onNavigateToCompaniesList() },
                            onReferralsClick = onNavigateToReferrals,
                            onProfileClick = onNavigateToProfile,
                            onHistoryClick = onNavigateToHistory,
                            onPaymentsClick = onNavigateToPayments,
                            onEarningsClick = onNavigateToEarnings,
                            onWithdrawClick = onNavigateToWithdrawal,
                            onCartClick = onNavigateToCart,
                            onMyProductsClick = onNavigateToMyProducts,
                            onWalletClick = onNavigateToWallet,
                            onWalletTransferClick = onNavigateToWalletTransfer
                        )
                    }

                    item { Spacer(modifier = Modifier.height(SECTION_SPACING)) }

                    // 2. Referral Code Section
                    item {
                        ReferralCodeCard(
                            referralCode = uiState.referralCode,
                            onCopy = { copyToClipboard(context, it) },
                            onShare = { shareReferralCode(context, it) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(SECTION_SPACING)) }

                    // 3. Statistics Section
                    item {
                        StatsSection(
                            totalReferrals = uiState.totalReferrals,
                            level1Referrals = uiState.level1Referrals,
                            level2Referrals = uiState.level2Referrals,
                            level3Referrals = uiState.level3Referrals
                        )
                    }

                    item { Spacer(modifier = Modifier.height(SECTION_SPACING)) }

                    // 4. Public Companies Section
                    item {
                        PublicCompaniesCard(onClick = onNavigateToPublicCompanies)
                    }

                    // Company Info (if has company but not active for QR)
                    if (uiState.hasCompany && !uiState.canGenerateQR) {
                        item { Spacer(modifier = Modifier.height(SECTION_SPACING)) }
                        item {
                            CompanyInfoCard(
                                companyName = uiState.empresaNombre ?: "",
                                isActive = uiState.empresaActiva ?: false,
                                status = uiState.empresaStatus ?: "",
                                onClick = onNavigateToCompaniesList
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernHeader(userName: String, isVerified: Boolean, onMenuClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03A9F4),
                        Color(0xFF2196F3),
                        Color(0xFF1976D2)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(Color(0xFF10B981), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Profile avatar placeholder
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun CartNotificationBanner(
    cartCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Productos en carrito",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "$cartCount producto(s) pendiente(s) de pagar",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE65100).copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver carrito",
                tint = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun GenerateQRCard(companyName: String, onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .height(130.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(CARD_CORNER_RADIUS))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.generate_qr),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = companyName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    hasCompany: Boolean,
    hasValidatedCompany: Boolean,
    canAccessPayments: Boolean,
    onQRClick: () -> Unit,
    onCompanyClick: () -> Unit,
    onReferralsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onPaymentsClick: () -> Unit,
    onEarningsClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onCartClick: () -> Unit = {},
    onMyProductsClick: () -> Unit = {},
    onWalletClick: () -> Unit = {},
    onWalletTransferClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = CARD_MARGIN_HORIZONTAL)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.quick_actions),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ver todo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid-like layout for quick actions (2 rows)
        val actionItems = buildList {
            add(ActionItem(Icons.Default.QrCode2, stringResource(R.string.qr_payments), GradientBlue, onQRClick))
            add(ActionItem(if (hasCompany) Icons.Default.QrCode else Icons.Default.Add, if (hasCompany) stringResource(R.string.my_companies) else stringResource(R.string.register_company), GradientGreen, onCompanyClick))
            add(ActionItem(Icons.Default.Group, stringResource(R.string.my_referrals), GradientOrange, onReferralsClick))
            add(ActionItem(Icons.Default.Person, stringResource(R.string.profile), GradientPurple, onProfileClick))
            add(ActionItem(Icons.Default.AccountBalanceWallet, "Retiros", GradientWarning, onWithdrawClick))
            add(ActionItem(Icons.Default.TrendingUp, "Ganancias", GradientSuccess, onEarningsClick))
            add(ActionItem(Icons.Default.AccountBalanceWallet, "Billetera", listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)), onWalletClick))
            add(ActionItem(Icons.AutoMirrored.Filled.Send, "Transferir", listOf(Color(0xFF0891B2), Color(0xFF22D3EE)), onWalletTransferClick))

            // Solo mostrar si tiene empresa
            if (hasCompany) {
                add(ActionItem(Icons.Default.History, "Historial", GradientTeal, onHistoryClick))
                add(ActionItem(Icons.Default.AttachMoney, "Pagos", GradientBlue, onPaymentsClick))
                add(ActionItem(Icons.Default.ShoppingBag, "Carrito", listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)), onCartClick))
                add(ActionItem(Icons.Default.Storefront, "Productos", listOf(Color(0xFF795548), Color(0xFFA1887F)), onMyProductsClick))
            }
        }

        // Grid de 4 columnas, filas dinámicas
        val rows = actionItems.chunked(4)
        rows.forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    ModernQuickActionButton(
                        icon = item.icon,
                        label = item.label,
                        gradient = item.gradient,
                        onClick = item.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Rellena slots vacíos en la última fila
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class ActionItem(
    val icon: ImageVector,
    val label: String,
    val gradient: List<Color>,
    val onClick: () -> Unit
)

@Composable
private fun ModernQuickActionButton(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(132.dp)
            .shadow(5.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = gradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(9.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(CARD_CORNER_RADIUS), spotColor = color.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReferralCodeCard(
    referralCode: String,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF03A9F4), Color(0xFF2196F3), Color(0xFF1976D2))
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.referral_code),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Code container - more prominent with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = referralCode.ifBlank { stringResource(R.string.no_code) },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface,
                            textAlign = TextAlign.Center,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (referralCode.isNotBlank()) {
                        // Copy button
                        Button(
                            onClick = { onCopy(referralCode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.copy_code),
                                color = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Share button
                        Button(
                            onClick = { onShare(referralCode) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF03A9F4),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.share_code),
                                color = Color(0xFF03A9F4),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Comparte tu código y gana comisiones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun StatsSection(
    totalReferrals: Int,
    level1Referrals: Int,
    level2Referrals: Int,
    level3Referrals: Int
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF03A9F4).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF03A9F4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.your_statistics),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Total badge
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(colors = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = totalReferrals.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Modern stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatCard(
                    level = "L1",
                    value = level1Referrals,
                    label = stringResource(R.string.level_1_short),
                    gradient = listOf(Color(0xFF10B981), Color(0xFF34D399)),
                    modifier = Modifier.weight(1f)
                )
                ModernStatCard(
                    level = "L2",
                    value = level2Referrals,
                    label = stringResource(R.string.level_2_short),
                    gradient = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24)),
                    modifier = Modifier.weight(1f)
                )
                ModernStatCard(
                    level = "L3",
                    value = level3Referrals,
                    label = stringResource(R.string.level_3_short),
                    gradient = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ModernStatCard(
    level: String,
    value: Int,
    label: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.linearGradient(colors = gradient)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompanyInfoCard(companyName: String, isActive: Boolean, status: String, onClick: () -> Unit) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Make comparison case-insensitive and handle different formats
    val statusLower = status?.lowercase() ?: ""

    // Prioritize status field over isActive boolean to show correct status
    // The isActive boolean may be true even when status is "pending"
    val (statusColor, statusText) = when {
        statusLower == "validated" || statusLower == "approved" || statusLower == "active" -> Pair(Color(0xFF4CAF50), stringResource(R.string.company_active))
        statusLower == "pending" || statusLower == "inactive" -> Pair(Color(0xFFFF9800), stringResource(R.string.company_pending))
        statusLower == "suspended" || statusLower == "rejected" -> Pair(Color(0xFFF44336), stringResource(R.string.company_inactive))
        // Fallback to isActive only if status is empty or unknown
        isActive -> Pair(Color(0xFF4CAF50), stringResource(R.string.company_active))
        else -> Pair(Color(0xFFFF9800), stringResource(R.string.company_pending))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .height(120.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(CARD_CORNER_RADIUS), spotColor = statusColor.copy(alpha = 0.15f))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.your_company),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = companyName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDrawerContent(
    userName: String,
    userEmail: String,
    isVerified: Boolean,
    isAdmin: Boolean,
    hasCompany: Boolean,
    hasEarningsAvailable: Boolean = false,
    hasWithdrawalsAvailable: Boolean = false,
    selfieUrl: String?,
    onNavigateToProfile: () -> Unit,
    onNavigateToReferrals: () -> Unit,
    onNavigateToCompany: () -> Unit,
    onNavigateToQR: () -> Unit,
    onNavigateToAdminEarnings: () -> Unit,
    onNavigateToAdminWithdrawals: () -> Unit,
    onNavigateToMyProducts: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
    onNavigateToEarnings: () -> Unit = {},
    onNavigateToWithdrawal: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onLogout: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val errorColor = MaterialTheme.colorScheme.error

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = surfaceColor
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

            // ── HEADER ──────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF003D5C), primary)
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    // Avatar con borde blanco translúcido
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(surfaceColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selfieUrl != null) {
                                val selfieUrlWithTimestamp = if (selfieUrl.contains("?")) {
                                    "$selfieUrl&t=${System.currentTimeMillis()}"
                                } else {
                                    "$selfieUrl?t=${System.currentTimeMillis()}"
                                }
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(selfieUrlWithTimestamp)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = userName.take(2).uppercase(),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = Color(0xFF69F0AE),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // ── SECCIÓN: GENERAL ────────────────────────────────────────────
            Spacer(modifier = Modifier.height(8.dp))
            DrawerSectionLabel("General", primary)
            DrawerItem(Icons.Default.Person, "Perfil", primary, onNavigateToProfile)
            DrawerItem(Icons.Default.Group, "Mis Referidos", primary, onNavigateToReferrals)
            DrawerItem(Icons.Default.Business, "Mi Empresa", primary, onNavigateToCompany)

            // ── SECCIÓN: FINANZAS ────────────────────────────────────────────
            if (hasEarningsAvailable || hasWithdrawalsAvailable) {
                Spacer(modifier = Modifier.height(4.dp))
                DrawerSectionLabel("Finanzas", primary)
                if (hasEarningsAvailable)
                    DrawerItem(Icons.Default.TrendingUp, "Ganancias", primary, onNavigateToEarnings)
                if (hasWithdrawalsAvailable)
                    DrawerItem(Icons.Default.Money, "Retiros", primary, onNavigateToWithdrawal)
                    DrawerItem(Icons.Default.AccountBalanceWallet, "Billetera", primary, onNavigateToWallet)
            }

            // ── SECCIÓN: MI EMPRESA ──────────────────────────────────────────
            if (hasCompany) {
                Spacer(modifier = Modifier.height(4.dp))
                DrawerSectionLabel("Mi Empresa", primary)
                DrawerItem(Icons.Default.QrCode2, "Pagos QR", primary, onNavigateToQR)
                DrawerItem(Icons.Default.Store, "Mis Productos", primary, onNavigateToMyProducts)
                DrawerItem(Icons.Default.ShoppingCart, "Carrito", primary, onNavigateToCart)
                DrawerItem(Icons.Default.History, "Historial", primary, onNavigateToHistory)
                DrawerItem(Icons.Default.Payments, "Pagos", primary, onNavigateToPayments)
            }

            // ── SECCIÓN: ADMINISTRACIÓN ──────────────────────────────────────
            if (isAdmin) {
                Spacer(modifier = Modifier.height(4.dp))
                DrawerSectionLabel("Administración", primary)
                DrawerItem(Icons.Default.AttachMoney, "Ganancias Admin", primary, onNavigateToAdminEarnings)
                DrawerItem(Icons.Default.AccountBalanceWallet, "Retiros Admin", primary, onNavigateToAdminWithdrawals)
            }

            // ── LOGOUT ───────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = errorColor.copy(alpha = 0.15f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(errorColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Cerrar sesión",
                        tint = errorColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = errorColor
                )
            }
            // ── VERSIÓN ──────────────────────────────────────────────────────
            Text(
                text = "Versión ${AppConfig.APP_VERSION}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private data class MenuItem(val icon: ImageVector, val label: String, val onClick: () -> Unit)

@Composable
private fun DrawerSectionLabel(label: String, primary: Color) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = primary.copy(alpha = 0.55f),
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
    )
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.logout),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                stringResource(R.string.logout_confirmation),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.logout))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun PublicCompaniesCard(onClick: () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(CARD_CORNER_RADIUS))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.15f),
                            Color(0xFF06B6D4).copy(alpha = 0.15f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF10B981), Color(0xFF34D399))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.public_companies),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.discover_companies),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Referral Code", text))
    Toast.makeText(context, context.getString(R.string.code_copied), Toast.LENGTH_SHORT).show()
}

private fun shareReferralCode(context: Context, referralCode: String) {
    val shareText = context.getString(R.string.share_referral_message, referralCode)
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_code)))
}
