package com.snick55.vkmessagertestapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class ClockView : View {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {
        setupAttributes(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
        setupAttributes(attrs)
    }

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var timeUpdater: Runnable

    private var dialColor: Int = Color.GRAY
    private var pointsColor: Int = Color.WHITE
    private var hourHandColor: Int = Color.BLACK
    private var minuteHandColor: Int = Color.BLACK
    private var secondHandColor: Int = Color.BLACK
    private var hourHandWidth: Float = 24f
    private var minuteHandWidth: Float = 12f
    private var secondHandWidth: Float = 6f


    private var textPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private lateinit var dialPaint: Paint
    private lateinit var pointerPaint: Paint
    private var strokePaint = Paint()

    private var hourData = 0f
    private var minuteData = 0f
    private var secondData = 0

    private var mWidth = 0.0f
    private var mHeight = 0.0f
    private var mRadius = 0.0f

    init {
        initParameters()
        timeUpdater = startCounting()
    }


    private fun startCounting(): Runnable {
        timeUpdater = object : Runnable {
            override fun run() {
                val currentTime: String =
                    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                setClockTime(
                    currentTime.substring(0, 2).toFloat() % 12,
                    currentTime.substring(3, 5).toFloat(),
                    currentTime.substring(6, 8).toInt()
                )
                handler.postDelayed(this, 1000)
            }
        }
        return timeUpdater
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        initParameters()
        drawBaseCircles(canvas)
        val numberCircleRadius = mRadius / 1.2f
        val numberLinesRadius = mRadius / 1.2f
        val pointRadius = mRadius / 15
        val minutesRadius = mRadius / 150

        drawNumbers(canvas,numberCircleRadius, pointRadius)
        drawMinutes(canvas,numberLinesRadius,minutesRadius)
        drawHandWithPaint(
            canvas,
            hourHandColor,
            hourHandWidth,
            calcXYForPosition(hourData, numberCircleRadius - 130, 30)
        )
        drawHandWithPaint(
            canvas,
            minuteHandColor,
            minuteHandWidth,
            calcXYForPosition(minuteData, numberCircleRadius - 90, 6)
        )
        drawHandWithPaint(
            canvas,
            secondHandColor,
            secondHandWidth,
            calcXYForPosition(secondData.toFloat(), numberCircleRadius - 30, 6)
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        mRadius = ((min(mWidth, mHeight)) / 2)
    }

    private fun calcXYForPosition(pos: Float, rad: Float, skipAngle: Int): ArrayList<Float> {
        val result = ArrayList<Float>(2)
        val startAngle = 270f
        val angle = startAngle + (pos * skipAngle)
        result.add(0, (rad * cos(angle * Math.PI / 180) + width / 2).toFloat())
        result.add(1, (height / 2 + rad * sin(angle * Math.PI / 180)).toFloat())
        return result
    }

    private fun drawHandWithPaint(
        canvas: Canvas,
        handColor: Int,
        strokeWidth: Float,
        xyData: ArrayList<Float>
    ) {
        val handPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        handPaint.color = handColor
        handPaint.strokeWidth = strokeWidth
        canvas.drawLine(mWidth / 2, mHeight / 2, xyData[0], xyData[1], handPaint)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        val typedArray: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.ClockView, 0, 0)
        dialColor =
            typedArray.getColor(R.styleable.ClockView_dialColor, dialColor)
        pointsColor =
            typedArray.getColor(R.styleable.ClockView_pointsColor, pointsColor)
        hourHandColor =
            typedArray.getColor(R.styleable.ClockView_hourHandColor, hourHandColor)
        minuteHandColor =
            typedArray.getColor(R.styleable.ClockView_minuteHandColor, minuteHandColor)
        secondHandColor =
            typedArray.getColor(R.styleable.ClockView_secondHandColor, secondHandColor)
        hourHandWidth =
            typedArray.getFloat(R.styleable.ClockView_hourHandWidth, hourHandWidth)
        minuteHandWidth =
            typedArray.getFloat(R.styleable.ClockView_minuteHandWidth, minuteHandWidth)
        secondHandWidth =
            typedArray.getFloat(R.styleable.ClockView_secondHandWidth, secondHandWidth)
        typedArray.recycle()
    }

    private fun drawNumbers(canvas: Canvas,numberCircleRadius: Float,pointRadius:Float){
        var num = 12
        for (i in 0..11) {
            Log.d("TAG","num = $num")
            val xyData = calcXYForPosition(i.toFloat(), numberCircleRadius, 30)
            val yPadding =
                if (mRadius > 200) xyData[1] + pointRadius - 10 else xyData[1] - pointRadius + 10
            canvas.drawText("$num", xyData[0], yPadding, textPaint)
            num = i + 1
        }
    }
    private fun drawMinutes(canvas: Canvas,numberLinesRadius:Float,minutesRadius: Float){
        for (i in 0..59) {
            val xyData = calcXYForPosition(i.toFloat(), numberLinesRadius, 6)
            canvas.drawCircle(xyData[0], xyData[1], minutesRadius, pointerPaint)
        }

    }

    private fun drawBaseCircles(canvas: Canvas) {
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, dialPaint)
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius - 10, strokePaint)
    }

    private fun setClockTime(hour: Float, minute: Float, second: Int) {
        hourData = hour + (minute / 60)
        minuteData = minute
        secondData = second
        invalidate()
    }

    private fun initParameters(){
        textPaint.color = Color.BLACK
        textPaint.style = Paint.Style.FILL_AND_STROKE
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 40f
        dialPaint = Paint()
        dialPaint.color = dialColor
        strokePaint.color = Color.BLACK
        strokePaint.strokeWidth = 20f
        strokePaint.style = Paint.Style.STROKE
        pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        pointerPaint.color = pointsColor
        textPaint.color = Color.BLACK
        textPaint.textSize = mRadius / 8
    }

    fun startClock() {
        handler.post(timeUpdater)
    }

    fun stopClock() {
        handler.removeCallbacks(timeUpdater)
    }
}