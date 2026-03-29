package com.christelldev.easyreferplus.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.network.AppConfig
import com.christelldev.easyreferplus.ui.theme.DesignConstants
import com.christelldev.easyreferplus.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartCount: Int = 0,
    isMotorizado: Boolean = false,
    pendingInvitationsCount: Int = 0,
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
    onNavigateToDriverPanel: () -> Unit = {},
    onNavigateToDriverHistory: () -> Unit = {},
    onNavigateToDriverInvitations: () -> Unit = {},
    onNavigateToAdminLiveMap: () -> Unit = {},
    onNavigateToAdminOrders: () -> Unit = {},
    onNavigateToAdminReports: () -> Unit = {},
    onNavigateToMisCompras: () -> Unit = {},
    onNavigateToMisVentas: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                isMotorizado = isMotorizado,
                pendingInvitationsCount = pendingInvitationsCount,
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
                onNavigateToDriverPanel = {
                    scope.launch { drawerState.close() }
                    onNavigateToDriverPanel()
                },
                onNavigateToDriverHistory = {
                    scope.launch { drawerState.close() }
                    onNavigateToDriverHistory()
                },
                onNavigateToDriverInvitations = {
                    scope.launch { drawerState.close() }
                    onNavigateToDriverInvitations()
                },
                onNavigateToAdminLiveMap = {
                    scope.launch { drawerState.close() }
                    onNavigateToAdminLiveMap()
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
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        ModernHeader(
                            userName = "${uiState.nombres} ${uiState.apellidos}".takeIf { it.isNotBlank() } ?: "Usuario",
                            isVerified = uiState.isVerified,
                            onMenuClick = { scope.launch { drawerState.open() } }
                        )
                    }

                    if (cartCount > 0) {
                        item {
                            CartNotificationBanner(
                                cartCount = cartCount,
                                onClick = onNavigateToCart
                            )
                        }
                        item { Spacer(modifier = Modifier.height(12.dp)) }
                    }

                    if (uiState.canGenerateQR) {
                        item {
                            GenerateQRCard(
                                companyName = uiState.empresaNombre ?: "",
                                onClick = onNavigateToQR
                            )
                        }
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }

                    item {
                        QuickActionsSection(
                            hasCompany = uiState.hasCompany,
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
                            onWalletTransferClick = onNavigateToWalletTransfer,
                            onMisComprasClick = onNavigateToMisCompras,
                            onMisVentasClick = onNavigateToMisVentas
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        ReferralCodeCard(
                            referralCode = uiState.referralCode,
                            onCopy = { copyToClipboard(context, it) },
                            onShare = { shareReferralCode(context, it) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        StatsSection(
                            totalReferrals = uiState.totalReferrals,
                            level1Referrals = uiState.level1Referrals,
                            level2Referrals = uiState.level2Referrals,
                            level3Referrals = uiState.level3Referrals
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        PublicCompaniesCard(onClick = onNavigateToPublicCompanies)
                    }

                    if (uiState.hasCompany && !uiState.canGenerateQR) {
                        item { Spacer(modifier = Modifier.height(24.dp)) }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Gradiente superior sutil
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                onClick = onMenuClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else Color.White
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFF69F0AE),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                tonalElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CartNotificationBanner(cartCount: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Productos en carrito",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "$cartCount pendiente(s) de pago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun GenerateQRCard(companyName: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.generate_qr),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = companyName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    hasCompany: Boolean,
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
    onWalletTransferClick: () -> Unit = {},
    onMisComprasClick: () -> Unit = {},
    onMisVentasClick: () -> Unit = {}
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.quick_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        val actionItems = buildList {
            add(ActionItem(Icons.Default.QrCode2, stringResource(R.string.qr_payments), Color(0xFF03A9F4), onQRClick))
            add(ActionItem(if (hasCompany) Icons.Default.QrCode else Icons.Default.Add, if (hasCompany) "Mi Empresa" else "Registrar", Color(0xFF10B981), onCompanyClick))
            add(ActionItem(Icons.Default.Group, "Referidos", Color(0xFFF59E0B), onReferralsClick))
            add(ActionItem(Icons.Default.AccountBalanceWallet, "Billetera", Color(0xFF8B5CF6), onWalletClick))
            add(ActionItem(Icons.AutoMirrored.Filled.TrendingUp, "Ganancias", Color(0xFFEC4899), onEarningsClick))
            add(ActionItem(Icons.Default.Money, "Retiros", Color(0xFF06B6D4), onWithdrawClick))
            add(ActionItem(Icons.AutoMirrored.Filled.Send, "Enviar", Color(0xFF3B82F6), onWalletTransferClick))
            add(ActionItem(Icons.Default.Person, "Perfil", Color(0xFF64748B), onProfileClick))

            add(ActionItem(Icons.Default.ShoppingBag, "Mis Compras", Color(0xFFFF6B35), onMisComprasClick))
            add(ActionItem(Icons.Default.ShoppingCart, "Carrito", Color(0xFFF43F5E), onCartClick))
            if (hasCompany) {
                add(ActionItem(Icons.Default.Storefront, "Productos", Color(0xFF795548), onMyProductsClick))
                add(ActionItem(Icons.Default.Store, "Mis Ventas", Color(0xFF059669), onMisVentasClick))
                add(ActionItem(Icons.Default.History, "Historial", Color(0xFF14B8A6), onHistoryClick))
                add(ActionItem(Icons.Default.Payments, "Pagos", Color(0xFF6366F1), onPaymentsClick))
            }
        }

        val rows = actionItems.chunked(4)
        rows.forEachIndexed { rowIndex, rowItems ->
            if (rowIndex > 0) Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    ModernQuickActionButton(
                        icon = item.icon,
                        label = item.label,
                        color = item.color,
                        onClick = item.onClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

private data class ActionItem(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun ModernQuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ReferralCodeCard(referralCode: String, onCopy: (String) -> Unit, onShare: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WorkspacePremium, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.referral_code),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = referralCode.ifBlank { "SIN CÓDIGO" },
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    letterSpacing = 4.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { onCopy(referralCode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copiar", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onShare(referralCode) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatsSection(totalReferrals: Int, level1Referrals: Int, level2Referrals: Int, level3Referrals: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.your_statistics),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Text(
                    text = totalReferrals.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModernStatCard("Nivel 1", level1Referrals, Color(0xFF10B981), Modifier.weight(1f))
            ModernStatCard("Nivel 2", level2Referrals, Color(0xFFF59E0B), Modifier.weight(1f))
            ModernStatCard("Nivel 3", level3Referrals, Color(0xFF8B5CF6), Modifier.weight(1f))
        }
    }
}

@Composable
private fun ModernStatCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun PublicCompaniesCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = Color(0xFF10B981).copy(alpha = 0.15f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Storefront, null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.public_companies), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(stringResource(R.string.discover_companies), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0xFF10B981))
        }
    }
}

@Composable
private fun CompanyInfoCard(companyName: String, isActive: Boolean, status: String, onClick: () -> Unit) {
    val statusColor = if (isActive) Color(0xFF10B981) else Color(0xFFF59E0B)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = statusColor.copy(alpha = 0.15f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Business, null, tint = statusColor, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(companyName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernDrawerContent(
    userName: String, userEmail: String, isVerified: Boolean, isAdmin: Boolean, hasCompany: Boolean,
    hasEarningsAvailable: Boolean, hasWithdrawalsAvailable: Boolean, selfieUrl: String?,
    isMotorizado: Boolean = false, pendingInvitationsCount: Int = 0,
    onNavigateToProfile: () -> Unit, onNavigateToReferrals: () -> Unit, onNavigateToCompany: () -> Unit,
    onNavigateToQR: () -> Unit, onNavigateToAdminEarnings: () -> Unit, onNavigateToAdminWithdrawals: () -> Unit,
    onNavigateToMyProducts: () -> Unit, onNavigateToCart: () -> Unit, onNavigateToHistory: () -> Unit,
    onNavigateToPayments: () -> Unit, onNavigateToEarnings: () -> Unit, onNavigateToWithdrawal: () -> Unit,
    onNavigateToWallet: () -> Unit, onNavigateToWalletTransfer: () -> Unit,
    onNavigateToDriverPanel: () -> Unit = {}, onNavigateToDriverHistory: () -> Unit = {},
    onNavigateToDriverInvitations: () -> Unit = {}, onNavigateToAdminLiveMap: () -> Unit = {},
    onLogout: () -> Unit
) {
    ModalDrawerSheet(modifier = Modifier.width(310.dp), drawerContainerColor = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier.fillMaxWidth().background(
                    brush = Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
                ).padding(24.dp)
            ) {
                Column {
                    Surface(modifier = Modifier.size(72.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            if (selfieUrl != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current).data(selfieUrl).crossfade(true).build(),
                                    contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(userName.take(1), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimary)
                    Text(userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            DrawerSectionLabel("General")
            DrawerItem(Icons.Default.Person, "Perfil", onNavigateToProfile)
            DrawerItem(Icons.Default.Group, "Referidos", onNavigateToReferrals)
            DrawerItem(Icons.Default.Business, "Mi Empresa", onNavigateToCompany)

            DrawerSectionLabel("Finanzas")
            DrawerItem(Icons.Default.AccountBalanceWallet, "Billetera", onNavigateToWallet)
            DrawerItem(Icons.AutoMirrored.Filled.Send, "Transferir", onNavigateToWalletTransfer)
            if (hasEarningsAvailable) DrawerItem(Icons.AutoMirrored.Filled.TrendingUp, "Ganancias", onNavigateToEarnings)
            if (hasWithdrawalsAvailable) DrawerItem(Icons.Default.Money, "Retiros", onNavigateToWithdrawal)

            if (hasCompany) {
                DrawerSectionLabel("Empresa")
                DrawerItem(Icons.Default.QrCode2, "Pagos QR", onNavigateToQR)
                DrawerItem(Icons.Default.Store, "Productos", onNavigateToMyProducts)
                DrawerItem(Icons.Default.ShoppingCart, "Carrito", onNavigateToCart)
            }

            if (isAdmin) {
                DrawerSectionLabel("Admin")
                DrawerItem(Icons.Default.AttachMoney, "Ganancias Admin", onNavigateToAdminEarnings)
                DrawerItem(Icons.Default.Map, "Conductores en Vivo", onNavigateToAdminLiveMap)
            }

            if (isMotorizado || pendingInvitationsCount > 0) {
                DrawerSectionLabel("Motorizado")
                if (pendingInvitationsCount > 0) {
                    DrawerItem(Icons.Default.MailOutline, "Invitaciones ($pendingInvitationsCount)", onNavigateToDriverInvitations)
                }
                if (isMotorizado) {
                    DrawerItem(Icons.Default.DirectionsBike, "Mis Pedidos", onNavigateToDriverPanel)
                    DrawerItem(Icons.Default.History, "Historial de Entregas", onNavigateToDriverHistory)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(24.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            DrawerItem(Icons.AutoMirrored.Filled.Logout, "Cerrar Sesión", onLogout, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerSectionLabel(label: String) {
    Text(
        text = label.uppercase(),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        letterSpacing = 1.sp
    )
}

@Composable
private fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit, color: Color = MaterialTheme.colorScheme.primary) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LogoutConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cerrar Sesión", fontWeight = FontWeight.ExtraBold) },
        text = { Text("¿Estás seguro que deseas salir?") },
        confirmButton = { Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Salir", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = RoundedCornerShape(24.dp)
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Code", text))
    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
}

private fun shareReferralCode(context: Context, code: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Únete a EasyRefer con mi código: $code")
    }
    context.startActivity(Intent.createChooser(intent, "Compartir código"))
}
