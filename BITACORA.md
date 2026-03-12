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

## 📌 Pendientes
- [x] Subir a GitHub
- [ ] QA completo de la app
- [ ] Verificar funcionamiento de Ganancias y Retiros con datos reales

---

## 🔗 Proyectos Relacionados
- **Backend:** EasyReferBackendPython (misma base de datos)

---

*Este archivo debe ser actualizado cada vez que se hagan cambios significativos al proyecto.*
