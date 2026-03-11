# Documentación Completa de Endpoints - Aplicación Móvil EasyRefer

## Autenticación y Seguridad

### Iniciar Sesión
- **Endpoint**: `POST /api/auth/login`
- **Descripción**: Autentica al usuario y devuelve tokens JWT
- **Body**:
```json
{
  "phone": "+593987654321",
  "password": "MiPassword123"
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Login exitoso",
  "access_token": "eyJ...",
  "refresh_token": "eyJ...",
  "token_type": "bearer",
  "expires_in": 1800,
  "refresh_expires_in": 2592000,
  "user_id": 5,
  "nombres": "JUAN",
  "apellidos": "PEREZ",
  "email": "juan@email.com",
  "phone": "+593987654321",
  "referral_code": "9089FBM51",
  "phone_verified": true,
  "is_verified": true,
  "session_id": "sess_abc123",
  "total_active_sessions": 1,
  "max_sessions": 5
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Credenciales incorrectas
    ```json
    {
      "success": false,
      "message": "Teléfono o contraseña incorrectos",
      "remaining_attempts": 2,
      "error_type": "invalid_credentials"
    }
    ```
  - **429 Too Many Requests**: Cuenta bloqueada por intentos fallidos
    ```json
    {
      "success": false,
      "message": "Demasiados intentos fallidos. Cuenta bloqueada por 60 segundos.",
      "locked_until_seconds": 60,
      "error_type": "login_locked"
    }
    ```
  - **403 Forbidden**: Cuenta desactivada o teléfono no verificado
    ```json
    {
      "success": false,
      "message": "Cuenta desactivada. Contacte al administrador.",
      "error_type": "account_disabled"
    }
    ```
    o
    ```json
    {
      "success": false,
      "message": "Debe verificar su número de teléfono antes de iniciar sesión",
      "error_type": "phone_not_verified"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Al iniciar sesión en la aplicación
- **Advertencias**: Los tokens expiran y deben renovarse con el endpoint de refresh

### Obtener Perfil
- **Endpoint**: `GET /api/auth/profile`
- **Descripción**: Obtiene la información del perfil del usuario autenticado
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Respuesta Exitosa (200)**:
```json
{
  "user_id": 5,
  "nombres": "JUAN",
  "apellidos": "PEREZ",
  "email": "juan@email.com",
  "phone": "+593987654321",
  "cedula_ruc": "0912345678",
  "is_ruc": false,
  "empresa_nombre": null,
  "referral_code": "9089FBM51",
  "phone_verified": true,
  "is_verified": true,
  "has_company": false,
  "created_at": "2026-02-10T15:30:00"
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Cargar información del usuario al abrir la app
- **Advertencias**: Solo disponible para usuarios autenticados

### Actualizar Perfil
- **Endpoint**: `PUT /api/auth/profile`
- **Descripción**: Actualiza la información del perfil del usuario
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Body (sin empresa)**:
```json
{
  "nombres": "CARLOS",
  "apellidos": "GARCIA",
  "email": "carlos@email.com",
  "cedula_ruc": "0912345678"
}
```
- **Body (con empresa - solo email permitido)**:
```json
{
  "email": "nuevo@email.com"
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Perfil actualizado exitosamente",
  "updated_fields": ["email"],
  "profile": {
    "user_id": 5,
    "nombres": "JUAN",
    "apellidos": "PEREZ",
    "email": "nuevo@email.com",
    "phone": "+593987654321",
    "cedula_ruc": "0912345678",
    "is_ruc": false,
    "empresa_nombre": null,
    "has_company": true
  }
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Errores de validación (email/cedula duplicados, documento inválido)
    ```json
    {
      "success": false,
      "message": "Email ya registrado",
      "error_type": "validation_error",
      "errors": [
        {
          "field": "email",
          "message": "Este email ya está registrado"
        }
      ]
    }
    ```
  - **403 Forbidden**: Campo no permitido (ej: intentar editar `nombres` con empresa)
    ```json
    {
      "success": false,
      "message": "No tienes permiso para editar este campo",
      "error_type": "forbidden_field"
    }
    ```
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Cambiar información personal del usuario
- **Advertencias**: Algunos campos no se pueden editar después de crear empresa

### Cerrar Sesión
- **Endpoint**: `POST /api/auth/logout`
- **Descripción**: Cierra la sesión del usuario y invalida el refresh token
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Header Opcional**: `X-Refresh-Token: <refresh_token>`
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Logout exitoso. Refresh token invalidado."
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": true,
      "message": "Logout exitoso"
    }
    ```
- **Casos de Uso**: Al cerrar sesión manualmente o al desloguear por inactividad
- **Advertencias**: Recomendado enviar el refresh token para invalidarlo también

### Reenviar Código de Verificación
- **Endpoint**: `POST /api/auth/resend-phone-code`
- **Descripción**: Reenvía el código de verificación al número de teléfono
- **Body**:
```json
{
  "phone": "+593987654321"
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Código de verificación reenviado exitosamente"
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Demasiados intentos de reenvío
    ```json
    {
      "success": false,
      "message": "Demasiados intentos de reenvío. Debes solicitar un nuevo código.",
      "error_type": "too_many_resend_attempts"
    }
    ```
  - **429 Too Many Requests**: Rate limit excedido
    ```json
    {
      "success": false,
      "message": "Debes esperar antes de solicitar otro código",
      "wait_time": 60,
      "error_type": "rate_limited"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Cuando el usuario no recibe el código de verificación inicial
- **Advertencias**: 
  - Se permite un máximo de 2 reenvíos (3 códigos totales: 1 original + 2 reenvíos)
  - Después del 3er intento total, se debe reiniciar el proceso de verificación
  - Existe un límite de velocidad de 60 segundos entre reenvíos (después del primer reenvío)

### Renovar Tokens
- **Endpoint**: `POST /api/auth/refresh`
- **Descripción**: Renueva los tokens de acceso cuando el access_token expira
- **Body**:
```json
{
  "refresh_token": "eyJ..."
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Tokens renovados exitosamente",
  "access_token": "eyJ_nuevo...",
  "refresh_token": "eyJ_nuevo...",
  "token_type": "bearer",
  "expires_in": 1800,
  "refresh_expires_in": 2592000
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Refresh token inválido o expirado
    ```json
    {
      "detail": "Invalid refresh token"
    }
    ```
  - **429 Too Many Requests**: Demasiados intentos de refresh
    ```json
    {
      "success": false,
      "message": "Demasiados intentos de refresh. Espera X segundos.",
      "error_type": "rate_limit_exceeded",
      "retry_after": 300
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Cuando el access token expira (automáticamente en el cliente)
- **Advertencias**: Si falla, se debe redirigir al login

## Verificación y Registro

### Verificar Número de Teléfono
- **Endpoint**: `POST /api/auth/verify-phone-number`
- **Descripción**: Envía un código de verificación al número de teléfono
- **Body**:
```json
{
  "phone": "+593987654321"
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Codigo de verificacion enviado",
  "phone": "+593987654321"
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Teléfono ya registrado
    ```json
    {
      "success": false,
      "message": "Teléfono ya registrado",
      "error_type": "phone_exists"
    }
    ```
  - **429 Too Many Requests**: Rate limit excedido
    ```json
    {
      "success": false,
      "message": "Demasiadas solicitudes. Intente más tarde.",
      "error_type": "rate_limited"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Primer paso del proceso de registro
- **Advertencias**: 
  - Rate limiting aplicado (1 por minuto)
  - El código de verificación expira después de 10 minutos
  - Se permite un máximo de 3 solicitudes de código (1 original + 2 reenvíos) antes de que deba reiniciar el proceso

### Confirmar Código SMS
- **Endpoint**: `POST /api/auth/confirm-phone-code`
- **Descripción**: Confirma el código recibido por SMS
- **Body**:
```json
{
  "phone": "+593987654321",
  "code": "1234"
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Telefono verificado exitosamente",
  "verified": true,
  "verification_token": "eyJ..."
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Código incorrecto o expirado
    ```json
    {
      "success": false,
      "message": "Código incorrecto o expirado",
      "error_type": "invalid_code"
    }
    ```
  - **429 Too Many Requests**: Demasiados intentos
    ```json
    {
      "success": false,
      "message": "Demasiados intentos. Intente más tarde.",
      "error_type": "too_many_attempts"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Segundo paso del proceso de registro
- **Advertencias**: 
  - Máximo 3 intentos de verificación con códigos incorrectos antes de que el código expire
  - El código de verificación expira después de 10 minutos
  - Los intentos de verificación y reenvío son contadores separados

### Completar Registro
- **Endpoint**: `POST /api/auth/complete-registration`
- **Descripción**: Completa el proceso de registro con información personal
- **Body**:
```json
{
  "verification_token": "eyJ...",
  "referral_code": "REFIEREYA",
  "cedula_ruc": "0912345678",
  "nombres": "JUAN",
  "apellidos": "PEREZ",
  "empresa_nombre": null,
  "email": "juan@email.com",
  "password": "MiPassword123",
  "confirm_password": "MiPassword123",
  "is_adult": true,
  "accepts_privacy_policy": true
}
```
- **Respuesta Exitosa (201)**:
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "user_id": 5,
  "referral_code": "9089FBM51"
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Token de verificación inválido o datos duplicados
    ```json
    {
      "success": false,
      "message": "Token de verificación inválido",
      "error_type": "invalid_token"
    }
    ```
  - **422 Unprocessable Entity**: Validación de campos fallida
    ```json
    {
      "success": false,
      "message": "Error de validación",
      "error_type": "validation_error",
      "errors": [
        {
          "field": "cedula_ruc",
          "message": "Número de cédula es inválido"
        },
        {
          "field": "cedula_ruc",
          "message": "Número de RUC es inválido"
        },
        {
          "field": "email",
          "message": "Formato de email inválido"
        },
        {
          "field": "password",
          "message": "La contraseña debe tener al menos 8 caracteres"
        },
        {
          "field": "confirm_password",
          "message": "Las contraseñas no coinciden"
        },
        {
          "field": "is_adult",
          "message": "Debe ser mayor de edad para registrarse"
        },
        {
          "field": "accepts_privacy_policy",
          "message": "Debe aceptar las políticas de privacidad"
        }
      ]
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Tercer y último paso del proceso de registro
- **Advertencias**: 
  - **Validación de Cédula/RUC**: El sistema valida que la cédula o RUC cumpla con el algoritmo oficial ecuatoriano
  - **Mensajes de error simplificados**: Por razones de seguridad, todos los errores de validación de cédula/RUC mostrarán mensajes genéricos ("Número de cédula es inválido" o "Número de RUC es inválido") sin detalles específicos sobre el tipo de error
  - **Cédula (10 dígitos)**: 
    - Formato: 10 dígitos numéricos
  - **RUC (13 dígitos)**:
    - Formato: 13 dígitos numéricos
  - **Una vez registrado**, solo email y teléfono pueden modificarse
  - **Restricciones de edición**: Si el usuario tiene empresa registrada, solo puede editar el email

## Empresas

### Registrar Empresa
- **Endpoint**: `POST /api/companies/register`
- **Descripción**: Registra una nueva empresa para el usuario
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Body**:
```json
{
  "company_name": "Mi Empresa S.A.",
  "company_description": "Descripcion de la empresa",
  "product_description": "Productos que ofrece",
  "address": "Av. Principal 123",
  "city": "Guayaquil",
  "province": "Guayas",
  "latitude": -2.1894,
  "longitude": -79.8891,
  "website": "https://miempresa.com",
  "facebook_url": "https://facebook.com/miempresa",
  "instagram_url": "https://instagram.com/miempresa",
  "whatsapp_number": "+593987654321",
  "commission_percentage": 10.0,
  "payment_day": 15,
  "payment_frequency": "monthly"
}
```
- **Respuesta Exitosa (201)**:
```json
{
  "success": true,
  "message": "Empresa registrada exitosamente",
  "company_id": 123,
  "company_name": "Mi Empresa S.A."
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Usuario ya tiene empresa
    ```json
    {
      "success": false,
      "message": "Ya tienes una empresa registrada",
      "error_type": "company_exists"
    }
    ```
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **422 Unprocessable Entity**: Validación de campos fallida
    ```json
    {
      "success": false,
      "message": "Error de validación",
      "error_type": "validation_error",
      "errors": [
        {
          "field": "company_name",
          "message": "Nombre de empresa requerido"
        }
      ]
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Cuando un usuario quiere registrar su empresa en la plataforma
- **Advertencias**: Solo un usuario puede tener una empresa

### Ver Mi Empresa
- **Endpoint**: `GET /api/companies/my-company`
- **Descripción**: Obtiene la información de la empresa del usuario autenticado
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "company": {
    "id": 123,
    "company_name": "Mi Empresa S.A.",
    "company_description": "Descripcion de la empresa",
    "address": "Av. Principal 123",
    "city": "Guayaquil",
    "province": "Guayas",
    "latitude": -2.1894,
    "longitude": -79.8891,
    "website": "https://miempresa.com",
    "facebook_url": "https://facebook.com/miempresa",
    "instagram_url": "https://instagram.com/miempresa",
    "whatsapp_number": "+593987654321",
    "commission_percentage": 10.0,
    "status": "pending_validation",
    "payment_day": 15,
    "payment_frequency": "monthly",
    "created_at": "2026-02-10T15:30:00",
    "updated_at": "2026-02-10T15:30:00"
  }
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **404 Not Found**: Usuario no tiene empresa registrada
    ```json
    {
      "success": false,
      "message": "No tienes empresa registrada",
      "error_type": "no_company"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Visualizar información de la empresa en la app
- **Advertencias**: Disponible solo para usuarios con empresa

## Productos

### Agregar Producto
- **Endpoint**: `POST /api/companies/my-company/products`
- **Descripción**: Agrega un nuevo producto o servicio a la empresa del usuario
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Body**:
```json
{
  "product_name": "Producto X",
  "product_description": "Descripcion",
  "category": "general",
  "price": 25.50,
  "specific_commission_percentage": null,
  "use_company_default": true
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Producto agregado exitosamente",
  "product_id": 456
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **404 Not Found**: Usuario no tiene empresa
    ```json
    {
      "success": false,
      "message": "No tienes empresa registrada",
      "error_type": "no_company"
    }
    ```
  - **422 Unprocessable Entity**: Validación de campos fallida
    ```json
    {
      "success": false,
      "message": "Error de validación",
      "error_type": "validation_error",
      "errors": [
        {
          "field": "product_name",
          "message": "Nombre de producto requerido"
        }
      ]
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Añadir nuevos productos o servicios a la empresa
- **Advertencias**: Si se modifica un producto existente, la empresa pasa a estado pendiente de validación

### Actualizar Producto
- **Endpoint**: `PUT /api/companies/my-company/products/{product_id}`
- **Descripción**: Actualiza la información de un producto existente
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Body**:
```json
{
  "product_name": "Updated Product Name",
  "product_description": "Updated description of the product",
  "category": "Updated Category",
  "price": 99.99,
  "specific_commission_percentage": 15.0,
  "use_company_default": false,
  "display_order": 2,
  "is_active": true
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Producto actualizado exitosamente"
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **404 Not Found**: Producto no encontrado o usuario no tiene empresa
    ```json
    {
      "success": false,
      "message": "Producto no encontrado",
      "error_type": "product_not_found"
    }
    ```
  - **422 Unprocessable Entity**: Validación de campos fallida
    ```json
    {
      "success": false,
      "message": "Error de validación",
      "error_type": "validation_error",
      "errors": [
        {
          "field": "product_name",
          "message": "Nombre de producto muy corto"
        }
      ]
    }
    ```
  - **429 Too Many Requests**: Rate limit excedido
    ```json
    {
      "success": false,
      "message": "Demasiadas solicitudes. Intente nuevamente en X segundos.",
      "retry_after": 30
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Modificar información de productos existentes
- **Advertencias**: Al actualizar un producto, la empresa cambia a estado 'pending_validation'

## Transacciones

### Mis Transacciones
- **Endpoint**: `GET /api/companies/transactions/my-transactions`
- **Descripción**: Obtiene las transacciones asociadas al usuario
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Parámetros Query**: `page=1&per_page=20`
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "transactions": [
    {
      "id": 789,
      "amount": 100.00,
      "commission_amount": 10.00,
      "status": "completed",
      "created_at": "2026-02-10T15:30:00",
      "referenced_company": {
        "id": 123,
        "name": "Otra Empresa"
      }
    }
  ],
  "pagination": {
    "page": 1,
    "per_page": 20,
    "total": 1,
    "pages": 1
  }
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Visualizar historial de transacciones
- **Advertencias**: Paginado para grandes volúmenes de datos

## Retiros y Comisiones

### Saldo de Comisiones
- **Endpoint**: `GET /api/auth/withdrawals/balance`
- **Descripción**: Obtiene el saldo disponible para retiro
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "balance": {
    "available_balance": 500.00,
    "pending_balance": 100.00,
    "total_earned": 1000.00,
    "total_withdrawn": 400.00
  }
}
```
- **Errores Comunes**:
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Consultar saldo disponible para retiro
- **Advertencias**: El saldo puede incluir comisiones pendientes de procesamiento

### Solicitar Retiro
- **Endpoint**: `POST /api/auth/withdrawals/requests`
- **Descripción**: Crea una nueva solicitud de retiro
- **Cabecera Requerida**: `Authorization: Bearer <access_token>`
- **Body**:
```json
{
  "bank_account_id": 1,
  "requested_amount": 50.00,
  "notes": "Retiro mensual"
}
```
- **Respuesta Exitosa (200)**:
```json
{
  "success": true,
  "message": "Solicitud de retiro creada exitosamente",
  "request_id": 789
}
```
- **Errores Comunes**:
  - **400 Bad Request**: Saldo insuficiente o cuenta inválida
    ```json
    {
      "success": false,
      "message": "Saldo insuficiente para realizar el retiro",
      "error_type": "insufficient_funds"
    }
    ```
  - **401 Unauthorized**: Token inválido o expirado
    ```json
    {
      "detail": "Not authenticated"
    }
    ```
  - **422 Unprocessable Entity**: Validación de campos fallida
    ```json
    {
      "success": false,
      "message": "Error de validación",
      "error_type": "validation_error",
      "errors": [
        {
          "field": "requested_amount",
          "message": "Monto mínimo de retiro es $25.00"
        }
      ]
    }
    ```
  - **500 Internal Server Error**: Error interno del servidor
    ```json
    {
      "success": false,
      "message": "Error interno del servidor",
      "error_type": "internal_error"
    }
    ```
- **Casos de Uso**: Solicitar retiro de comisiones ganadas
- **Advertencias**: Las solicitudes requieren aprobación administrativa