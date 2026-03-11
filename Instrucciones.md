# Instrucciones Técnicas - Carrito de Compras con QR

## Problema Actual
Cuando el usuario presiona "Finalizar Compra" en el carrito, no aparece ningún código QR que la empresa pueda escanear para registrar la venta y pagar las comisiones a los referidos.

## Flujo Esperado
1. Cliente hace checkout → App muestra código QR
2. Cliente muestra QR a la empresa
3. Empresa escanea el QR → Se registra la venta
4. Sistema calcula y distribuye comisiones a referidos

---

## PARTE 1: Backend - Modificar checkout para generar QR

### Archivo: `E:\Users\Chris\StudioProjects\Proyectos WEB\EasyReferPython\api\cart.py`

**Cambio en la función checkout (línea 295-365):**

Necesitas importar los modelos de QR Transaction al inicio del archivo (después de las importaciones existentes):

```python
# Agregar después de las importaciones existentes
from models.qr_transaction import QRTransaction, generate_qr_code_secret
from datetime import datetime, timedelta
```

**Modificar la función checkout para crear un QRTransaction:**

Reemplaza la función completa `checkout` (líneas 295-365) con este código:

```python
@router.get("/cart/checkout", response_model=dict)
async def checkout(
    request: Request,
    current_user: User = Depends(get_current_verified_user),
    db: Session = Depends(get_db)
):
    """
    Generar código QR para checkout - el cliente muestra el QR a la empresa
    """
    try:
        cart_items = db.query(CartItem).filter(
            CartItem.user_id == current_user.id
        ).all()

        if not cart_items:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El carrito está vacío"
            )

        # Obtener información de productos
        checkout_items = []
        total = 0
        reference_code = str(uuid.uuid4())[:8].upper()

        # Recolectar empresas únicas de los productos
        companies_in_cart = {}

        for item in cart_items:
            product = db.query(CompanyProduct).filter(
                CompanyProduct.id == item.product_id,
                CompanyProduct.status == 'active'
            ).first()

            if product:
                company = db.query(Company).filter(Company.id == product.company_id).first()

                item_total = product.current_price * item.quantity
                total += item_total

                checkout_items.append({
                    "product_id": product.id,
                    "product_name": product.product_name,
                    "quantity": item.quantity,
                    "unit_price": product.current_price,
                    "subtotal": item_total,
                    "company_name": company.company_name if company else None,
                    "company_id": company.id if company else None
                })

                # Guardar empresa para crear QR
                if company and company.id not in companies_in_cart:
                    companies_in_cart[company.id] = company

        if not companies_in_cart:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No hay empresas disponibles para procesar esta compra"
            )

        # ============================================
        # GENERAR QR TRANSACTION (CÓDIGO NUEVO)
        # ============================================
        qr_code = generate_qr_code_secret(32)
        qr_secret = generate_qr_code_secret(32)

        # Usar la primera empresa del carrito (o puedes manejar multi-empresa)
        primary_company = list(companies_in_cart.values())[0]

        # Calcular expiración (24 horas para dar tiempo al cliente)
        expires_at = datetime.utcnow() + timedelta(hours=24)

        # Crear registro de transacción QR
        qr_transaction = QRTransaction(
            transaction_id=QRTransaction.generate_transaction_id(),
            transaction_type="purchase",  # purchase = el cliente compra
            qr_code=qr_code,
            qr_secret=qr_secret,
            company_id=primary_company.id,
            seller_user_id=primary_company.owner_user_id,
            amount=total,
            currency="USD",
            description=f"Compra de ${total} - ${len(checkout_items)} productos",
            buyer_user_id=current_user.id,  # El cliente es el comprador
            referral_code_used=current_user.referral_code,  # Guardar código de referido
            expires_at=expires_at
        )

        db.add(qr_transaction)
        db.commit()
        db.refresh(qr_transaction)

        # Generar datos para el QR
        qr_payload = f"QR_CODE:{qr_code}|COMPANY:{primary_company.id}|AMOUNT:{total}|BUYER:{current_user.id}"
        qr_data_url = f"easyrefer://checkout?code={qr_code}&secret={qr_secret}"

        return {
            "success": True,
            "reference_code": reference_code,
            "qr_code": qr_code,           # Código QR para mostrar
            "qr_payload": qr_payload,      # Datos del QR
            "qr_data_url": qr_data_url,   # URL profunda
            "payment_url": f"/checkout/{reference_code}",
            "items": checkout_items,
            "total": total,
            "company_name": primary_company.company_name,
            "company_id": primary_company.id,
            "payment_method": "Pago en tienda - Muestra este QR",
            "instructions": "Muestra este código QR a la empresa para registrar tu compra. El pago se realiza en tienda."
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error en checkout: {str(e)}")
        db.rollback()
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Error interno del servidor"
        )
```

---

## PARTE 2: Android - Mostrar QR en el Carrito

### Archivo: `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\data\network\ProductRepository.kt`

**Agregar la respuesta de checkout al modelo (buscar CheckoutResponse):**

Revisar que el modelo `CheckoutResponse` en `ProductModels.kt` tenga los campos del QR:

```kotlin
// En data/model/ProductModels.kt - buscar data class CheckoutResponse
data class CheckoutResponse(
    val success: Boolean,
    val message: String? = null,
    val referenceCode: String? = null,
    val qrCode: String? = null,           // AGREGAR ESTE CAMPO
    val qrPayload: String? = null,        // AGREGAR ESTE CAMPO
    val qrDataUrl: String? = null,        // AGREGAR ESTE CAMPO
    val paymentUrl: String? = null,
    val items: List<CheckoutItem>? = null,
    val total: Double? = null,
    val companyName: String? = null,      // AGREGAR ESTE CAMPO
    val companyId: Int? = null,          // AGREGAR ESTE CAMPO
    val paymentMethod: String? = null,
    val instructions: String? = null
)
```

**En ProductRepository.kt - función checkout:**

```kotlin
// Modificar para mapear los nuevos campos del QR
suspend fun checkout(authorization: String): CheckoutResult {
    // ... código existente ...
    return try {
        val response = apiService.checkout(authorization)
        if (response.isSuccessful && response.body() != null) {
            val body = response.body()!!
            if (body.success) {
                CheckoutResult.Success(
                    referenceCode = body.referenceCode ?: "",
                    qrCode = body.qrCode ?: "",              // AGREGAR
                    qrPayload = body.qrPayload ?: "",         // AGREGAR
                    qrDataUrl = body.qrDataUrl ?: "",         // AGREGAR
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
```

---

### Archivo: `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\data\network\ProductRepository.kt`

**Agregar qrCode a CheckoutResult:**

```kotlin
// Buscar sealed class CheckoutResult y agregar:
sealed class CheckoutResult {
    data class Success(
        val referenceCode: String,
        val qrCode: String,              // NUEVO
        val qrPayload: String,            // NUEVO
        val qrDataUrl: String,            // NUEVO
        val paymentUrl: String,
        val items: List<com.christelldev.easyreferplus.data.model.CheckoutItem>,
        val total: Double,
        val instructions: String
    ) : CheckoutResult()
    data class Error(val message: String) : CheckoutResult()
}
```

---

### Archivo: `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\ui\viewmodel\ProductViewModel.kt`

**Actualizar el estado de CheckoutState:**

```kotlin
// En la clase ProductViewModel, actualizar CheckoutState:
sealed class CheckoutState {
    data object Idle : CheckoutState()
    data object Processing : CheckoutState()
    data class Success(
        val message: String,
        val orderId: String?,
        val qrCode: String?,         // NUEVO
        val qrPayload: String?,      // NUEVO
        val companyName: String?     // NUEVO
    ) : CheckoutState()
    data class Error(val message: String) : CheckoutState()
}
```

**Actualizar la función checkout():**

```kotlin
fun checkout() {
    viewModelScope.launch {
        _checkoutState.value = CheckoutState.Processing
        when (val result = repository.checkout(authorization)) {
            is CheckoutResult.Success -> {
                _cartItems.value = emptyList()
                _cartCount.value = 0
                _checkoutState.value = CheckoutState.Success(
                    message = result.instructions.ifBlank { "Tu pedido ha sido creado exitosamente" },
                    orderId = result.referenceCode,
                    qrCode = result.qrCode,           // NUEVO
                    qrPayload = result.qrPayload,     // NUEVO
                    companyName = "" // El backend debería devolver esto
                )
            }
            is CheckoutResult.Error -> {
                _checkoutState.value = CheckoutState.Error(result.message)
            }
        }
    }
}
```

---

### Archivo: `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\ui\screens\cart\CartScreen.kt`

**Agregar importación para QR:**

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import android.graphics.Bitmap
import androidm.webkit.WebView
import androidm.graphics.BitmapFactory
// AGREGAR: Generación de QR
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
```

**Nota:** Necesitarás agregar la dependencia de ZXing en build.gradle:
```groovy
implementation 'com.journeyapps:zxing-android-embedded:4.3.0'
implementation 'com.google.zxing:core:3.5.2'
```

**Modificar el CheckoutResultDialog para mostrar el QR:**

```kotlin
@Composable
fun CheckoutResultDialog(
    isSuccess: Boolean,
    message: String,
    orderId: String? = null,
    qrCode: String? = null,          // NUEVO PARÁMETRO
    companyName: String? = null,     // NUEVO PARÁMETRO
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de resultado
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = if (isSuccess) Color(0xFF10B981).copy(alpha = 0.1f)
                                    else Color(0xFFF44336).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = if (isSuccess) Color(0xFF10B981) else Color(0xFFF44336)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isSuccess) "¡Compra Exitosa!" else "Error en la Compra",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                // MOSTRAR QR SI ESTÁ DISPONIBLE
                if (isSuccess && qrCode != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Muestra este QR a la empresa",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppBlue,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Generar y mostrar QR
                    val qrBitmap = remember(qrCode) { generateQRCode(qrCode, 300) }
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Código QR",
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Código: ${orderId ?: qrCode.take(8)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }

                if (orderId != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Orden: #$orderId",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) Color(0xFF10B981) else AppBlue
                    )
                ) {
                    Text(
                        text = if (isSuccess) "Aceptar" else "Intentar de Nuevo",
                        color = Color.White
                    )
                }
            }
        }
    }
}

// Función para generar código QR
@Composable
fun generateQRCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
```

**Agregar import de Image:**

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
```

---

### Actualizar el llamado al diálogo en CartScreen:

```kotlin
// En la función CartScreen, cambiar el llamado a CheckoutResultDialog:
when (checkoutState) {
    is CheckoutState.Success -> {
        CheckoutResultDialog(
            isSuccess = true,
            message = checkoutState.message,
            orderId = checkoutState.orderId,
            qrCode = checkoutState.qrCode,        // NUEVO
            companyName = checkoutState.companyName, // NUEVO
            onDismiss = {
                onCheckoutDismiss()
                onNavigateBack()
            }
        )
    }
    // ... resto del código
}
```

---

## RESUMEN DE COLORES A USAR

- **AppBlue (Primary):** `#03A9F4` (3, 169, 244)
- **Verde Success:** `#10B981` (16, 185, 129)
- **Rojo Error:** `#F44336` (244, 67, 54)
- **Fondo Tarjeta:** `#FFFFFF` (White)
- **Gris Texto:** `#757575` (117, 117, 117)

---

## ARCHIVOS A MODIFICAR

1. `E:\Users\Chris\StudioProjects\Proyectos WEB\EasyReferPython\api\cart.py`
   - Agregar imports
   - Modificar función checkout()

2. `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\data\model\ProductModels.kt`
   - Agregar campos QR a CheckoutResponse

3. `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\data\network\ProductRepository.kt`
   - Agregar campos QR a CheckoutResult
   - Mapear campos en función checkout()

4. `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\ui\viewmodel\ProductViewModel.kt`
   - Agregar campos QR a CheckoutState
   - Actualizar función checkout()

5. `E:\Users\Chris\StudioProjects\EasyReferPlus\app\src\main\java\com\christelldev\easyreferplus\ui\screens\cart\CartScreen.kt`
   - Agregar imports (Image, ZXing)
   - Agregar función generateQRCode()
   - Modificar CheckoutResultDialog para mostrar QR

6. `E:\Users\Chris\StudioProjects\EasyReferPlus\app\build.gradle` (app level)
   - Agregar dependencia ZXing

---

## NOTAS IMPORTANTES

1. El backend debe devolver `qr_code`, `qr_payload`, y `company_name` en la respuesta del checkout
2. El QR se genera localmente en el Android usando la librería ZXing
3. El QR contiene el código único que la empresa escaneará para registrar la venta
4. Al escanear, el sistema automáticamente asocia la venta al comprador y calcula comisiones

---

**Proceso Terminado**
