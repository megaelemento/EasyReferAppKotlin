package com.christelldev.easyreferplus.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.christelldev.easyreferplus.data.model.TransferResponse
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object WalletReceiptImageBuilder {

    fun share(context: Context, transfer: TransferResponse) {
        val senderName = context
            .getSharedPreferences("EasyReferPrefs", Context.MODE_PRIVATE)
            .getString("user_nombres", null) ?: ""
        val bmp = buildBitmap(context, transfer, senderName)
        val file = File(context.cacheDir, "comprobante_${transfer.id}.png")
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                "Compartir comprobante"
            )
        )
    }

    // "16 de marzo de 2026"
    private fun fmtDateLong(iso: String): String = try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(iso)!!
        val day   = SimpleDateFormat("d", Locale("es", "EC")).format(input)
        val month = SimpleDateFormat("MMMM", Locale("es", "EC")).format(input)
        val year  = SimpleDateFormat("yyyy", Locale("es", "EC")).format(input)
        "El $day de $month de $year"
    } catch (_: Exception) { iso.take(10) }

    private fun tp(
        size: Float,
        color: Int,
        bold: Boolean = false,
        center: Boolean = false,
        right: Boolean = false
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        textAlign = when {
            center -> Paint.Align.CENTER
            right  -> Paint.Align.RIGHT
            else   -> Paint.Align.LEFT
        }
    }

    private fun buildBitmap(context: Context, t: TransferResponse, senderName: String): Bitmap {
        val dens = context.resources.displayMetrics.density
        fun f(v: Float) = v * dens

        // ── Palette ──────────────────────────────────────────────────────────
        val BLUE       = Color.parseColor("#1565C0")
        val DARK       = Color.parseColor("#1A2340")
        val GRAY       = Color.parseColor("#6B7A99")
        val LIGHT_GRAY = Color.parseColor("#E4EAF4")
        val GREEN_BG   = Color.parseColor("#E8F5E9")
        val GREEN_FG   = Color.parseColor("#2E7D32")

        // ── Width & horizontal padding ────────────────────────────────────────
        val W    = (380f * dens).toInt()
        val half = W / 2f
        val PH   = f(28f)   // page horizontal padding
        val CP   = f(24f)   // card inner padding

        // ── Y trace (top → bottom) ────────────────────────────────────────────
        var y = f(24f)

        // logo
        val yLogo = y + f(13f);               y += f(32f)

        // divider after logo
        val yLogoDivider = y;                 y += f(24f)

        // check circle center
        val yCircle = y + f(30f);             y += f(74f)

        // "¡Transferencia Exitosa!"
        val ySuccess = y + f(14f);            y += f(26f)

        // amount
        val yAmt = y + f(34f);               y += f(48f)

        // "A [name]"
        val yToName = y + f(16f);            y += f(24f)

        // date
        val yDate = y + f(14f);              y += f(22f)

        // "De [sender]"
        val yFrom = y + f(14f);              y += f(30f)

        // divider
        val yDiv = y;                         y += f(24f)

        // detail rows (2 rows)
        val rowH  = f(36f)
        val yR1   = y + f(14f);              y += rowH
        val yR2   = y + f(14f);              y += rowH + f(8f)

        // footer
        val yFooter = y + f(16f);            y += f(36f)

        val TOTAL_H = (y / dens).toInt() + 12

        // ── Bitmap & canvas ───────────────────────────────────────────────────
        val bmp = Bitmap.createBitmap(W, (TOTAL_H * dens).toInt(), Bitmap.Config.ARGB_8888)
        val cv  = Canvas(bmp)
        val p   = Paint(Paint.ANTI_ALIAS_FLAG)

        // ── Background white ─────────────────────────────────────────────────
        p.color = Color.WHITE
        cv.drawRect(0f, 0f, W.toFloat(), bmp.height.toFloat(), p)

        // ── Top logo area (background watermark strip) ────────────────────────
        // Subtle top bar — very light blue tint
        p.color = Color.parseColor("#F5F8FF")
        cv.drawRect(0f, 0f, W.toFloat(), yLogoDivider + f(1f), p)

        // "Enfoque Refer" logo text
        cv.drawText("Enfoque Refer", half, yLogo,
            tp(f(15f), BLUE, bold = true, center = true))

        // Logo divider
        p.color = LIGHT_GRAY
        cv.drawRect(PH, yLogoDivider, W - PH, yLogoDivider + f(0.8f), p)

        // ── Green check circle ────────────────────────────────────────────────
        p.color = GREEN_BG
        cv.drawCircle(half, yCircle, f(30f), p)
        // Inner darker ring
        p.color = Color.parseColor("#C8E6C9")
        p.style = Paint.Style.STROKE
        p.strokeWidth = f(1.5f)
        cv.drawCircle(half, yCircle, f(30f), p)
        p.style = Paint.Style.FILL
        // Checkmark "✓"
        cv.drawText("✓", half, yCircle + f(11f),
            tp(f(28f), GREEN_FG, bold = true, center = true))

        // ── "¡Transferencia Exitosa!" ─────────────────────────────────────────
        cv.drawText("¡Transferencia Exitosa!", half, ySuccess,
            tp(f(14f), GREEN_FG, bold = true, center = true))

        // ── Big amount ────────────────────────────────────────────────────────
        cv.drawText("\$${String.format("%.2f", t.amount)}", half, yAmt,
            tp(f(40f), DARK, bold = true, center = true))

        // ── "A [recipient]" ───────────────────────────────────────────────────
        val truncName = if (t.recipientName.length > 32) t.recipientName.take(30) + "…" else t.recipientName
        cv.drawText("A $truncName", half, yToName,
            tp(f(13f), DARK, bold = true, center = true))

        // ── Date ─────────────────────────────────────────────────────────────
        cv.drawText(fmtDateLong(t.createdAt), half, yDate,
            tp(f(12f), GRAY, center = true))

        // ── "De [sender]" ─────────────────────────────────────────────────────
        if (senderName.isNotBlank()) {
            val truncSender = if (senderName.length > 32) senderName.take(30) + "…" else senderName
            cv.drawText("De $truncSender", half, yFrom,
                tp(f(12f), GRAY, center = true))
        }

        // ── Divider ───────────────────────────────────────────────────────────
        p.color = LIGHT_GRAY
        cv.drawRect(PH, yDiv, W - PH, yDiv + f(0.8f), p)

        // ── Detail rows ───────────────────────────────────────────────────────
        fun row(label: String, value: String, yPos: Float) {
            cv.drawText(label, PH, yPos, tp(f(12f), GRAY))
            cv.drawText(value, W - PH, yPos,
                tp(f(12f), DARK, bold = true, right = true))
        }

        // Format phone: keep as-is or trim +593 prefix for display
        val displayPhone = t.recipientPhone
            .let { if (it.startsWith("+593")) "0${it.drop(4)}" else it }

        row("Teléfono destino", displayPhone, yR1)
        row("N° de comprobante", t.id.toString().padStart(8, '0'), yR2)

        // ── Footer ────────────────────────────────────────────────────────────
        // Subtle gray footer background
        p.color = Color.parseColor("#F5F8FF")
        cv.drawRect(0f, yFooter - f(20f), W.toFloat(), bmp.height.toFloat(), p)

        p.color = LIGHT_GRAY
        cv.drawRect(PH, yFooter - f(20f), W - PH, yFooter - f(19.2f), p)

        cv.drawText("Enfoque Refer · Plataforma de referidos", half, yFooter,
            tp(f(10f), GRAY, center = true))

        return bmp
    }
}
