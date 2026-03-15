package com.christelldev.easyreferplus.ui.screens.products

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.data.model.Product
import com.christelldev.easyreferplus.data.model.ProductCategory
import com.christelldev.easyreferplus.ui.theme.AppBlue
import com.christelldev.easyreferplus.ui.theme.DesignConstants

// Constantes de diseño elegante
private val CARD_CORNER_RADIUS = DesignConstants.CARD_CORNER_RADIUS
private val CARD_ELEVATION = DesignConstants.CARD_ELEVATION
private val CARD_MARGIN_HORIZONTAL = DesignConstants.CARD_MARGIN_HORIZONTAL
private val GradientPrimary = DesignConstants.GradientPrimary
private val GradientSuccess = DesignConstants.GradientSuccess
private val GradientPurple = DesignConstants.GradientPurple
private val GradientOrange = DesignConstants.GradientOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProductsScreen(
    products: List<Product> = emptyList(),
    categories: List<ProductCategory> = emptyList(),
    isLoading: Boolean = false,
    isCompanyOwner: Boolean = false,
    onAddProduct: () -> Unit = {},
    onEditProduct: (Product) -> Unit = {},
    onDeleteProduct: (Int) -> Unit = {},
    onViewProduct: (Product) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Productos",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (isCompanyOwner) {
                        IconButton(onClick = onAddProduct) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Agregar producto",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.surface,
                    actionIconContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(colors = GradientPrimary)
                )
            )
        },
        floatingActionButton = {
            if (isCompanyOwner) {
                FloatingActionButton(
                    onClick = onAddProduct,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.background(
                        brush = Brush.linearGradient(colors = GradientPrimary),
                        shape = CircleShape
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar producto", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                products.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No tienes productos",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        if (isCompanyOwner) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Toca + para agregar tu primer producto",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                onEdit = if (isCompanyOwner) {{ onEditProduct(product) }} else null,
                                onDelete = if (isCompanyOwner) {{ onDeleteProduct(product.id) }} else null,
                                onView = {{ onViewProduct(product) }}
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onView: () -> Unit = {}
) {
    // Determinar el gradiente basado en el estado
    val statusGradient = when (product.status) {
        "active" -> GradientSuccess
        "draft" -> GradientOrange
        else -> listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), MaterialTheme.colorScheme.outline)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CARD_MARGIN_HORIZONTAL)
            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS))
            .clickable { onView() },
        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen con gradiente de fondo
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE3F2FD),
                                Color(0xFFBBDEFB)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.primaryImage?.imageUrl,
                        contentDescription = product.productName,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(colors = GradientPrimary),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (product.productDescription != null) {
                    Text(
                        product.productDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$${String.format("%.2f", product.displayPrice)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF03A9F4),
                        fontWeight = FontWeight.Bold
                    )

                    if (product.offerPrice != null && product.offerPrice < product.price) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$${String.format("%.2f", product.price)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }

                if (product.quantity != null && product.quantity > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Stock: ${product.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status badge con gradiente
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(colors = statusGradient),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        product.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.surface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Actions
            Column {
                if (onEdit != null) {
                    Card(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onEdit() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF03A9F4).copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFF03A9F4),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                if (onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onDelete() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF44336).copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    product: Product? = null,
    categories: List<ProductCategory> = emptyList(),
    isLoading: Boolean = false,
    successMessage: String? = null,
    onSave: (
        name: String,
        description: String?,
        categoryId: Int?,
        size: String?,
        weight: String?,
        dimensions: String?,
        quantity: Int,
        price: Double,
        offerPrice: Double?,
        commission: Double?,
        useCompanyDefault: Boolean,
        status: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onUploadImage: (Int) -> Unit = {},
    onDeleteImage: (Int) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // Mostrar snackbar cuando hay mensaje de éxito
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            // Navegar hacia atrás después de mostrar el mensaje
            onNavigateBack()
        }
    }
    var name by remember { mutableStateOf(product?.productName ?: "") }
    var description by remember { mutableStateOf(product?.productDescription ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.productCategoryId) }
    var size by remember { mutableStateOf(product?.size ?: "") }
    var weight by remember { mutableStateOf(product?.weight ?: "") }
    var dimensions by remember { mutableStateOf(product?.dimensions ?: "") }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "0") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var offerPrice by remember { mutableStateOf(product?.offerPrice?.toString() ?: "") }
    var commission by remember { mutableStateOf(product?.specificCommissionPercentage?.toString() ?: "") }
    var useCompanyDefault by remember { mutableStateOf(product?.useCompanyDefault ?: true) }
    var status by remember { mutableStateOf(product?.status ?: "active") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (product == null) "Nuevo Producto" else "Editar Producto",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = Color.White
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(colors = GradientPrimary)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del producto *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }

            // Category dropdown
            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Dimensions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = size,
                        onValueChange = { size = it },
                        label = { Text("Talla") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        label = { Text("Peso") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dimensions,
                        onValueChange = { dimensions = it },
                        label = { Text("Dimensiones") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad en stock") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Precio *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("$") }
                    )
                    OutlinedTextField(
                        value = offerPrice,
                        onValueChange = { offerPrice = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Precio oferta") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        prefix = { Text("$") }
                    )
                }
            }

            // Commission
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = useCompanyDefault,
                        onCheckedChange = { useCompanyDefault = it }
                    )
                    Text("Usar comisión de la empresa")
                }

                if (!useCompanyDefault) {
                    OutlinedTextField(
                        value = commission,
                        onValueChange = { commission = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Comisión (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        suffix = { Text("%") }
                    )
                }
            }

            // Status
            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = status.replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Estado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("active", "draft", "hidden").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    status = s
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Image upload section - solo si ya existe el producto (tiene ID)
            if (product != null && product.id > 0) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                        shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Imágenes del Producto",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Mostrar imágenes existentes
                            if (product.images.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    product.images.forEach { image ->
                                        Box(
                                            modifier = Modifier.size(80.dp)
                                        ) {
                                            AsyncImage(
                                                model = image.imageUrl,
                                                contentDescription = "Imagen del producto",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(12.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            // Botón de eliminar
                                            IconButton(
                                                onClick = { onDeleteImage(image.id) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                        shape = CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar imagen",
                                                    tint = MaterialTheme.colorScheme.surface,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Botón para agregar imagen
                            OutlinedButton(
                                onClick = { onUploadImage(product.id) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar Imagen")
                            }
                        }
                    }
                }
            }

            // Save button con gradiente
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = CARD_ELEVATION, shape = RoundedCornerShape(CARD_CORNER_RADIUS)),
                    shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Button(
                        onClick = {
                            val priceVal = price.toDoubleOrNull() ?: 0.0
                            val offerPriceVal = offerPrice.toDoubleOrNull()
                            val commissionVal = commission.toDoubleOrNull()
                            val quantityVal = quantity.toIntOrNull() ?: 0

                            onSave(
                                name,
                                description.ifBlank { null },
                                selectedCategoryId,
                                size.ifBlank { null },
                                weight.ifBlank { null },
                                dimensions.ifBlank { null },
                                quantityVal,
                                priceVal,
                                offerPriceVal,
                                commissionVal,
                                useCompanyDefault,
                                status
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (name.isNotBlank() && price.isNotBlank() && !isLoading)
                                        GradientPrimary else listOf(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        enabled = name.isNotBlank() && price.isNotBlank() && !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            Text(
                                "Guardar Producto",
                                color = MaterialTheme.colorScheme.surface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
