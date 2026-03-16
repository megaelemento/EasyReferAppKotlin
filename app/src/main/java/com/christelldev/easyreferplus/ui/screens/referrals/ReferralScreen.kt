package com.christelldev.easyreferplus.ui.screens.referrals

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.viewmodel.ReferralViewModel

// Constantes de diseño elegante
private val CARD_CORNER_RADIUS = 20.dp
private val CARD_ELEVATION = 8.dp
private val CARD_MARGIN_HORIZONTAL = 16.dp
private val GradientPrimary = listOf(Color(0xFF03A9F4), Color(0xFF2196F3))
private val GradientSuccess = listOf(Color(0xFF10B981), Color(0xFF34D399))
private val GradientOrange = listOf(Color(0xFFF59E0B), Color(0xFFFBBF24))
private val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    viewModel: ReferralViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Inicializar WebSocket y conectar (en orden garantizado)
    LaunchedEffect(Unit) {
        viewModel.initWebSocketManager(context)
        viewModel.connectWebSocket()
    }

    // Desconectar al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.disconnectWebSocket()
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, context.getString(R.string.code_copied), Toast.LENGTH_SHORT).show()
    }

    val snackbarHostState = androidx.compose.runtime.remember { androidx.compose.material3.SnackbarHostState() }

    // Mostrar errores
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.my_referrals_title),
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppBlue
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(16.dp)
            ) {
                // Tu código de referido
                YourReferralCodeCard(
                    code = uiState.userReferralCode,
                    onCopy = { copyToClipboard(uiState.userReferralCode) }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Estadísticas
                StatsSection(
                    level1Count = uiState.level1Count,
                    level2Count = uiState.level2Count,
                    level3Count = uiState.level3Count,
                    totalCount = uiState.totalCount
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Buscar referido
                SearchReferralSection(
                    searchCode = uiState.searchCode,
                    isSearching = uiState.isSearching,
                    searchResult = uiState.searchResult,
                    onSearchCodeChange = viewModel::updateSearchCode,
                    onSearch = viewModel::searchReferral,
                    onClearResult = viewModel::clearSearchResult
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Árbol de referidos por nivel
                if (uiState.totalCount > 0) {
                    ReferralTreeSection(
                        level1Codes = uiState.level1Codes,
                        level2Codes = uiState.level2Codes,
                        level3Codes = uiState.level3Codes,
                        onCopyCode = { copyToClipboard(it) }
                    )
                } else {
                    EmptyReferralsMessage()
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun YourReferralCodeCard(
    code: String,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.horizontalGradient(colors = GradientPrimary))
                .combinedClickable(
                    onClick = { },
                    onLongClick = { onCopy() }
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.your_code),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.copy_code),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StatsSection(
    level1Count: Int,
    level2Count: Int,
    level3Count: Int,
    totalCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(
            count = level1Count,
            label = stringResource(R.string.level_1),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            count = level2Count,
            label = stringResource(R.string.level_2),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            count = level3Count,
            label = stringResource(R.string.level_3),
            modifier = Modifier.weight(1f)
        )
        StatItem(
            count = totalCount,
            label = stringResource(R.string.total_referrals),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatItem(
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val gradient = when (label) {
        stringResource(R.string.level_1) -> GradientSuccess
        stringResource(R.string.level_2) -> GradientOrange
        stringResource(R.string.level_3) -> GradientPurple
        else -> GradientPrimary
    }

    Card(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.linearGradient(colors = gradient))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchReferralSection(
    searchCode: String,
    isSearching: Boolean,
    searchResult: com.christelldev.easyreferplus.ui.viewmodel.SearchResult?,
    onSearchCodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearResult: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.search_referral),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchCode,
                    onValueChange = {
                        onSearchCodeChange(it)
                        onClearResult()
                    },
                    placeholder = { Text(stringResource(R.string.search_referral_hint)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onSearch,
                    enabled = !isSearching && searchCode.isNotBlank(),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppBlue)
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_button),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Resultado de búsqueda
            searchResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                val (backgroundColor, textColor, message) = if (result.found) {
                    Triple(
                        Color(0xFFC8E6C9),
                        Color(0xFF2E7D32),
                        stringResource(R.string.code_found, result.level ?: 0)
                    )
                } else {
                    Triple(
                        Color(0xFFFFCDD2),
                        Color(0xFFC62828),
                        stringResource(R.string.code_not_found)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundColor)
                        .padding(12.dp)
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ReferralTreeSection(
    level1Codes: List<String>,
    level2Codes: List<String>,
    level3Codes: List<String>,
    onCopyCode: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.referral_tree),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (level1Codes.isNotEmpty()) {
            ReferralLevelCard(
                level = stringResource(R.string.level_1),
                codes = level1Codes,
                onCopyCode = onCopyCode
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (level2Codes.isNotEmpty()) {
            ReferralLevelCard(
                level = stringResource(R.string.level_2),
                codes = level2Codes,
                onCopyCode = onCopyCode
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (level3Codes.isNotEmpty()) {
            ReferralLevelCard(
                level = stringResource(R.string.level_3),
                codes = level3Codes,
                onCopyCode = onCopyCode
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReferralLevelCard(
    level: String,
    codes: List<String>,
    onCopyCode: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "$level (${codes.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = AppBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            codes.forEach { code ->
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .combinedClickable(
                            onClick = { },
                            onLongClick = { onCopyCode(code) }
                        )
                )
            }
        }
    }
}

@Composable
fun EmptyReferralsMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_referrals),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.invite_friends_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
