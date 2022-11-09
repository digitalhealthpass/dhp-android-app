package com.merative.watson.healthpass.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.BLACK
import android.graphics.Color.TRANSPARENT
import android.graphics.Paint
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import com.merative.watson.healthpass.R
import kotlin.math.roundToInt

class ScannerOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var boxLeftBorder = 0f
    private var boxTopBorder = 0f

    private var boxWidth = 0f
    private var boxHeight = 0f
    private var cornerLineWidth = 0f
    private var cornerLineLength = 0f

    private var overlayColor = 0
    private var cornerLineColor = 0

    private var drawText = ""
    private var textColor = 0
    private var textSize = 0f

    private val cornerLinePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint()
    private val overlayRect = RectF()
    private val drawTextCoordinate = Coordinate()

    init {
        attrs?.let {
            val typedArray = getAttributes(context, attrs, defStyleAttr)

            boxWidth = typedArray.getDimension(R.styleable.ScannerOverlay_square_width, 0f)
            boxHeight = typedArray.getDimension(R.styleable.ScannerOverlay_square_height, 0f)
            cornerLineWidth = typedArray.getDimension(R.styleable.ScannerOverlay_line_width, 4f)
            cornerLineLength = typedArray.getDimension(R.styleable.ScannerOverlay_line_length, 40f)
            overlayColor =
                typedArray.getColor(R.styleable.ScannerOverlay_overlay_color, TRANSPARENT)
            cornerLineColor = typedArray.getColor(R.styleable.ScannerOverlay_line_color, BLACK)

            drawText = typedArray.getString(R.styleable.ScannerOverlay_text) ?: ""
            textColor =
                typedArray.getColor(R.styleable.ScannerOverlay_text_color, BLACK)
            textSize = typedArray.getDimension(R.styleable.ScannerOverlay_text_size, 20f)

            typedArray.recycle()
        }

        initCornerLinePaint()
        initOverlayPaint()
        initTextPaint()
    }

    private fun getAttributes(context: Context, attrs: AttributeSet?, defAttr: Int) =
        context.obtainStyledAttributes(attrs, R.styleable.ScannerOverlay, defAttr, 0)

    private fun initCornerLinePaint() {
        cornerLinePaint
            .apply { color = cornerLineColor }
            .apply { strokeWidth = dpToPx(cornerLineWidth).toFloat() }
            .apply { strokeCap = Paint.Cap.ROUND }
    }

    private fun initOverlayPaint() {
        overlayPaint
            .apply { style = Paint.Style.FILL }
            .apply { color = overlayColor }
    }

    private fun initTextPaint() {
        textPaint
            .apply { style = Paint.Style.FILL }
            .apply { color = this@ScannerOverlay.textColor }
            .apply { textSize = this@ScannerOverlay.textSize }
            .apply { textAlignment = TEXT_ALIGNMENT_CENTER }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        val viewWidth = MeasureSpec.getSize(widthMeasureSpec)

        if (boxWidth == 0f || boxHeight == 0f) {
            boxWidth = viewWidth.toFloat() / 3.8f
            boxHeight = boxWidth
        }

        boxLeftBorder = (viewWidth - dpToPx(boxWidth)) / 2.toFloat()
        boxTopBorder = (viewHeight - dpToPx(boxHeight)) / 2.toFloat()

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawTopOverlay(canvas)
        drawLeftOverlay(canvas)
        drawRightOverlay(canvas)
        drawBottomOverlay(canvas)
        drawCornerLines(canvas)
        drawText(canvas)

        invalidate()
    }

    private fun drawText(canvas: Canvas) {
        val staticLayout = StaticLayout.Builder
            .obtain(drawText, 0, drawText.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .build()
        drawTextCoordinate.y = dpToPx(boxHeight) + boxTopBorder + height / 20

        canvas.translate(drawTextCoordinate.x, drawTextCoordinate.y)
        staticLayout.draw(canvas)
        canvas.save()
        canvas.restore()
    }

    private fun drawTopOverlay(canvas: Canvas) {
        overlayRect.apply {
            left = 0f
            top = 0f
            right = measuredWidth.toFloat()
            bottom = this@ScannerOverlay.boxTopBorder + 0.25f
        }
        canvas.drawRoundRect(overlayRect, 0f, 0f, overlayPaint)
    }

    private fun drawLeftOverlay(canvas: Canvas) {
        overlayRect.apply {
            left = 0f
            top = this@ScannerOverlay.boxTopBorder
            right = this@ScannerOverlay.boxLeftBorder
            bottom = dpToPx(boxHeight) + this@ScannerOverlay.boxTopBorder
        }
        canvas.drawRoundRect(overlayRect, 0f, 0f, overlayPaint)
    }

    private fun drawRightOverlay(canvas: Canvas) {
        overlayRect.apply {
            left = dpToPx(boxWidth) + this@ScannerOverlay.boxLeftBorder
            top = this@ScannerOverlay.boxTopBorder
            right = measuredWidth.toFloat()
            bottom = dpToPx(boxHeight) + this@ScannerOverlay.boxTopBorder
        }
        canvas.drawRoundRect(overlayRect, 0f, 0f, overlayPaint)
    }

    private fun drawBottomOverlay(canvas: Canvas) {
        overlayRect.apply {
            left = 0f
            top = dpToPx(boxHeight) + this@ScannerOverlay.boxTopBorder - 0.25f
            right = measuredWidth.toFloat()
            bottom = measuredHeight.toFloat()
        }
        canvas.drawRoundRect(overlayRect, 0f, 0f, overlayPaint)
    }

    private fun drawCornerLines(canvas: Canvas) {
        drawTopLeftHorizontalLine(canvas)
        drawTopRightHorizontalLine(canvas)
        drawBottomLeftHorizontalLine(canvas)
        drawBottomRightHorizontalLine(canvas)

        drawTopLeftVerticalLine(canvas)
        drawTopRightVerticalLine(canvas)
        drawBottomLeftVerticalLine(canvas)
        drawBottomRightVerticalLine(canvas)
    }

    private fun drawBottomRightVerticalLine(canvas: Canvas) {
        canvas.drawLine(
            dpToPx(boxWidth) + boxLeftBorder,
            (dpToPx(boxHeight) + boxTopBorder) - cornerLineLength,
            dpToPx(boxWidth) + boxLeftBorder,
            (dpToPx(boxHeight) + boxTopBorder),
            cornerLinePaint
        )
    }

    private fun drawTopRightVerticalLine(canvas: Canvas) {
        canvas.drawLine(
            dpToPx(boxWidth) + boxLeftBorder,
            boxTopBorder,
            dpToPx(boxWidth) + boxLeftBorder,
            boxTopBorder + cornerLineLength,
            cornerLinePaint
        )
    }

    private fun drawBottomLeftVerticalLine(canvas: Canvas) {
        canvas.drawLine(
            boxLeftBorder,
            (dpToPx(boxHeight) + boxTopBorder) - cornerLineLength,
            boxLeftBorder,
            (dpToPx(boxHeight) + boxTopBorder),
            cornerLinePaint
        )
    }

    private fun drawTopLeftVerticalLine(canvas: Canvas) {
        canvas.drawLine(
            boxLeftBorder,
            boxTopBorder,
            boxLeftBorder,
            boxTopBorder + cornerLineLength,
            cornerLinePaint
        )
    }

    private fun drawBottomRightHorizontalLine(canvas: Canvas) {
        canvas.drawLine(
            (dpToPx(boxWidth) + boxLeftBorder - cornerLineLength),
            dpToPx(boxHeight) + boxTopBorder,
            dpToPx(boxWidth) + boxLeftBorder,
            dpToPx(boxHeight) + boxTopBorder,
            cornerLinePaint
        )
    }

    private fun drawBottomLeftHorizontalLine(canvas: Canvas) {
        canvas.drawLine(
            boxLeftBorder,
            dpToPx(boxHeight) + boxTopBorder,
            boxLeftBorder + cornerLineLength,
            dpToPx(boxHeight) + boxTopBorder,
            cornerLinePaint
        )
    }

    private fun drawTopRightHorizontalLine(canvas: Canvas) {
        canvas.drawLine(
            (dpToPx(boxWidth) + boxLeftBorder - cornerLineLength),
            boxTopBorder,
            dpToPx(boxWidth) + boxLeftBorder,
            boxTopBorder,
            cornerLinePaint
        )
    }

    private fun drawTopLeftHorizontalLine(canvas: Canvas) {
        canvas.drawLine(
            boxLeftBorder,
            boxTopBorder,
            boxLeftBorder + cornerLineLength,
            boxTopBorder,
            cornerLinePaint
        )
    }

    private fun dpToPx(dp: Float): Int {
        val displayMetrics = resources.displayMetrics
        return (dp.toInt() * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    class Coordinate(var x: Float = 0f, var y: Float = 0f)
}