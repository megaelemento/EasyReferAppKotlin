package com.christelldev.easyreferplus.ui.utils

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.cos
import kotlin.math.sin

object MarkerUtils {

    /**
     * Pin moderno con icono de motocicleta.
     * Verde esmeralda = en turno | Gris pizarra = fuera de turno.
     */
    fun createDriverMarker(name: String?, isOnDuty: Boolean): BitmapDescriptor {
        val W = 92
        val H = 120
        val cx    = W / 2f
        val cR    = 35f          // radio del círculo
        val cy    = cR + 6f      // centro del círculo (margen top 6px)
        val tipY  = H - 5f       // punta del pin

        val mainColor = if (isOnDuty) Color.rgb(5, 150, 105)   else Color.rgb(71, 85, 105)
        val rimColor  = if (isOnDuty) Color.rgb(16, 185, 129)  else Color.rgb(100, 116, 139)

        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // ── Sombra ──────────────────────────────────────────────────────────
        val shadowPath = buildPinPath(cx + 2.5f, cy + 3f, cR + 1f, tipY + 3f)
        canvas.drawPath(shadowPath, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(55, 0, 0, 0)
            maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
        })

        // ── Borde blanco ─────────────────────────────────────────────────────
        val borderPath = buildPinPath(cx, cy, cR + 5f, tipY)
        canvas.drawPath(borderPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE })

        // ── Aro de color claro (borde interior) ──────────────────────────────
        val rimPath = buildPinPath(cx, cy, cR + 2.5f, tipY - 1.5f)
        canvas.drawPath(rimPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = rimColor })

        // ── Relleno principal ────────────────────────────────────────────────
        val mainPath = buildPinPath(cx, cy, cR, tipY - 3f)
        canvas.drawPath(mainPath, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = mainColor })

        // ── Brillo interior (gradiente radial desde arriba-izquierda) ────────
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                cx - cR * 0.25f, cy - cR * 0.30f,
                cR * 0.85f,
                intArrayOf(Color.argb(72, 255, 255, 255), Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawPath(mainPath, glowPaint)

        // ── Icono de moto ────────────────────────────────────────────────────
        drawMotoIcon(canvas, cx, cy, cR * 0.60f)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // ── Forma de pin (teardrop) ──────────────────────────────────────────────
    private fun buildPinPath(cx: Float, cy: Float, r: Float, tipY: Float): Path {
        val exitDeg  = 36.0
        val lAngle   = Math.toRadians(90.0 + exitDeg)
        val rAngle   = Math.toRadians(90.0 - exitDeg)
        val lx = cx + r * cos(lAngle).toFloat()
        val ly = cy + r * sin(lAngle).toFloat()
        val rx = cx + r * cos(rAngle).toFloat()
        val ry = cy + r * sin(rAngle).toFloat()

        return Path().apply {
            moveTo(lx, ly)
            lineTo(cx, tipY)
            lineTo(rx, ry)
            arcTo(
                cx - r, cy - r, cx + r, cy + r,
                (90f - exitDeg.toFloat()),
                -(360f - 2f * exitDeg.toFloat()),
                false
            )
            close()
        }
    }

    // ── Icono de motocicleta (vista lateral, trazos blancos) ─────────────────
    private fun drawMotoIcon(canvas: Canvas, cx: Float, cy: Float, sc: Float) {
        val sw = (sc * 0.155f).coerceAtLeast(2.8f)

        val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = sw
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // ── Ruedas ───────────────────────────────────────────────────────────
        val wR   = sc * 0.310f          // radio rueda
        val rWx  = cx - sc * 0.490f     // rueda trasera X
        val fWx  = cx + sc * 0.490f     // rueda delantera X
        val wy   = cy + sc * 0.310f     // Y de los ejes

        canvas.drawCircle(rWx, wy, wR, stroke)
        canvas.drawCircle(fWx, wy, wR, stroke)

        // ── Chasis / frame ───────────────────────────────────────────────────
        // Triángulo principal: eje trasero → pivot central → horquilla delantera
        val pivX = cx - sc * 0.04f
        val pivY = cy - sc * 0.08f

        // Brazo oscilante (eje trasero → pivot)
        canvas.drawLine(rWx, wy, pivX, pivY, stroke)

        // Tubo principal (pivot → parte superior de la horquilla)
        val forkTopX = fWx - sc * 0.07f
        val forkTopY = pivY - sc * 0.36f
        canvas.drawLine(pivX, pivY, forkTopX, forkTopY, stroke)

        // Horquilla delantera (fork top → eje delantera)
        canvas.drawLine(forkTopX, forkTopY, fWx, wy, stroke)

        // ── Asiento / depósito (línea horizontal sobre el pivot) ─────────────
        val seatR = cx - sc * 0.36f   // extremo trasero del asiento
        val seatF = cx + sc * 0.10f   // extremo delantero
        val seatY = pivY - sc * 0.30f
        canvas.drawLine(seatR, seatY + sc * 0.05f, seatF, seatY, stroke)

        // Tubo del depósito (seatF → forkTop)
        canvas.drawLine(seatF, seatY, forkTopX, forkTopY, stroke)

        // ── Manillar ──────────────────────────────────────────────────────────
        canvas.drawLine(
            forkTopX, forkTopY,
            forkTopX + sc * 0.28f, forkTopY - sc * 0.22f,
            stroke
        )

        // ── Cabeza del conductor ──────────────────────────────────────────────
        val headX = seatR + sc * 0.12f
        val headY = seatY - sc * 0.36f
        canvas.drawCircle(headX, headY, sc * 0.185f, fill)

        // Torso (línea cabeza → asiento)
        canvas.drawLine(headX, headY + sc * 0.185f, seatR + sc * 0.08f, seatY, stroke)
    }
}
