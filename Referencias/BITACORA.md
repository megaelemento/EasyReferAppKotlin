# Bitácora de Desarrollo - EasyReferPlus

## Proyecto
**Nombre**: EasyReferPlus
**Tecnología**: Jetpack Compose + Kotlin
**Min SDK**: 26 | **Target SDK**: 36

---

## Actualización (2026-02-18) - Auto-login tras registro

### Problema
- Nuevo usuario se registraba pero al ir al Home no cargaba nada
- El token no se guardaba automáticamente tras el registro

### Solución

1. **RegisterRequest.kt** - Agregar campos de token a `CompleteRegistrationResponse`

2. **RegisterRepository.kt**:
   - Agregar `sharedPreferences` al constructor
   - Agregar método `saveTokens()`
   - Llamar `saveTokens()` tras registro exitoso
   - Actualizar `Factory` para recibir contexto

3. **RegisterResult.kt** - Actualizar `RegistrationSuccess` para incluir tokens

4. **MainActivity.kt**:
   - `MainNavigation` ahora recibe `registerRepository` como parámetro
   - Crear `RegisterRepository.Factory(this).create()` en el Activity

### Notas
- El backend ahora retorna tokens automáticamente tras el registro

---

## Actualización (2026-02-16) - Sistema de Pagos de Empresa

### Cambios Realizados

1. **CompanyModels.kt**
   - `PaymentAccessResponse`: Agregado campo `companyStatus`
   - `PaymentHistoryResponse`: Agregado campo `pendingCommissionsAmount` y `company` (CompanyInfo)
   - Nuevo modelo `CompanyInfo` con `id`, `companyName`, `paymentStatus`

2. **CompanyRepository.kt**
   - `PaymentResult.HistorySuccess`: Ahora incluye `pendingCommissionsAmount` y `companyName`
   - `PaymentAccessInfo`: Agregado campo `companyStatus`
   - `checkPaymentAccess()`: Actualizado para pasar companyStatus

3. **HomeViewModel.kt**
   - `HomeUiState`: Agregados campos `paymentCompanyStatus` y `paymentAccessMessage`
   - `checkPaymentAccess()`: Actualizado para guardar companyStatus y message
   - `canGenerateQR`: Corregido para verificar `empresaStatus?.lowercase() == "validated"`

4. **PaymentsViewModel.kt**
   - `loadPayments()`: Actualizado para usar `pendingCommissionsAmount` y `companyName` del historial

5. **ApiService.kt**
   - `registerCompanyPayment`: Cambiado endpoint de `admin/companies/payments/register` a `api/companies/my-company/payments/register`

### Lógica del Botón de Pagos
- El botón "Pagos" en HomeScreen ahora solo aparece cuando:
  1. El usuario tiene una empresa con `status == 'validated'`
  2. La empresa está activa (`is_active == True`)
  3. Hay pagos pendientes (`pending_amount > 0`)

---

## Configuración del Entorno

### Android SDK
- **Ruta**: `E:\android\SDK`
- **Build Tools**: Disponibles en `build-tools/`
- **Platforms**: Disponibles en `platforms/`

---

## Diseño Visual

### Estilo General
- **Forma de tarjetas**: Bordes redondeados (24dp)
- **Botones/campos**: Bordes redondeados (12dp)
- **Sombras**: Suaves en las tarjetas
- **Header**: Color sólido (no transparente)

### Colores del Tema

**Ubicación**:
- Compose: `ui/theme/Theme.kt`
- XML: `res/values/colors.xml`

#### Modo Claro (Light)
| Elemento | Color | Hex | Uso |
|----------|-------|-----|-----|
| Primary | Celeste | `#00AEEF` | Botones principales, destacados |
| Primary Variant | Azul oscuro | `#0090C7` | Estados hover, énfasis |
| Secondary | Verde azulado | `#03DAC6` | Acentos secundarios |
| Background | Blanco | `#FFFFFF` | Fondo de pantalla |
| Surface | Blanco | `#FFFFFF` | Fondo de tarjetas |
| Surface Variant | Gris muy claro | `#F5F5F5` | Campos de formulario |
| Error | Rojo | `#BA1A1A` | Mensajes de error |
| Outline | Gris medio | `#79747E` | Bordes, separadores |

#### Modo Oscuro (Dark)
| Elemento | Color | Hex | Uso |
|----------|-------|-----|-----|
| Primary | Celeste claro | `#6DD5FA` | Botones principales |
| Primary Container | Azul medio | `#0077B6` | Containers destacados |
| Secondary | Verde menta | `#4FFFD6` | Acentos secundarios |
| Background | Negro suave | `#121212` | Fondo de pantalla |
| Surface | Gris oscuro | `#1E1E1E` | Fondo de tarjetas |
| Surface Variant | Gris medio | `#2D2D2D` | Campos de formulario |
| Error | Coral claro | `#FFB4AB` | Mensajes de error |
| Outline | Gris medio | `#938F99` | Bordes, separadores |

---

## Funcionalidades Implementadas

### 1. Pantalla Login
- **Campo Teléfono**: 10 dígitos (incluye 0 inicial)
  - Usuario escribe: `0987654321`
  - Backend recibe: `+593987654321`
- **Campo Contraseña**: Mínimo 6 caracteres, toggle mostrar/ocultar
- **Validaciones en tiempo real**
- **Loading spinner** durante petición
- **Mensajes de error específicos**

### 2. Pantalla Registro (3 pasos)
**Paso 1 - Teléfono**: Enviar código SMS
**Paso 2 - Verificación**: Confirmar código SMS con 4 campos OTP individuales
- Auto-foco al siguiente campo al ingresar dígito
- Teclado numérico
- Auto-envío al completar 4 dígitos
- Resend con cooldown de 60 segundos

**Paso 3 - Datos Personales**:
- Nombres (solo letras, máx 50)
- Apellidos (solo letras, máx 50)
- Cédula/RUC (solo dígitos, máx 13)
- Email (validación formato)
- Password (mín 8 caracteres, mayúscula, minúscula, número)
- Confirmar Password
- Código de referido (opcional)
- Checkbox mayor de edad
- Checkbox política de privacidad

### 3. Navegación
- Login → Registro → Home
- Verificación de sesión activa al iniciar
- Flecha atrás en pantallas de registro

### 4. API Endpoints
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/auth/login` | Autenticación |
| POST | `/api/auth/verify-phone-number` | Enviar código SMS |
| POST | `/api/auth/confirm-phone-code` | Confirmar código |
| POST | `/api/auth/complete-registration` | Completar registro |

### 5. Manejo de Errores
- **3 intentos** para código SMS antes de bloquear
- **Rate limiting** después de 3 reenvíos (60 segundos espera)
- **Errores específicos**:
  - `RegisterError.PhoneAlreadyExists` (400)
  - `RegisterError.InvalidCode` (400)
  - `RegisterError.MaxAttemptsExceeded` (400, 0 intentos)
  - `RegisterError.RateLimited` (429)
  - `RegisterError.InvalidToken` (400)
  - `RegisterError.ValidationError` (422)
  - `RegisterError.ServerUnavailable` (network)
  - `RegisterError.Unknown` (otros)

---

## Archivos del Proyecto

```
app/src/main/java/com/christelldev/easyreferplus/
├── MainActivity.kt
├── data/
│   ├── model/
│   │   ├── LoginRequest.kt
│   │   ├── LoginResponse.kt
│   │   ├── VerifyPhoneRequest.kt
│   │   ├── VerifyPhoneResponse.kt
│   │   ├── ConfirmCodeRequest.kt
│   │   ├── ConfirmCodeResponse.kt
│   │   ├── CompleteRegistrationRequest.kt
│   │   ├── CompleteRegistrationResponse.kt
│   │   ├── HealthResponse.kt
│   │   ├── ReferralModels.kt
│   │   └── CompanyModels.kt
│   └── network/
│       ├── ApiService.kt
│       ├── AuthRepository.kt
│       ├── AuthInterceptor.kt
│       ├── RegisterApiService.kt
│       ├── RegisterRepository.kt
│       ├── RegisterResult.kt
│       ├── RegisterError.kt
│       ├── ReferralRepository.kt
│       ├── CompanyRepository.kt
│       ├── PasswordResetRepository.kt
│       ├── RetrofitClient.kt
│       └── ServerUnavailableInterceptor.kt
└── ui/
    ├── screens/
    │   ├── login/LoginScreen.kt
    │   ├── register/RegisterScreen.kt
    │   │   ├── PhoneStep - Paso teléfono
    │   │   ├── CodeStepContent - Paso código OTP
    │   │   ├── OtpDigitField - Campo dígito individual
    │   │   └── CompleteRegistrationStep - Datos personales
    │   ├── home/HomeScreen.kt
    │   ├── referrals/ReferralScreen.kt
    │   ├── companies/CompanyScreen.kt
    │   └── passwordreset/PasswordResetScreen.kt
    ├── theme/
    │   ├── Theme.kt
    │   └── Color.kt
    └── viewmodel/
        ├── LoginViewModel.kt
        ├── RegisterViewModel.kt
        ├── RegisterStep (enum: PHONE, CODE, COMPLETE)
        ├── PasswordResetViewModel.kt
        ├── ReferralViewModel.kt
        └── CompanyViewModel.kt
```

---

## Pendiente por Implementar
- [ ] Pantalla Perfil (complementaria)
- [ ] Pantalla Configuración
- [ ] Pantalla Invitar Amigos

---

## Correcciones Realizadas (2026-02-13) - Tercera Parte: Sistema OTP Mejorado

### Nuevos Endpoints
1. **POST `/api/auth/resend-phone-code`**
   - Nuevo endpoint para reenviar código SMS
   - Request: `{ "phone": "+593..." }`
   - Response: `{ "success": true, "message": "..." }`

2. **GET `/api/privacy/policy`**
   - Obtiene la política de privacidad actual
   - Response: `{ "success": true, "policy": { "id", "version", "title", "content", ... } }`

### RegisterRepository.kt
1. **Nuevos tipos de error**
   - `RegisterError.GlobalRateLimitExceeded` - Límite global alcanzado
   - `RegisterError.GlobalVerificationLimitExceeded` - Límite de verificaciones globales alcanzado
   - `RegisterError.MaxResendAttemptsExceeded` - Máximo de reenvíos alcanzado

2. **Nueva función parseResendError**
   - Maneja errores 429 y 400 del endpoint de reenvío
   - Detecta `error_type: "global_verification_limit_exceeded"`, `global_rate_limit_exceeded`, `too_many_resend_attempts`

3. **Función getPrivacyPolicy**
   - Obtiene título y contenido de la política de privacidad
   - Retorna `PrivacyPolicyResult.Success(title, content)` o `Error(message)`

### RegisterViewModel.kt
1. **Estados para reenvío de código**
   - `resendAttempts: Int = 0` - Intentos de reenvío realizados
   - `maxResendAttempts: Int = 3` - Máximo de intentos permitidos
   - `isResendEnabled: Boolean = true` - Habilita/deshabilita botón de reenvío
   - `waitSeconds: Int?` - Segundos de espera restantes

2. **Estados para política de privacidad**
   - `showPrivacyPolicyDialog: Boolean` - Muestra/oculta diálogo
   - `privacyPolicyTitle: String` - Título de la política
   - `privacyPolicyContent: String` - Contenido de la política
   - `isLoadingPrivacyPolicy: Boolean` - Loading al cargar política

3. **Función resendCode()**
   - Verifica máximo de intentos (3)
   - Verifica cooldown (60 segundos)
   - Llama al endpoint de reenvío
   - Incrementa contador de intentos
   - Muestra mensaje con intentos restantes

4. **Función onPrivacyPolicyCheckClick()**
   - Al hacer clic en checkbox, carga y muestra el modal de políticas

5. **Función loadPrivacyPolicy()**
   - Llama al endpoint para obtener política
   - Muestra diálogo con título y contenido

6. **Funciones de navegación**
   - `goToPhoneStep()` - Limpia todo y vuelve al paso 1
   - `goToPhoneStepWithMessage(message)` - Igual + mensaje de error

7. **Mensajes de error actualizados**
   - `getVerifyErrorMessage()` - Agregado GlobalRateLimitExceeded, GlobalVerificationLimitExceeded
   - `getResendErrorMessage()` - Nuevo, maneja MaxResendAttemptsExceeded, GlobalRateLimitExceeded, etc.
   - `getConfirmErrorMessage()` - Agregado GlobalRateLimitExceeded, GlobalVerificationLimitExceeded

### RegisterScreen.kt
1. **Modal de Política de Privacidad**
   - Agregado `PrivacyPolicyDialog` composable
   - Muestra título y contenido en AlertDialog
   - Scrollable para contenido largo
   - Botones "Cerrar" y "Aceptar"

2. **Checkbox de Privacidad**
   - `onPrivacyPolicyCheckClick` ahora llama a `loadPrivacyPolicy()` en lugar de toggle directo
   - Obliga al usuario a leer y aceptar la política

### RegisterApiService.kt
1. **Nuevo endpoint resendCode**
   - `@POST("api/auth/resend-phone-code")`

2. **Nuevo endpoint getPrivacyPolicy**
   - `@GET("api/privacy/policy")`

### RegisterRequest.kt
1. **Nuevos modelos**
   - `ResendCodeRequest(phone: String)`
   - `ResendCodeResponse(success: Boolean, message: String)`
   - `PrivacyPolicy(id, version, title, content, isCurrent, createdAt)`
   - `PrivacyPolicyResponse(success: Boolean, policy: PrivacyPolicy?)`

---

## Correcciones Realizadas (2026-02-13) - Segunda Parte

### RegisterRepository.kt
1. **Parsing de errores Pydantic/FastAPI**
   - Agregada clase `PydanticError` para parsear formato `detail` de FastAPI
   - Ahora extrae campo de `detail[0].loc` y mensaje de `detail[0].msg`
   - Remueve prefijo "Value error, " de los mensajes

2. **Formato de errors mixto (400)**
   - El campo `errors` puede ser `List<ValidationError>` o `List<String>`
   - Agregada lógica para detectar y parsear ambos formatos
   - Análisis de mensajes de strings para mapear al campo correcto:
     - "correo"/"email" → email
     - "cédula"/"ruc" → cedula_ruc
     - "referido"/"referral" → referral_code
     - "password" → password/confirm_password

### RegisterViewModel.kt
1. **Navegación automática al expirar token**
   - Agregada función `goToPhoneStep()` - Limpia todos los datos y vuelve al paso de teléfono
   - Agregada función `goToPhoneStepWithMessage()` - Igual + mensaje de error
   - Cuando el backend devuelve `InvalidToken`, navega automáticamente al paso 1

2. **Campo referralCodeError**
   - Agregado campo `referralCodeError` a `RegisterUiState`
   - Ahora se limpian estos errores al iniciar `completeRegistration()`

### RegisterScreen.kt
1. **Botón Crear Cuenta deshabilitado**
   - Ahora solo se habilita cuando: `!isLoading && isAdult && acceptsPrivacyPolicy`
   - Obliga al usuario a marcar ambos checkboxes

2. **Error de Código de Referido**
   - Agregado parámetro `referralCodeError` a `CompleteRegistrationStep`
   - Ahora muestra error en el campo cuando el backend lo devuelve

---

## Correcciones Realizadas (2026-02-13) - Primera Parte

### RegisterViewModel.kt
1. **Función updateApellidos** - Corregido uso de `copy()` sin parámetros nombrados
   - Antes: `copy(cleanValue, error)` asignaba a `phone` y `phoneError`
   - Después: `copy(apellidos = cleanValue, apellidosError = error)`

2. **Validación de apellidos en completeRegistration**
   - Antes: `validateApellidos("")` validaba con string vacío
   - Después: `validateApellidos(currentState.apellidos)` usa el valor real

3. **Errores del backend (422) ahora se muestran en campos específicos**
   - Modificado `RegisterError.ValidationError` para contener mapa de errores de campo
   - Modificado `parseRegistrationError` para extraer errores del campo del backend
   - El ViewModel ahora asigna errores a los campos correspondientes (nombres, apellidos, cedula_ruc, email, password, confirm_password)

4. **Formato de datos para backend**
   - Nombres: `trim().uppercase()`
   - Apellidos: `trim().uppercase()`
   - Email: `trim().lowercase()`

5. **Validación de Cédula/RUC simplificada**
   - Validación local solo verifica: no vacío, 10-13 dígitos
   - Validación completa (algoritmo Ecuador) ahora la hace el backend
   - Removidas funciones `isValidCedula()` e `isValidRuc()`

6. **Mensaje de error genérico mejorado**
   - Cuando hay errores de campo del backend, muestra "Verifique los campos marcados con error."

7. **Bug de validación duplicada**
   - Corregido: `validateApellidos()` se llamaba dos veces, ahora usa la variable ya calculada

### RegisterRepository.kt
1. **RegisterError.ValidationError** - Ahora acepta `fieldErrors: Map<String, String>`
2. **parseRegistrationError** - Extrae errores específicos del campo del backend (422)

---

## Correcciones Realizadas (2025-02-12)

### RegisterScreen.kt
1. **Función ErrorText** - Corregida sintaxis corrupta en línea 656
2. **Líneas duplicadas** - Eliminadas líneas duplicadas después de ErrorText
3. **Imports FocusRequester** - Corregido paquete: `androidx.compose.ui.focus`
4. **OTP Auto-foco** - Agregado FocusRequester para auto-paso al siguiente campo
5. **Teclado numérico** - Agregado `KeyboardType.NumberPassword` a OtpDigitField
6. **Campo Apellidos** - Habilitado (antes estaba `enabled = false`)
7. **Parámetros completos** - Agregado `apellidos` y `apellidosError` a CompleteRegistrationStep

### RegisterViewModel.kt
1. **Errores de compilación**:
   - `erroresError` renombrado a `apellidosError`
   - Campo `apellidos` agregado a RegisterUiState
   - `apellidos = currentState.apellidos` agregado a completeRegistration()
2. **Función updateApellidos** - Corregida para actualizar `apellidos` y `apellidosError`

### RegisterRepository.kt
1. **parseVerifyError** - Eliminado doble envoltura `RegisterResult.Error(error)`
   - Ya devuelve `RegisterResult` directamente

---

## Notas Importantes

1. **Formato Teléfono**: Usuario ingresa 10 dígitos → Backend recibe `+593` + 9 dígitos

2. **Errores HTTP Comunes**:
   - 400: Teléfono ya existe/datos inválidos
   - 401: Credenciales incorrectas
   - 403: Cuenta bloqueada
   - 429: Demasiados intentos
   - 5xx: Servidor no disponible

3. **Strings**: Usar `stringResource(R.string.key)` en Compose

4. **Flujo OTP**:
   - 4 campos individuales
   - Auto-foco al siguiente al escribir
   - Submit automático al completar 4 dígitos
   - Reenvío: 60 segundos cooldown

5. **Validaciones**:
   - Nombres/Apellidos: Solo letras, máx 50 caracteres
   - Cédula/RUC: Validación completa la hace el backend (10 o 13 dígitos)
   - Email: Formato válido
   - Password: Mín 8 chars, 1 mayúscula, 1 minúscula, 1 número

6. **Mensajes de Error de Cédula/RUC (Backend)**:
   - "Número de cédula es inválido" - Para cédula inválida
   - "Número de RUC es inválido" - Para RUC inválido
   - "Número de identificación es inválido" - Para otros casos

7. **Flujo de Token Expirado**:
   - Si el token de verificación expira, el app navega automáticamente al paso 1
   - Limpia todos los datos de registro (cédula, email, password, etc.)
   - Muestra mensaje: "La sesión expiró. Por favor, inicia el registro nuevamente."

8. **Formato de Errores del Backend**:
   - Error 422: Formato Pydantic `{"detail": [{"loc": [...], "msg": "..."}]}`
   - Error 400: Formato nuevo `{"errors": ["mensaje1", "mensaje2"]}`
   - Error 400: Formato anterior `{"errors": [{"field": "...", "message": "..."}]}`

9. **Flujo de Política de Privacidad**:
   - El usuario hace clic en el checkbox de "Acepto la política de privacidad"
   - Se abre un AlertDialog con el título y contenido de la política
   - El usuario debe leer el contenido y presionar "Aceptar" para continuar
   - El checkbox se marca automáticamente al aceptar
   - El botón "Crear Cuenta" solo se habilita cuando ambos checkboxes (edad y privacidad) están marcados

10. **Límite de Reenvíos de OTP**:
    - Máximo 3 intentos de reenvío por sesión
    - Después de cada reenvío, 60 segundos de espera
    - Al agotar los 3 intentos, debe reiniciar el proceso de registro

11. **Rate Limiting Global**:
    - `global_verification_limit_exceeded`: Límite de verificaciones globales alcanzado
    - `global_rate_limit_exceeded`: Límite de solicitudes globales alcanzado
    - Ambos muestran mensajes indicando esperar antes de intentar de nuevo

---

---

## Correcciones Realizadas (2026-02-13) - Cuarta Parte: Health Check y Contador

### Health Check del Servidor

Se implementó un sistema centralizado para verificar la disponibilidad del servidor antes de cada petición API.

### Nuevos Archivos
1. **HealthResponse.kt** - Modelo para respuesta del health check
2. **HealthCheckInterceptor.kt** - Interceptor que verifica `/health` antes de cada request

### HealthCheckInterceptor.kt
- Verifica endpoint `/health` antes de cada petición
- Cachea resultado por 30 segundos
- Lanza `IOException` cuando servidor no está disponible
- Evita recursión infinita usando cliente separado

### RegisterRepository.kt
1. **parseException** - Maneja `IOException` como `ServerUnavailable`
2. **parseConfirmError** - Maneja nuevo formato de error del backend con `error_type`
   - `error_type: "http_error"` → código incorrecto
   - `error_type: "too_many_attempts"` → demasiados intentos
   - Extrae intentos restantes del mensaje
3. **RateLimitError** - Ahora incluye mensaje del backend

### RegisterViewModel.kt
1. **Contador regresivo** - `startCountdown(seconds)` para cooldown
2. **Mensajes actualizados** - "En mantenimiento, intente más tarde" cuando servidor no disponible

### RegisterScreen.kt
1. **Contador visual** - Muestra "Reenviar en X segundos..."
2. **Contador con LaunchedEffect** - Decrementa cada segundo
3. **Botón siempre visible** - Se muestra cuando countdown llegó a 0

### AuthRepository.kt
1. **parseException** - Maneja `IOException` como `ServerUnavailable`

### ViewModels (Login y Register)
- Mensaje de error cambiado a "En mantenimiento, intente más tarde"

---

*Última actualización: 2026-02-14 (HomeScreen mejorado, Sesiones, Ganancias Admin)*

---

## Correcciones Realizadas (2026-02-14) - Quinta Parte: Pantalla Home, Menú y Logout

### HomeScreen.kt
1. **Rediseño completo** con tema azul (#00AEEF)
2. **Sección superior redondeada** con stats
3. **ModalNavigationDrawer** con menú lateral
4. **Menú items**: Mis Referidos, Editar Perfil, Invitar Amigos, Configuración, Cerrar Sesión
5. **Tarjetas de estadísticas**: Ganancias, Referidos, Clicks
6. **Sección de referidos** con lista de referidos
7. **Código de referido** con botón copiar
8. **Diálogo de confirmación** para logout

### MainActivity.kt
1. **Navegación** hacia ReferralScreen desde menú
2. **Callback onLogoutComplete** que reinicia MainActivity

### Logout
- Confirmación con AlertDialog
- Limpia tokens usando `authRepository.clearTokens()`
- Reinicia actividad después de logout

---

## Correcciones Realizadas (2026-02-14) - Sexta Parte: Pantalla Mis Referidos

### Endpoints Implementados
1. **GET `/api/auth/my-referrals`** - Obtiene árbol de referidos
2. **GET `/api/auth/search-referral?code=XXX`** - Busca código de referido

### Nuevos Archivos
1. **ReferralModels.kt** - Modelos para respuestas
2. **ReferralRepository.kt** - Repository para referidos
3. **ReferralViewModel.kt** - ViewModel con estado
4. **ReferralScreen.kt** - UI de referidos

### ReferralScreen.kt
1. **Tu código de referido** - Card azul con código
2. **Long-press para copiar** - Usa combinedClickable
3. **Estadísticas por nivel** - Nivel 1, 2, 3 y total
4. **Búsqueda de referidos** - Campo de búsqueda
5. **Árbol de referidos** - Lista por niveles
6. **Mensaje cuando no hay referidos**

### ApiService.kt
- Agregados endpoints de referidos

### ViewModel
- Carga datos al iniciar
- Busca referidos por código
- Muestra errores de validación

---

## Correcciones Realizadas (2026-02-14) - Séptima Parte: Registro de Empresa

### Endpoints Implementados
1. **POST `/api/companies/register`** - Registra empresa
2. **GET `/api/cities/ecuador/provinces`** - Lista provincias
3. **GET `/api/cities/ecuador/province-cities/{province}`** - Ciudades por provincia

### Nuevos Archivos
1. **CompanyModels.kt** - Modelos de request/response
2. **CompanyRepository.kt** - Repository para empresa
3. **CompanyViewModel.kt** - ViewModel con estado
4. **CompanyScreen.kt** - UI de registro de empresa

### CompanyScreen.kt
1. **Sección Información de Empresa**
   - Nombre (requerido, máx 200 chars)
   - Descripción (requerido, máx 500 chars)
   - Descripción de productos (requerido, mín 10, máx 500)

2. **Sección Ubicación**
   - Dirección (requerido)
   - Dropdown Provincia (lista de 24 provincias de Ecuador)
   - Dropdown Ciudad (carga automática al seleccionar provincia)

3. **Sección Comisiones**
   - Porcentaje (1-50%)
   - Día de pago (15 o 28-31)

4. **Sección Adicional (Opcional)**
   - Website
   - Facebook URL
   - Instagram URL
   - WhatsApp (formato +593)

5. **Validaciones**
   - Campo por campo
   - Errores del servidor mostrados en cada campo
   - Validación local: comisión 1-50%, payment_day válido

### Validaciones Actualizadas
- `company_name`: 1-200 caracteres
- `company_description`: 1-500 caracteres
- `product_description`: 10-500 caracteres
- `address`: 1-500 caracteres
- `commission_percentage`: 1.00-50.00
- `payment_day`: 15 o 28-31
- `whatsapp_number`: formato ecuatoriano (+593)

---

## Correcciones Realizadas (2026-02-14) - Octava Parte: Refresh Token Automático

### Problema Original
- Token expiraba y no se refrescaba automáticamente
- App crasheaba al intentar hacer requests con token vencido

### Solución Implementada

### AuthInterceptor.kt
1. **Intercepta todas las peticiones** que requieren auth
2. **Agrega Bearer token** automáticamente
3. **Maneja 401 Unauthorized**
   - Intenta refresh de token
   - Reintenta request con nuevo token
4. **No agrega token** a endpoints públicos (login, register, cities, etc.)
5. **Manejo de errores** para evitar crashes

### RetrofitClient.kt
1. **Logging interceptor seguro** - No falla cuando el body está cerrado
2. **setAuthRepository()** - Configura el interceptor de auth

### AuthRepository.kt
1. **refreshToken()** - Método para refrescar token
2. **saveTokensFromRefresh()** - Guarda nuevos tokens
3. **Manejo de errores** cuando refresh falla

### Modelos Actualizados
1. **RefreshTokenRequest/Response** en LoginResponse.kt
2. **ApiService** tiene endpoint `POST /api/auth/refresh`

### Endpoints que No Requieren Token
- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/refresh`
- `/password-reset`
- `/cities/`

---

## Pendiente por Implementar
- [ ] Pantalla Perfil
- [ ] Pantalla Configuración
- [ ] Pantalla Invitar Amigos

---

## Correcciones Realizadas (2026-02-14) - Novena Parte: Sistema de Códigos QR

### Endpoints Implementados

1. **POST `/api/qr-transactions/generate`** - Generar código QR de pago
   - Request: company_id, amount, currency, description, referral_code (opcional)
   - Response: qr_transaction, qr_image_url, qr_data

2. **POST `/api/qr-transactions/scan`** - Escanear código QR
   - Request: qr_code, qr_secret
   - Response: qr_transaction con datos de la empresa

3. **POST `/api/qr-transactions/process`** - Procesar transacción QR
   - Request: qr_code, qr_secret, buyer_document, buyer_name, buyer_phone, buyer_email, payment_method
   - Response: transaction, qr_transaction, referral_distribution

4. **GET `/api/qr-transactions/status/{qr_code}`** - Obtener estado de QR
5. **GET `/api/qr-transactions/my-generated`** - Lista de QR generados
6. **GET `/api/qr-transactions/my-scanned`** - Lista de QR escaneados
7. **GET `/api/companies/my-companies`** - Obtener empresas del usuario

### Nuevos Archivos

1. **QRModels.kt** - Modelos de request/response
   - GenerateQRRequest, GenerateQRResponse
   - ScanQRRequest, ScanQRResponse
   - ProcessQRRequest, ProcessQRResponse
   - QRTransaction, QRCompany, ProcessedTransaction
   - ReferralDistribution, QRPagination
   - UserCompanyResponse, UserCompaniesResponse

2. **QRRepository.kt** - Repository para QR transactions
   - generateQR(), scanQR(), processQR()
   - getQRStatus(), getMyGeneratedQRs(), getMyScannedQRs()

3. **QRViewModel.kt** - ViewModel con estado
   - Estados para generar, escanear y procesar QR
   - Validaciones de campos
   - Manejo de errores

4. **QRScreen.kt** - UI de pagos QR
   - Pestañas para Generar/Escanear QR
   - Formulario de generación con dropdown de empresas
   - Formulario de escaneo con entrada manual
   - Formulario de datos del comprador
   - Resultado de transacción

### ApiService.kt Actualizado
- Agregados endpoints de QR transactions
- Agregado endpoint getMyCompanies

### CompanyRepository Actualizado
- Agregado método getMyCompanies()

### HomeScreen Actualizado
- Agregado item de menú para Pagos QR
- Navegación a pantalla QR

### MainActivity Actualizado
- Agregada ruta QR
- Agregado QRViewModel
- Navegación a QRScreen

### Flujo de Uso

**Para generar QR (usuarios con empresa):**
1. Seleccionar empresa del dropdown
2. Ingresar monto y descripción
3. Opcional: agregar código de referido
4. Generar QR
5. Copiar código QR

**Para escanear QR (todos los usuarios):**
1. Escanear código QR con la cámara
2. Verificar datos del QR
3. Ingresar datos del comprador
4. Confirmar pago

### QR Scanner (Implementado 2026-02-14)
- Cámara usando CameraX
- Detección de QR con ML Kit Barcode
- Overlay con marco de escaneo moderno
- Botón de flash para iluminar
- Manejo de permisos de cámara
- Parseo de formato easyrefer://pay?code=XXX&secret=YYY

### Dependencias Agregadas
- CameraX (1.4.0)
- ML Kit Barcode Scanning (17.3.0)
- Accompanist Permissions (0.34.0)

### Pendiente
- Carga automática de empresas del usuario
- Historial de transacciones QR

---

## Correcciones Realizadas (2026-02-14) - Décima Parte: HomeScreen Mejorado, Sesiones y Ganancias Admin

### HomeScreen.kt - Rediseño Completo

1. **Diseño Visual**
   - Tarjetas simétricas con sombras (CardDefaults.cardElevation)
   - Soporte para modo oscuro/claro
   - Tipografía consistente
   - Bordes redondeados (24dp)

2. **Nueva Sección QR en Home**
   - Botón para generar QR ubicado al inicio del Home
   - **Solo visible** cuando la empresa está validada y activa
   - Lógica: `hasCompany && (empresaActiva == true || empresaStatus == "approved")`

3. **Reordenamiento de Secciones**
   - Quick Actions (horizontal scroll)
   - Código de Referido
   - Estadísticas (Ganancias, Referidos, Clicks)
   - Balance Disponible

4. **Quick Actions**
   - Implementado con LazyRow (scroll horizontal)
   - Acciones: Mis Referidos, Mi Empresa, Pagos QR, Perfil
   - Iconos con etiquetas

### Logout - Limpieza de Caché

1. **AuthRepository.kt**
   - Nuevo método `clearAllData()` que limpia SharedPreferences completo
   - Método `clearTokens()` renombrado internamente

2. **Logout Flow**
   - Confirmación con AlertDialog
   - Limpia tokens y datos de caché
   - Reinicia MainActivity

### Sesiones - SessionManagementScreen

1. **Endpoints Implementados**
   - GET `/api/auth/sessions` - Lista de sesiones activas
   - DELETE `/api/auth/sessions/{sessionId}` - Revocar sesión específica
   - DELETE `/api/auth/sessions` - Revocar todas las sesiones

2. **Nuevos Archivos**
   - `SessionModels.kt` - Modelos de sesión
   - `SessionRepository.kt` - Repository para sesiones
   - `SessionViewModel.kt` - ViewModel con estado
   - `SessionManagementScreen.kt` - UI de gestión de sesiones

3. **SessionManagementScreen.kt**
   - Lista de sesiones activas con dispositivo, ubicación, última actividad
   - Indicador de sesión actual
   - Botón para revocar sesión específica
   - Botón para revocar todas las sesiones
   - Diálogos de confirmación
   - Pull-to-refresh
   - Estados: loading, empty, error, success

### Admin Earnings - Ganancias Admin

1. **Endpoints Implementados**
   - GET `/api/admin/earnings?search=&page=&per_page=` - Lista de ganancias
   - Request params: search, page, per_page
   - Response: paginated list con totales por nivel

2. **Nuevos Archivos**
   - `AdminEarningsModels.kt` - Modelos de ganancias admin
   - `AdminEarningsRepository.kt` - Repository para ganancias
   - `AdminEarningsViewModel.kt` - ViewModel con paginación
   - `AdminEarningsScreen.kt` - UI de ganancias admin

3. **AdminEarningsScreen.kt**
   - Resumen de ganancias (Total, Pagadas, Pendientes)
   - Estadísticas por nivel (Nivel 1, 2, 3)
   - Campo de búsqueda por usuario, teléfono o código
   - Tabla paginada con registros
   - Paginación con Previous/Next
   - Estados: loading, empty, error, success
   - Pull-to-refresh

### Menú - Opción Admin Solo para Admin

1. **UserProfile.kt**
   - Agregado campo `role: String?`
   - Agregado `isAdmin: Boolean` computed property (role == "admin")

2. **ProfileResponse.kt**
   - Agregado campo `role: String?`
   - Agregado `isAdmin: Boolean` computed property

3. **HomeViewModel.kt**
   - Agregado campo `isAdmin: Boolean` al estado

4. **HomeScreen.kt**
   - Item de menú "Ganancias" solo visible para `isAdmin == true`
   - Navegación a AdminEarningsScreen

### String Resources Agregadas
- `admin_earnings` - Ganancias
- `search_by_user` - Buscar por usuario
- `no_earnings` - Sin ganancias
- `no_earnings_description` - Descripción vacío
- `paid_earnings` - Pagadas
- `paid` - Pagado
- `total_earnings` - Total Ganancias
- `search` - Buscar
- `filter` - Filtrar
- `page_info` - Info de página
- `previous` - Anterior
- `next` - Siguiente

### Pendiente
- [ ] Pantalla Perfil (complementaria)
- [ ] Pantalla Configuración
- [ ] Pantalla Invitar Amigos

---

## Correcciones Realizadas (2026-02-14) - Onceava Parte: Corrección Endpoints de Sesiones

### Problema Identificado
Los endpoints de sesiones en el app no coincidían con los del backend, causando errores "No autorizado".

### Endpoints Corregidos (Backend)

| Método | Endpoint Anterior (App) | Endpoint Correcto (Backend) |
|--------|-------------------------|------------------------------|
| GET | `/api/auth/sessions` | `/api/auth/sessions/list` |
| POST | `/api/auth/sessions/revoke` | `/api/auth/sessions/invalidate-session` |
| POST | `/api/auth/sessions/revoke-all` | `/api/auth/sessions/logout-all-except-current` |

### Archivos Modificados

1. **ApiService.kt**
   - `getSessions` → `GET /api/auth/sessions/list`
   - `revokeSession` → `POST /api/auth/sessions/invalidate-session`
   - `revokeAllSessions` → `POST /api/auth/sessions/logout-all-except-current`
   - Nuevo: `forceLogoutAll` → `POST /api/auth/sessions/force-logout-all`

2. **SessionModels.kt**
   - `SessionInfo`: Actualizado para usar `sessionId`, `createdAt` (Long timestamp), `lastUsed` (Long), `ipAddress`, `deviceInfo`, `userAgent`, `isCurrent`
   - `SessionsResponse`: Ahora incluye `success`, `message`, `totalSessions`, `maxSessions`, `sessions[]`
   - `InvalidateSessionRequest`: `{session_id: string}`
   - `InvalidateSessionResponse`: `{success, message, session_id}`
   - `LogoutAllResponse`: `{success, message, sessions_closed}`

3. **SessionRepository.kt**
   - Nuevos métodos: `invalidateSession()`, `logoutAllExceptCurrent()`, `forceLogoutAll()`
   - Nuevos tipos de resultado: `InvalidateResult`, `LogoutAllResult`

4. **SessionViewModel.kt**
   - Actualizado para usar nuevos métodos del repository
   - Agregado `requireReLogin` para manejar cierre de sesión actual

5. **SessionManagementScreen.kt**
   - Actualizado para usar campos correctos: `session.sessionId`, `session.deviceInfo`, `session.isCurrent`
   - timestamps convertidos a fecha legible usando `Date(timestamp * 1000)`

### Admin Earnings - CORREGIDO

**SOLUCIÓN IMPLEMENTADA**: Se crearon nuevos endpoints JSON en el backend que aceptan JWT Bearer token:

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/admin/earnings` | Lista de ganancias (paginado) |
| GET | `/api/admin/earnings/user/{user_id}` | Ganancias de usuario |
| GET | `/api/admin/earnings/platform` | Ganancias de plataforma |

**Autenticación**: JWT Bearer token (usa `get_current_admin_user` middleware)

**El panel web (`/admin/*`) sigue funcionando** con cookies - no hay cambios visibles.

### Archivos Modificados en Backend
1. **earnings_json.py** (NUEVO) - Endpoints JSON para mobile
2. **main.py** - Registrado el nuevo router

### Pendiente
- [ ] Pantalla Perfil (complementaria)
- [ ] Pantalla Configuración
- [ ] Pantalla Invitar Amigos

### RECEIPT INTEGRATION (COMPLETADO)

El flujo QR ahora muestra el Receipt después de procesar el pago:

1. **QRScreen.kt**
   - Agregado check para mostrar ReceiptScreen cuando hay receipt en el estado
   - Import agregado: ReceiptScreen, ProcessReceiptResponse

2. **QRViewModel.kt**
   - Agregado campo `receipt: ProcessReceiptResponse?` al estado
   - Actualizado `processQR()` para almacenar el receipt
   - Nuevo método `dismissReceipt()` para limpiar después de mostrar

3. **QRRepository.kt**
   - Actualizado para mapear la nueva respuesta ProcessReceiptResponse
   - Guardado del receipt en QRResult.ProcessSuccess

4. **QRModels.kt**
   - Actualizado `ScanQRResponse` para nuevos campos del backend
   - Agregado `ProcessReceiptResponse` para el receipt

5. **ApiService.kt**
   - Cambiado retorno de `processQR` a `ProcessReceiptResponse`

**Flujo:**
1. User escanea QR → /scan valida
2. User confirma pago → /process procesa y retorna receipt
3. App muestra ReceiptScreen automáticamente
4. User puede compartir o finalizar

---

## Décima Segunda Parte: Historial de Transacciones QR (2026-02-14)

### Backend - Cambios Realizados

1. **Modelo QRTransaction (models/qr_transaction.py)**
   - Agregado `transaction_id` (UUID único para receipts)
   - Agregado `transaction_type` ("sale" | "purchase")
   - Agregado `seller_user_id` (vendedor)
   - Agregado buyer info: `buyer_user_id`, `buyer_document`, `buyer_name`, `buyer_phone`, `buyer_email`, `payment_method`

2. **Schemas (schemas/qr_transaction.py)**
   - Actualizado `QRScanResponse` - ahora solo devuelve info para validar
   - Nuevo `ProcessQRRequest` - datos del comprador
   - Nuevo `ProcessQRResponse` - receipt completo con transaction_id único

3. **API Endpoints (api/qr_transactions.py)**
   - `POST /scan` - Ahora solo valida, no procesa
   - `POST /process` - **NUEVO** - Procesa el pago y genera receipt
   - `GET /history` - **NUEVO** - Historial de ventas/compras
   - `GET /receipt/{transaction_id}` - **NUEVO** - Receipt individual

### Android App - Cambios Realizados

1. **Modelos QR (QRModels.kt)**
   - `QRScanInfo` - info de QR escaneado
   - `ProcessReceiptResponse` - receipt de transacción
   - `TransactionHistoryItem` - item de historial
   - `HistoryPagination`, `HistoryStats`, etc.

2. **ApiService.kt**
   - Agregados: `getTransactionHistory`, `getTransactionReceipt`

3. **QRRepository.kt**
   - Agregado `HistoryResult` sealed class
   - Agregado método `getHistory()`

4. **Nuevas Pantallas**
   - `ReceiptScreen.kt` - Receipt elegante después del pago
     - Muestra detalles de transacción (empresa, comprador/vendedor, monto, método de pago)
     - Botón para compartir receipt via Intent (WhatsApp, SMS, email, etc.)
   - `HistoryScreen.kt` - Historial con tabs (Todo/Ventas/Compras)

5. **ViewModel**
   - `HistoryViewModel.kt` - Gestiona estado del historial

6. **Navegación**
   - Agregada ruta `Screen.History`
   - Agregado botón "Historial" en Quick Actions del Home
   - Agregada navegación desde Home

---

## Actualización (2026-02-18) - Sistema de Categorías y Servicios

### Nuevas Tablas a Crear (Producción)

```sql
-- Tabla de categorías
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

-- Tabla de servicios
CREATE TABLE services (
    id SERIAL PRIMARY KEY,
    category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE,
    UNIQUE(category_id, name)
);

-- Agregar campos a tabla companies (si no existen)
ALTER TABLE companies ADD COLUMN IF NOT EXISTS category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS service_id INTEGER REFERENCES services(id) ON DELETE SET NULL;

-- Índices para mejor rendimiento
CREATE INDEX IF NOT EXISTS idx_companies_category ON companies(category_id);
CREATE INDEX IF NOT EXISTS idx_companies_service ON companies(service_id);
CREATE INDEX IF NOT EXISTS idx_services_category ON services(category_id);
```

### Archivos Creados/Modificados

**Backend (EasyReferPython):**
- `models/category.py` - Modelo Category
- `models/service.py` - Modelo Service
- `models/company.py` - Agregados campos category_id y service_id
- `models/__init__.py` - Imports de Category y Service
- `schemas/company.py` - Agregados campos de categoría/servicio
- `services/company_service.py` - Filtros de categoría/servicio
- `api/admin/categories.py` - API CRUD de categorías
- `api/admin/services.py` - API CRUD de servicios
- `api/categories_services.py` - API pública para app móvil
- `api/companies/companies.py` - Filtros en búsqueda de empresas
- `api/admin/admin_routes.py` - Rutas HTML para categorías y servicios
- `templates/admin/categories.html` - Página de admin categorías
- `templates/admin/services.html` - Página de admin servicios
- `templates/admin/base.html` - Menú actualizado
- `main.py` - Nuevos routers registrados

### Uso

1. Acceder a `/admin/categories` para crear categorías
2. Acceder a `/admin/services` para crear servicios (asociados a categorías)
3. Las empresas ahora pueden seleccionar categoría y servicio al crearse
4. El filtro de empresas públicas incluye: Provincia, Ciudad, Categoría, Servicio

### Endpoints API

- `GET /api/categories-services/` - Obtener todas las categorías y servicios
- `GET /api/categories-services/categories` - Lista de categorías activas
- `GET /api/categories-services/services?category_id=X` - Lista de servicios (opcional por categoría)
- `GET /api/companies/?category_id=X&service_id=Y` - Filtrar empresas
- `GET /api/admin/categories` - Admin: lista categorías
- `POST /api/admin/categories` - Admin: crear categoría
- `PUT /api/admin/categories/{id}` - Admin: actualizar categoría
- `DELETE /api/admin/categories/{id}` - Admin: eliminar categoría
- `GET /api/admin/services` - Admin: lista servicios
- `POST /api/admin/services` - Admin: crear servicio
- `PUT /api/admin/services/{id}` - Admin: actualizar servicio
- `DELETE /api/admin/services/{id}` - Admin: eliminar servicio

---

## Actualización (2026-03-04) - Sistema de Carrito de Compras

### Pantallas Agregadas

1. **CartScreen.kt** (`ui/screens/cart/CartScreen.kt`)
   - Lista de productos en el carrito
   - Selector de cantidad (+/-)
   - Eliminar items
   - Vaciar carrito
   - Checkout con código de compra

### Modelos Agregados (data/model/)

1. **CartItem.kt**
   - id: Int
   - productId: Int
   - productName: String
   - productDescription: String
   - quantity: Int
   - price: Double
   - offerPrice: Double?
   - currentPrice: Double
   - companyId: Int
   - companyName: String
   - images: List<ProductImage>
   - subtotal: Double

### Repository

1. **CartRepository.kt** (NUEVO)
   - getCart(): Obtener items del carrito
   - addToCart(productId, quantity): Agregar producto
   - updateQuantity(productId, quantity): Actualizar cantidad
   - removeFromCart(productId): Eliminar item
   - clearCart(): Vaciar carrito

### ViewModel

1. **CartViewModel.kt** (NUEVO)
   - Estados: cartItems, isLoading, total, itemCount
   - Funciones: loadCart, addItem, removeItem, updateQuantity, clearCart

---

## Actualización (2026-03-04) - Sistema de Ganancias de Usuario

### Pantallas Agregadas

1. **EarningsScreen.kt** (`ui/screens/earnings/EarningsScreen.kt`)
   - Resumen de ganancias (Total, Pagado, Pendiente)
   - Estadísticas por nivel (L1, L2, L3)
   - Top empresas por comisiones
   - Lista de comisiones con filtros
   - Paginación infinita
   - Pull-to-refresh

### Modelos

1. **EarningsSummary.kt**
   - totalEarned: Double
   - totalPaid: Double
   - totalPending: Double
   - totalCommissions: Int
   - pendingCount: Int
   - paidCount: Int
   - scheduledCount: Int
   - level1Earnings: Double
   - level2Earnings: Double
   - level3Earnings: Double

2. **CommissionResponse.kt**
   - id, userId, companyId, amount, status, level, createdAt, companyName

### Repository

1. **EarningsRepository.kt** (NUEVO)
   - getEarningsSummary(): Obtener resumen de ganancias
   - getCommissions(page, filter): Obtener lista de comisiones

### ViewModel

1. **EarningsViewModel.kt** (NUEVO)
   - Estados: summary, commissions, isLoading, selectedFilter
   - Funciones: loadSummary, loadCommissions, filterBy, loadMore

---

## Actualización (2026-03-04) - Sistema de Retiros

### Pantallas Agregadas

1. **WithdrawalScreen.kt** (`ui/screens/withdrawal/WithdrawalScreen.kt`)
   - Balance disponible para retiro
   - Lista de cuentas bancarias
   - Agregar nueva cuenta bancaria
   - Seleccionar cuenta para retiro
   - Solicitar retiro
   - Historial de solicitudes
   - Estados: pending, approved, rejected, completed

### Modelos

1. **WithdrawalModels.kt**
   - BankAccountResponse: id, bankName, accountType, accountNumber, accountHolderName, isDefault
   - WithdrawalRequestResponse: id, amount, status, createdAt, processedAt, bankAccount
   - WithdrawalBalanceResponse: availableBalance, totalEarned, totalWithdrawn, pendingWithdrawal

### Repository

1. **WithdrawalRepository.kt** (NUEVO)
   - getBalance(): Obtener balance
   - getBankAccounts(): Obtener cuentas
   - createBankAccount(): Crear cuenta
   - deleteBankAccount(): Eliminar cuenta
   - requestWithdrawal(): Solicitar retiro
   - getHistory(): Historial de solicitudes

### ViewModel

1. **WithdrawalViewModel.kt** (NUEVO)
   - Estados: balance, accounts, requests, isLoading, successMessage, errorMessage
   - Funciones: loadBalance, loadAccounts, createAccount, requestWithdrawal

---

## Actualización (2026-03-04) - Detalle de Transacción

### Pantallas Agregadas

1. **TransactionDetailScreen.kt** (`ui/screens/history/TransactionDetailScreen.kt`)
   - Detalles completos de la transacción QR
   - Información del vendedor/comprador
   - Monto y método de pago
   - Fecha y estado
   - Comisión generada
   - Compartir receipt

### ViewModel

1. **TransactionDetailViewModel.kt** (NUEVO)
   - Estados: transaction, isLoading, error
   - Funciones: loadTransaction

---

## Actualización (2026-03-04) - Retiros Admin

### Pantallas Agregadas

1. **AdminWithdrawalsScreen.kt** (`ui/screens/admin/AdminWithdrawalsScreen.kt`)
   - Lista de solicitudes de retiro
   - Filtros por estado
   - Buscar por usuario
   - Aprobar/Rechazar retiros
   - Marcar como pagado
   - Detalle de retiro

### ViewModel

1. **AdminWithdrawalsViewModel.kt** (NUEVO)
   - Estados: withdrawals, isLoading, selectedFilter
   - Funciones: loadWithdrawals, approveWithdrawal, rejectWithdrawal, markAsPaid

---

## Actualización (2026-03-04) - Pantalla de Mantenimiento

### Pantallas Agregadas

1. **MaintenanceScreen.kt** (`ui/screens/maintenance/MaintenanceScreen.kt`)
   - Pantalla mostrada cuando el servidor no está disponible
   - Icono de "Cloud Off"
   - Mensaje de mantenimiento
   - Botón de reintentar
   - Auto-verificación de conectividad

### NetworkMonitor

1. **NetworkMonitor.kt** (`data/network/NetworkMonitor.kt`)
   - isServerAvailable(): Verificar si el servidor responde
   - Usa endpoint /health

---

## Actualización (2026-03-04) - Detalle de Empresa

### Pantallas Agregadas

1. **CompanyDetailScreen.kt** (`ui/screens/companies/CompanyDetailScreen.kt`)
   - Información completa de la empresa
   - Logo de la empresa
   - Descripción y productos
   - Porcentaje de comisión
   - Día de pago
   - Botón para navegar a productos
   - Estado de la empresa

### ViewModel

1. **CompanyDetailViewModel.kt** (NUEVO)
   - Estados: company, isLoading, error
   - Funciones: loadCompany

---

## Actualización (2026-03-04) - Empresas Públicas

### Pantallas Agregadas

1. **PublicCompaniesScreen.kt** (`ui/screens/companies/PublicCompaniesScreen.kt`)
   - Lista de empresas disponibles
   - Filt Provincia, Ciudad, Categoría, Servicio
   - Busros:car por nombre
   - Cards con información de empresa
   - Navegación a detalle

### ViewModel

1. **PublicCompaniesViewModel.kt** (NUEVO)
   - Estados: companies, isLoading, filters
   - Funciones: loadCompanies, applyFilters, search

---

## Actualización (2026-03-04) - Lista de Empresas

### Pantallas Agregadas

1. **CompaniesListScreen.kt** (`ui/screens/companies/CompaniesListScreen.kt`)
   - Lista de empresas del usuario
   - Estado de cada empresa
   - Editar/Eliminar empresa

---

## Resumen de Archivos del Proyecto Android

```
EasyReferPlus/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/christelldev/easyreferplus/
│       │   ├── MainActivity.kt
│       │   ├── data/
│       │   │   ├── model/
│       │   │   │   ├── LoginRequest.kt
│       │   │   │   ├── LoginResponse.kt
│       │   │   │   ├── RegisterRequest.kt
│       │   │   │   ├── ReferralModels.kt
│       │   │   │   ├── CompanyModels.kt
│       │   │   │   ├── QRModels.kt
│       │   │   │   ├── SessionModels.kt
│       │   │   │   ├── CategoryServiceModels.kt
│       │   │   │   ├── ProductModels.kt
│       │   │   │   ├── WithdrawalModels.kt
│       │   │   │   ├── EarningsSummary.kt
│       │   │   │   └── ...
│       │   │   └── network/
│       │   │       ├── ApiService.kt
│       │   │       ├── AuthRepository.kt
│       │   │       ├── AuthInterceptor.kt
│       │   │       ├── ReferralRepository.kt
│       │   │       ├── CompanyRepository.kt
│       │   │       ├── QRRepository.kt
│       │   │       ├── SessionRepository.kt
│       │   │       ├── ProductRepository.kt
│       │   │       ├── WithdrawalRepository.kt
│       │   │       ├── EarningsRepository.kt
│       │   │       ├── CartRepository.kt
│       │   │       ├── ProfileRepository.kt
│       │   │       ├── RetrofitClient.kt
│       │   │       ├── HealthCheckInterceptor.kt
│       │   │       ├── NetworkMonitor.kt
│       │   │       └── WebSocketManager.kt
│       │   └── ui/
│       │       ├── screens/
│       │       │   ├── login/
│       │       │   ├── register/
│       │       │   ├── home/
│       │       │   ├── referrals/
│       │       │   ├── companies/
│       │       │   ├── products/
│       │       │   ├── qr/
│       │       │   ├── history/
│       │       │   ├── sessions/
│       │       │   ├── earnings/
│       │       │   ├── withdrawal/
│       │       │   ├── cart/
│       │       │   ├── profile/
│       │       │   ├── passwordreset/
│       │       │   ├── admin/
│       │       │   └── maintenance/
│       │       ├── viewmodel/
│       │       └── theme/
│       └── res/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Lista Completa de ViewModels

| ViewModel | Descripción |
|-----------|-------------|
| LoginViewModel | Estado del login |
| RegisterViewModel | Estado del registro (3 pasos) |
| HomeViewModel | Dashboard principal |
| ReferralViewModel | Árbol de referidos |
| CompanyViewModel | Registro de empresa |
| PublicCompaniesViewModel | Empresas públicas |
| CompanyDetailViewModel | Detalle de empresa |
| QRViewModel | Generación y escaneo QR |
| HistoryViewModel | Historial de transacciones |
| TransactionDetailViewModel | Detalle de transacción |
| SessionViewModel | Gestión de sesiones |
| EarningsViewModel | Ganancias del usuario |
| WithdrawalViewModel | Retiros |
| CartViewModel | Carrito de compras |
| ProductViewModel | Productos |
| ProfileViewModel | Perfil de usuario |
| PasswordResetViewModel | Recuperar contraseña |
| AdminEarningsViewModel | Ganancias admin |
| AdminWithdrawalsViewModel | Retiros admin |

---

## Lista Completa de Repositories

| Repository | Descripción |
|-----------|-------------|
| AuthRepository | Autenticación |
| RegisterRepository | Registro |
| ReferralRepository | Referidos |
| CompanyRepository | Empresas |
| QRRepository | Transacciones QR |
| SessionRepository | Sesiones |
| ProductRepository | Productos |
| WithdrawalRepository | Retiros |
| EarningsRepository | Ganancias |
| CartRepository | Carrito |
| ProfileRepository | Perfil |
| PasswordResetRepository | Recuperar contraseña |
| AdminEarningsRepository | Ganancias admin |
| AdminWithdrawalsRepository | Retiros admin |

---

## Configuración de la App

### Dependencias Principales (build.gradle.kts)
- Jetpack Compose
- Retrofit + OkHttp
- CameraX + ML Kit Barcode
- Coil (imágenes)
- ZXing (generación QR)
- Navigation Compose
- Material3

### API Base URL
- Desarrollo: `http://10.0.2.2:8971/` (Android Emulator)
- Producción: `https://api.easyreferplus.com/`

---

