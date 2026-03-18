# BITÁCORA - EasyRefer App Kotlin

## 📋 Información del Proyecto
- **Nombre:** EasyRefer App Kotlin
- **Tech Stack:** Kotlin, Jetpack Compose, Android SDK
- **Repositorio:** https://github.com/megaelemento/EasyReferAppKotlin
- **Fecha de inicio:** 2026-03-04
- **Estado:** En desarrollo activo

---

## 📝 Historial de Cambios

### 2026-03-12 - Fix Porcentajes de Comisiones
**Objetivo:** Sincronizar porcentajes de comisiones entre panel admin y app Kotlin.

**Problema identificado:**
- Panel Admin: 20%-20%-20%-40% (configurable desde BD)
- App Kotlin: Hardcoded 40%-30%-20% (incorrecto)

**Solución implementada:**
1. Backend: Modificado endpoint `/api/companies/transactions/my-earnings/summary` para devolver `commission_percentages`
2. App Kotlin: 
   - Agregado modelo `CommissionPercentages` en `EarningsSummary.kt`
   - Modificado `EarningsUiState` para guardar porcentajes
   - Actualizado `EarningsScreen` para usar valores dinámicos desde backend
   - Valores por defecto: 20%-20%-20% (si no viene del backend)

**Autenticación:** Todos los endpoints protegidos con JWT Token

**APK generado:** easyrefer-debug.apk (46 MB)
**Estado:** ✅ Build successful - Subido a Drive

---

### 2026-03-12 - Corrección de Sombras en Botones
**Objetivo:** Eliminar sombras duplicadas en botones (doble elevación).

**Cambios realizados:**
- Analizadas todas las pantallas de la app
- Identificados 5 botones con `.shadow()` que causaban doble elevación:
  - LoginScreen.kt - 1 botón
  - PasswordResetScreen.kt - 3 botones
  - MaintenanceScreen.kt - 1 botón
- Eliminado `.shadow()` de botones, ahora usan solo `ButtonDefaults.buttonElevation(defaultElevation = 0.dp)`
- Las tarjetas (Cards) mantienen sus sombras correctas con `CARD_ELEVATION = 2.dp`

**APK generado:** easyrefer-debug.apk (46 MB)
**Estado:** ✅ Completado - Build successful

---

### 2026-03-11 - Subida a GitHub
**Objetivo:** Subir el proyecto a GitHub para control de versiones.

**Cambios realizados:**
- Inicializado repositorio Git
- Creado .gitignore para Android/Kotlin
- Subido a GitHub: https://github.com/megaelemento/EasyReferAppKotlin

**Estado:** ✅ Completado

---

### 2026-03-11 - QA y Correcciones del Panel
**Objetivo:** Revisar el panel de admin y opciones de Ganancias/Retiros.

**Cambios realizados:**
- Verificado que las opciones de Ganancias y Retiros aparecen condicionalmente según el balance
- Cards de acción rápida funcionando correctamente
- Menú lateral muestra Ganancias/Retiros solo cuando hay balance positivo

**Estado:** ✅ Completado

---

### 2026-03-11 - Fix de API Endpoints
**Objetivo:** Corregir los endpoints del panel admin.

**Cambios realizados:**
- Cambiado middleware de autenticación de cookies a token JWT para endpoints admin
- Corregidos los endpoints de empresas, retiros, referidos

**Estado:** ✅ Completado

---

## 📝 Historial de Cambios

### 2026-03-18 - Build APK con Cambios Recientes
**Objetivo:** Compilar y subir APK con los últimos cambios al Drive.

**Cambios detectados en git:**
- `HomeScreen.kt` - Modificaciones en pantalla principal
- `LoginScreen.kt` - Actualizaciones en login
- `PasswordResetScreen.kt` - Cambios en recuperación de contraseña
- `RegisterScreen.kt` - Modificaciones en registro
- `Theme.kt` - Ajustes de tema

**Build realizado:**
- Clean build: 38 tareas ejecutadas
- Tiempo: 2m 19s
- APK generado: `app-debug.apk` (45.9 MB)
- Subido a Google Drive: `EasyRefer/Apps/app-debug.apk`

**Warnings detectados (no bloqueantes):**
- Deprecaciones de `LocalLifecycleOwner` (mover a lifecycle-runtime-compose)
- Deprecaciones de íconos `Filled` (usar versión `AutoMirrored`)
- Deprecaciones de `menuAnchor()` (usar `MenuAnchorType`)
- Casts no verificados y condiciones siempre verdaderas

**Estado:** ✅ Completado

---

### 2026-03-18 00:34 - Build APK (Sin Cambios)
**Objetivo:** Compilar y subir APK al Drive.

**Build realizado:**
- Build incremental: 37 tareas up-to-date
- Tiempo: 3s
- APK generado: `app-debug.apk` (46.1 MB)
- Subido a Google Drive: `EasyRefer/Apps/app-debug.apk`

**Nota:** Sin cambios de código desde el build anterior.

**Estado:** ✅ Completado

---

### 2026-03-18 00:48 - Build APK con Fix de Error
**Objetivo:** Compilar y subir APK con corrección de error de compilación.

**Cambios detectados en git:**
- `HomeScreen.kt` - Modificaciones en pantalla principal
- `LoginScreen.kt` - Actualizaciones en login
- `PasswordResetScreen.kt` - Cambios en recuperación de contraseña
- `QRScannerScreen.kt` - Modificaciones en scanner QR
- `QRScreen.kt` - Cambios en pantalla QR
- `RegisterScreen.kt` - Modificaciones en registro
- `Theme.kt` - Ajustes de tema
- `QRViewModel.kt` - **FIX:** `setScannedQRData` ahora acepta `String?` para qrSecret

**Error corregido:**
- `QRScreen.kt:156` - Error de tipo: `String?` no compatible con `String`
- Solución: Modificado `QRViewModel.setScannedQRData()` para aceptar nullable y usar `?: ""` como fallback

**Build realizado:**
- Build: 37 tareas (8 ejecutadas, 29 up-to-date)
- Tiempo: 1m 2s
- APK generado: `app-debug.apk` (45.9 MB)
- Subido a Google Drive: `EasyRefer/Apps/app-debug.apk`

**Estado:** ✅ Completado

---

### 2026-03-18 00:35 - Build APK Clean (Con Cambios)
**Objetivo:** Compilar y subir APK con todos los cambios recientes al Drive.

**Cambios detectados en git:**
- `HomeScreen.kt` - Modificaciones en pantalla principal
- `LoginScreen.kt` - Actualizaciones en login
- `PasswordResetScreen.kt` - Cambios en recuperación de contraseña
- `QRScreen.kt` - Modificaciones en pantalla QR
- `RegisterScreen.kt` - Cambios en registro
- `Theme.kt` - Ajustes de tema

**Build realizado:**
- Clean build: 38 tareas ejecutadas
- Tiempo: 1m 42s
- APK generado: `app-debug.apk` (45.9 MB)
- Subido a Google Drive: `EasyRefer/Apps/app-debug.apk`

**Warnings detectados (no bloqueantes):**
- Deprecaciones de `LocalLifecycleOwner` (mover a lifecycle-runtime-compose)
- Deprecaciones de íconos `Filled` (usar versión `AutoMirrored`)
- Deprecaciones de `menuAnchor()` (usar `MenuAnchorType`)
- Casts no verificados y condiciones siempre verdaderas

**Estado:** ✅ Completado

---

### 2026-03-17 - Build APK Nightly
**Objetivo:** Compilar y subir APK actualizado a Drive.

**Cambios realizados:**
- Build successful con Gradle (37 tareas, 4 ejecutadas, 33 up-to-date)
- APK generado: `app-debug.apk` (47 MB)
- Subido a Google Drive: `EasyRefer/Apps/app-debug.apk`

**Estado:** ✅ Completado

---

## 📌 Pendientes
- [x] Subir a GitHub
- [ ] QA completo de la app
- [ ] Verificar funcionamiento de Ganancias y Retiros con datos reales

---

## 🔗 Proyectos Relacionados
- **Backend:** EasyReferBackendPython (misma base de datos)

---

*Este archivo debe ser actualizado cada vez que se hagan cambios significativos al proyecto.*
