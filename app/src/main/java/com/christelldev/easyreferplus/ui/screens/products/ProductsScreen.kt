package com.christelldev.easyreferplus.ui.screens.products

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.ColorPainter
import coil.compose.AsyncImage
import com.christelldev.easyreferplus.R
import com.christelldev.easyreferplus.data.model.Product
import com.christelldev.easyreferplus.data.model.ProductCategory
import com.christelldev.easyreferplus.ui.theme.DesignConstants

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
    val isDark = isSystemInDarkTheme()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (isCompanyOwner) {
                LargeFloatingActionButton(
                    onClick = onAddProduct,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", modifier = Modifier.size(32.dp))
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Gradiente superior sutil
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp)
                    .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent)))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                // Cabecera Premium
                TopAppBar(
                    title = {
                        Text(
                            text = "Mis Productos",
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (products.isEmpty()) {
                    EmptyProductsState(isCompanyOwner, onAddProduct)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(products, key = { it.id }) { product ->
                            ProductCardPremium(
                                product = product,
                                onEdit = if (isCompanyOwner) { { onEditProduct(product) } } else null,
                                onDelete = if (isCompanyOwner) { { onDeleteProduct(product.id) } } else null,
                                onClick = { onViewProduct(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardPremium(
    product: Product,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onClick: () -> Unit
) {
    val statusColor = when (product.status) {
        "active" -> Color(0xFF10B981)
        "draft" -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Imagen
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                if (product.images.isNotEmpty()) {
                    AsyncImage(
                        model = product.primaryImage?.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = remember { ColorPainter(Color(0xFFE0E0E0)) },
                        error = remember { ColorPainter(Color(0xFFEEEEEE)) }
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(product.productName, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = product.productDescription ?: "Sin descripción",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$${String.format(java.util.Locale.US, "%.2f", product.displayPrice)}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.offerPrice != null && product.offerPrice < product.price) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "$${String.format(java.util.Locale.US, "%.2f", product.price)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
                
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = product.status.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = statusColor,
                        fontSize = 9.sp
                    )
                }
            }

            // Actions
            if (onEdit != null || onDelete != null) {
                Column(horizontalAlignment = Alignment.End) {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (onDelete != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)) {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
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
    onSave: (name: String, description: String?, categoryId: Int?, size: String?, weight: String?, dimensions: String?, quantity: Int, price: Double, offerPrice: Double?, commission: Double?, useCompanyDefault: Boolean, status: String, keywords: String?) -> Unit,
    onUploadImage: (Int) -> Unit,
    onDeleteImage: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val scrollState = rememberScrollState()
    
    var name by remember { mutableStateOf(product?.productName ?: "") }
    var description by remember { mutableStateOf(product?.productDescription ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.productCategoryId) }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "0") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var offerPrice by remember { mutableStateOf(product?.offerPrice?.toString() ?: "") }
    var useCompanyDefault by remember { mutableStateOf(product?.useCompanyDefault ?: true) }
    var status by remember { mutableStateOf(product?.status ?: "active") }
    var keywords by remember { mutableStateOf(product?.keywords ?: "") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), Color.Transparent))))

            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(if (product == null) "Nuevo Producto" else "Editar Producto", fontWeight = FontWeight.ExtraBold, color = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White) },
                    navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = if (isDark) MaterialTheme.colorScheme.onBackground else Color.White) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )

                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).verticalScroll(scrollState).imePadding()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ProductTextField(value = name, onValueChange = { name = it }, label = "Nombre del producto *")
                            ProductTextField(value = description, onValueChange = { description = it }, label = "Descripción", maxLines = 3)
                            
                            // Categoría
                            var expanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                                OutlinedTextField(
                                    value = categories.find { it.id == selectedCategoryId }?.name ?: "Seleccionar categoría",
                                    onValueChange = {}, readOnly = true, label = { Text("Categoría") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true), shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                                )
                                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                    categories.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { selectedCategoryId = it.id; expanded = false }) }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ProductTextField(value = price, onValueChange = { price = it }, label = "Precio *", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal, prefix = "$")
                                ProductTextField(value = offerPrice, onValueChange = { offerPrice = it }, label = "Oferta", modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal, prefix = "$")
                            }
                            ProductTextField(value = quantity, onValueChange = { quantity = it }, label = "Stock", keyboardType = KeyboardType.Number)
                            ProductTextField(value = keywords, onValueChange = { keywords = it }, label = "Palabras clave (separadas por coma)", maxLines = 2)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            onSave(name, description.ifBlank { null }, selectedCategoryId, null, null, null, quantity.toIntOrNull() ?: 0, price.toDoubleOrNull() ?: 0.0, offerPrice.toDoubleOrNull(), null, useCompanyDefault, status, keywords.ifBlank { null })
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = name.isNotBlank() && price.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        else Text("Guardar Cambios", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                    }
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun ProductTextField(
    value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier,
    maxLines: Int = 1, keyboardType: KeyboardType = KeyboardType.Text, prefix: String? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) },
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        maxLines = maxLines, singleLine = maxLines == 1,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Next),
        prefix = prefix?.let { { Text(it, fontWeight = FontWeight.Bold) } },
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
    )
}

@Composable
fun EmptyProductsState(isOwner: Boolean, onAdd: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ShoppingBag, null, modifier = Modifier.size(100.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Sin productos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
        Text("Empieza a cargar tu catálogo para que otros usuarios puedan referirte.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isOwner) {
            Spacer(modifier = Modifier.height(40.dp))
            Button(onClick = onAdd, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Añadir Producto", fontWeight = FontWeight.Black)
            }
        }
    }
}
