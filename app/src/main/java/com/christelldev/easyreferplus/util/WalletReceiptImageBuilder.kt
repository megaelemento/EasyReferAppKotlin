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
        val bmp = buildBitmap(context, transfer)
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

    private fun fmtDate(iso: String): String = try {
        val d = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(iso)!!
        SimpleDateFormat("dd MMM yyyy   HH:mm", Locale("es", "EC")).format(d)
    } catch (_: Exception) { iso.take(16).replace("T", "   ") }

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

    private fun buildBitmap(context: Context, t: TransferResponse): Bitmap {
        val dens = context.resources.displayMetrics.density
        fun f(v: Float) = v * dens
        fun fi(v: Float) = (v * dens).toInt()

        val W    = fi(380f)
        val half = W / 2f
        val PH   = f(20f)   // horizontal page padding
        val CP   = f(22f)   // card inner padding

        // ── Y-position trace (all in px, drawn in order) ─────────────────────
        var y = f(154f) + CP                  // card top (154dp) + inner padding

        val yTitle      = y + f(16f);  y += f(28f)
        val yRef        = y + f(13f);  y += f(22f)
        val yDiv1       = y;           y += f(20f)
        val yRow1       = y + f(14f);  y += f(38f)
        val yRow2       = y + f(14f);  y += f(38f)
        val yRow3       = y + f(14f);  y += f(38f)
        val yDiv2       = y;           y += f(18f)
        val yAmtLabel   = y + f(13f)
        val yAmtValue   = y + f(20f)
        val cardB       = y + f(32f)           // card bottom = amount row + bottom padding
        val yFooterLine = cardB + f(14f)       // thin line below card
        val yFooter     = yFooterLine + f(24f) // footer text
        val TOTAL_H     = fi(yFooter / dens + 24f)

        val bmp = Bitmap.createBitmap(W, TOTAL_H, Bitmap.Config.ARGB_8888)
        val cv  = Canvas(bmp)
        val p   = Paint(Paint.ANTI_ALIAS_FLAG)

        // ── Background ───────────────────────────────────────────────────────
        p.color = Color.parseColor("#F0F4FF")
        cv.drawRect(0f, 0f, W.toFloat(), TOTAL_H.toFloat(), p)

        // ── Header gradient ──────────────────────────────────────────────────
        val headerH = f(170f)
        p.shader = LinearGradient(
            0f, 0f, 0f, headerH,
            Color.parseColor("#1565C0"), Color.parseColor("#2196F3"),
            Shader.TileMode.CLAMP
        )
        cv.drawRect(0f, 0f, W.toFloat(), headerH, p)
        p.shader = null

        // Icon circle
        p.color = Color.parseColor("#28FFFFFF")
        cv.drawCircle(half, f(50f), f(26f), p)
        cv.drawText("✓", half, f(59f), tp(f(18f), Color.WHITE, bold = true, center = true))

        // Header texts
        cv.drawText("¡Transferencia Exitosa!", half, f(96f),
            tp(f(13f), Color.parseColor("#CCFFFFFF"), center = true))
        cv.drawText("\$${String.format("%.2f", t.amount)}", half, f(140f),
            tp(f(38f), Color.WHITE, bold = true, center = true))
        cv.drawText("enviados", half, f(158f),
            tp(f(11f), Color.parseColor("#99FFFFFF"), center = true))

        // ── Card ─────────────────────────────────────────────────────────────
        val cardL = PH; val cardR = W - PH
        p.color = Color.WHITE
        p.setShadowLayer(f(8f), 0f, f(3f), Color.parseColor("#18000000"))
        cv.drawRoundRect(RectF(cardL, f(154f), cardR, cardB), f(18f), f(18f), p)
        p.clearShadowLayer()

        val cL = cardL + CP
        val cR = cardR - CP

        // Title
        cv.drawText("Comprobante de Pago", cL, yTitle,
            tp(f(14f), Color.parseColor("#1E293B"), bold = true))

        // "Aprobado" badge
        val badgeStr = "Aprobado"
        val bTP = tp(f(10f), Color.parseColor("#059669"), bold = true)
        val bW  = bTP.measureText(badgeStr) + f(22f)
        val bRect = RectF(cR - bW, yTitle - f(16f), cR, yTitle + f(5f))
        p.color = Color.parseColor("#D1FAE5")
        cv.drawRoundRect(bRect, f(10f), f(10f), p)
        cv.drawText(badgeStr, bRect.left + f(11f), yTitle, bTP)

        // Ref number
        cv.drawText("Ref. #${t.id.toString().padStart(6, '0')}", cL, yRef,
            tp(f(11f), Color.parseColor("#CBD5E1")))

        // Dividers
        p.color = Color.parseColor("#E2E8F0")
        cv.drawRect(cL, yDiv1, cR, yDiv1 + 1f, p)
        cv.drawRect(cL, yDiv2, cR, yDiv2 + 1f, p)

        // Detail rows
        fun row(label: String, value: String, yPos: Float, vColor: String = "#334155") {
            cv.drawText(label, cL, yPos, tp(f(12f), Color.parseColor("#94A3B8")))
            cv.drawText(value, cR, yPos, tp(f(12f), Color.parseColor(vColor), bold = true, right = true))
        }
        row("Beneficiario", t.recipientName,  yRow1)
        row("Teléfono",     t.recipientPhone, yRow2)
        row("Fecha",        fmtDate(t.createdAt), yRow3)

        // Amount row
        cv.drawText("Monto transferido", cL, yAmtLabel, tp(f(12f), Color.parseColor("#94A3B8")))
        cv.drawText(
            "\$${String.format("%.2f", t.amount)}", cR, yAmtValue,
            tp(f(22f), Color.parseColor("#2196F3"), bold = true, right = true)
        )

        // ── Footer thin line ─────────────────────────────────────────────────
        p.color = Color.parseColor("#CBD5E1")
        cv.drawRect(PH, yFooterLine, W - PH, yFooterLine + 0.5f, p)

        cv.drawText("Enviado con Enfoque Refer", half, yFooter,
            tp(f(11f), Color.parseColor("#94A3B8"), center = true))

        return bmp
    }
}
