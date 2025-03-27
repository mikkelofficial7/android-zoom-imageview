package com.lib.zoomimageengine

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.widget.AppCompatImageView

class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs),
    View.OnTouchListener,
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
    }

    private val mScaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val mGestureDetector: GestureDetector = GestureDetector(context, this)
    private val mMatrix: Matrix = Matrix()
    private val mMatrixValues = FloatArray(9)

    private var mode = NONE
    private var mSaveScale = 1f
    private var mMinScale = 1f
    private var mMaxScale = 4f

    private var origWidth = 0f
    private var origHeight = 0f
    private var viewWidth = 0
    private var viewHeight = 0
    private val mLast = PointF()
    private val mStart = PointF()

    init {
        super.setClickable(true)
        imageMatrix = mMatrix
        scaleType = ScaleType.MATRIX
        setOnTouchListener(this)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor
            val prevScale = mSaveScale
            mSaveScale *= scaleFactor

            mSaveScale = mSaveScale.coerceIn(mMinScale, mMaxScale)
            scaleFactor = mSaveScale / prevScale

            if (origWidth * mSaveScale <= viewWidth || origHeight * mSaveScale <= viewHeight) {
                mMatrix.postScale(scaleFactor, scaleFactor, viewWidth / 2f, viewHeight / 2f)
            } else {
                mMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            }
            fixTranslation()
            return true
        }
    }

    private fun fitToScreen() {
        mSaveScale = 1f
        val drawable = drawable ?: return
        val imageWidth = drawable.intrinsicWidth
        val imageHeight = drawable.intrinsicHeight
        if (imageWidth == 0 || imageHeight == 0) return

        val scaleX = viewWidth.toFloat() / imageWidth
        val scaleY = viewHeight.toFloat() / imageHeight
        val scale = scaleX.coerceAtMost(scaleY)

        mMatrix.setScale(scale, scale)

        val redundantXSpace = (viewWidth - scale * imageWidth) / 2f
        val redundantYSpace = (viewHeight - scale * imageHeight) / 2f

        mMatrix.postTranslate(redundantXSpace, redundantYSpace)

        origWidth = viewWidth - 2 * redundantXSpace
        origHeight = viewHeight - 2 * redundantYSpace

        imageMatrix = mMatrix
    }

    private fun fixTranslation() {
        mMatrix.getValues(mMatrixValues)
        val transX = mMatrixValues[Matrix.MTRANS_X]
        val transY = mMatrixValues[Matrix.MTRANS_Y]
        val fixTransX = getFixTranslation(transX, viewWidth.toFloat(), origWidth * mSaveScale)
        val fixTransY = getFixTranslation(transY, viewHeight.toFloat(), origHeight * mSaveScale)

        if (fixTransX != 0f || fixTransY != 0f) {
            mMatrix.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun getFixTranslation(trans: Float, viewSize: Float, contentSize: Float): Float {
        return when {
            contentSize <= viewSize -> 0f
            trans < viewSize - contentSize -> (viewSize - contentSize) - trans
            trans > 0 -> -trans
            else -> 0f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (mSaveScale == 1f) {
            fitToScreen()
        }
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        mScaleDetector.onTouchEvent(event)
        mGestureDetector.onTouchEvent(event)

        val currentPoint = PointF(event.x, event.y)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mLast.set(currentPoint)
                mStart.set(mLast)
                mode = DRAG
            }
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                val dx = currentPoint.x - mLast.x
                val dy = currentPoint.y - mLast.y
                val fixTransX = getFixTranslation(dx, viewWidth.toFloat(), origWidth * mSaveScale)
                val fixTransY = getFixTranslation(dy, viewHeight.toFloat(), origHeight * mSaveScale)
                mMatrix.postTranslate(fixTransX, fixTransY)
                fixTranslation()
                mLast.set(currentPoint)
            }
            MotionEvent.ACTION_POINTER_UP -> mode = NONE
        }

        imageMatrix = mMatrix
        return true
    }

    override fun onDown(event: MotionEvent): Boolean = false
    override fun onShowPress(event: MotionEvent) {}
    override fun onSingleTapUp(event: MotionEvent): Boolean = false
    override fun onScroll(
        e2: MotionEvent,
        p1: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean = false
    override fun onLongPress(event: MotionEvent) {}
    override fun onFling(
        e2: MotionEvent,
        p1: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean = false

    override fun onSingleTapConfirmed(event: MotionEvent): Boolean = false

    override fun onDoubleTap(event: MotionEvent): Boolean {
        fitToScreen()
        return true
    }

    override fun onDoubleTapEvent(event: MotionEvent): Boolean = false
}
