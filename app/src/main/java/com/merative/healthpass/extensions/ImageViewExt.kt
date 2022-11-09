package com.merative.healthpass.extensions

import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape
import android.util.Base64
import android.widget.ImageView
import androidx.annotation.Px
import androidx.core.view.isVisible
import com.merative.healthpass.R
import com.merative.healthpass.utils.asyncToUiSingle
import com.merative.watson.healthpass.utils.QRCodeEncoder
import io.reactivex.rxjava3.core.Single

/**
 * set the image view from the base64 string, unless it is empty or null then it will use the defaultString
 * as a TextDrawable, and if this is also empty or null, then it changes the ImageView to invisible
 */
fun ImageView.setImageFromBase64(base64String: String?, defaultString: String?) {
    when {
        base64String.isNotNullOrEmpty() -> {
            val decodedString: ByteArray = Base64.decode(base64String, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            setImageBitmap(decodedByte)
        }
        defaultString.isNotNullOrEmpty() -> {
            background = getTextDrawable(
                defaultString.orEmpty(),
                context.resources.getDimensionPixelSize(R.dimen.font_24).toFloat(),
                context.getColor(R.color.textLabels),
                context.getColor(R.color.cardBackground),
                context.resources.getDimensionPixelSize(R.dimen.card_corner).toFloat()
            )
        }
        else -> {
            isVisible = base64String.isNotNullOrEmpty() && defaultString.isNotNullOrEmpty()
        }
    }
}

fun getTextDrawable(
    text: String,
    fontSize: Float,
    backgroundColor: Int,
    fontColor: Int,
    radius: Float = 30f
): Drawable {
    val shape: Shape = object : Shape() {

        override fun draw(canvas: Canvas, paint: Paint) {
            paint.color = backgroundColor
            paint.style = Paint.Style.FILL

            val radiusArr = floatArrayOf(
                radius, radius,
                radius, radius,
                radius, radius,
                radius, radius
            )
            val myPath = Path()
            myPath.addRoundRect(
                RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat()),
                radiusArr,
                Path.Direction.CW
            )

            canvas.drawPath(myPath, paint)

            paint.color = fontColor
            paint.textSize = fontSize
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                text,
                canvas.width / 2.toFloat(),
                canvas.height / 2.toFloat() + fontSize / 3,
                paint
            )
        }
    }
    return ShapeDrawable(shape)
}

fun ImageView.setImageFromQR(
    qrCodeJson: String,
    @Px size: Int = 300,
    color: Int = Color.BLACK,
    backgroundColor: Int = Color.WHITE
) {
    Single.fromCallable {
        QRCodeEncoder().encode(qrCodeJson, size, color, backgroundColor)
    }.asyncToUiSingle()
        .subscribe({ bmp ->
            setImageBitmap(bmp)
        }, rxError("failed to set image from qr"))

}