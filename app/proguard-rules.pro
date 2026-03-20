# ============================================
# EasyRefer Plus - Reglas de ProGuard/R8
# ============================================

# Conservar Modelos de Datos (CRÍTICO para GSON/Retrofit)
-keep class com.christelldev.easyreferplus.data.model.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Gson specific rules
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Retrofit specific rules
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext {
    private volatile java.lang.Object _reusableContinuation;
}

# Coil (Image Loading)
-keep class coil.** { *; }