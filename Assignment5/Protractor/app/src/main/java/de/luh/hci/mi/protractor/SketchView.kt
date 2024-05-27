package de.luh.hci.mi.protractor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.RequiresApi
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.util.*

class SketchView(c: Context) : View(c) {
    private enum class Modi {
        TRAINING, EVALUATION, FREERUNNING
    }
    // Gesture names to evaluate
    private val gestureNames = arrayOf("circle", "check", "triangle")

    // Initialize a 3x3 confusion matrix (3 gestures, 10 attempts each)
    private val confusionMatrix = Array(3) { IntArray(3) }


    // @todo: define your own pattern of positive vibration feedback
    private val positiveVibrationFeedback = longArrayOf(50, 50) // Buzz for 50ms, pause for 50ms


    // @todo: define your own pattern of negative vibration feedback
    private val negativeVibrationFeedback = longArrayOf(50, 50, 50, 100, 50) // Buzz, pause, repeat

    private val paint = Paint()

    // will be filled in a method
    private var evaluationGestures = arrayOf<String?>()
    private var currentGestureToEvaluate = 0
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var state = Modi.TRAINING
    private val recognizer = GestureRecognizer()
    private val trace = Vector<PointF>()
    private val templates = Vector<Template>()
    private val gestureFileName = "gestures001.dat"
    private var dout: DataOutputStream? = null
    private val vibrator: Vibrator

    private fun clear() {
        if (canvas != null) {
            paint.setARGB(255, 255, 255, 255)
            canvas!!.drawPaint(paint)
        }
    }
    fun translateToOrigin(trace: List<PointF>): List<PointF> {
        // Calculate centroid
        var sumX = 0f
        var sumY = 0f
        for (point in trace) {
            sumX += point.x
            sumY += point.y
        }
        val centroid = PointF(sumX / trace.size, sumY / trace.size)

        // Translate trace to origin
        val translatedTrace = mutableListOf<PointF>()
        for (point in trace) {
            translatedTrace.add(PointF(point.x - centroid.x, point.y - centroid.y))
        }

        return translatedTrace
    }

    fun simulateGestureRecognition(gestureName: String): String {
        // Generate a trace for the specified gesture (this is highly simplified)
        val trace = createGestureTrace(gestureName) // This simulates a predefined gesture

        // Resample, normalize, etc. (if needed, based on your recognizer logic)
        val resampledTrace = recognizer.resample(trace, GestureRecognizer.SAMPLE_POINTS_COUNT)
        val translatedTrace = recognizer.translateToOrigin(resampledTrace) // Or similar normalization

        // Recognize the gesture
        val match = recognizer.recognize(translatedTrace, templates)

        // Return the recognized gesture name
        return match?.template?.gestureName ?: "unknown"
    }

    // A helper function to simulate a predefined gesture trace (example implementation)
    private fun createGestureTrace(gestureName: String): List<PointF> {
        val trace = mutableListOf<PointF>()

        // Example: create a simple circle gesture
        if (gestureName == "circle") {
            // Add points to simulate a circle
            trace.add(PointF(50f, 50f))
            trace.add(PointF(100f, 100f))
            trace.add(PointF(150f, 50f))
            // More points to form a circle
        }
        // Add other gesture shapes based on your defined gestures

        return trace
    }

    fun testGestureRecognition() {
        // Test 10 times for each gesture
        for (i in gestureNames.indices) {
            val gestureName = gestureNames[i]

            for (attempt in 1..10) {
                // Simulate drawing a gesture and getting recognized result
                val recognizedGesture = simulateGestureRecognition(gestureName)

                // Determine the indices for the confusion matrix
                val rowIndex = gestureNames.indexOf(gestureName) // the gesture entered
                val colIndex = gestureNames.indexOf(recognizedGesture) // the gesture recognized

                // Increment the corresponding cell in the confusion matrix
                confusionMatrix[rowIndex][colIndex] += 1
            }
        }

        // Output the confusion matrix to check results
        printConfusionMatrix()
    }

    fun printConfusionMatrix() {
        println("Confusion Matrix:")
        for (row in confusionMatrix) {
            println(row.joinToString(" ")) // Print each row of the matrix
        }
    }
    // Example trigger for the test
    fun runTests() {
        testGestureRecognition()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        canvas = Canvas(bitmap!!)
        paint.isAntiAlias = false
        clear()
        prompt()
    }

    override fun onDraw(c: Canvas) {
        if (bitmap != null) {
            c.drawBitmap(bitmap!!, 0f, 0f, null)
        }
    }

    private fun prompt() {
        if (templates.size < GESTURE_SET.size * EXAMPLES_PER_GESTURE) {
            val tmod = templates.size % EXAMPLES_PER_GESTURE
            val tdiv = templates.size / EXAMPLES_PER_GESTURE
            paint.setARGB(255, 0, 0, 0)
            canvas!!.drawText(
                "Draw example " + (tmod + 1) + " of gesture " + GESTURE_SET[tdiv],
                10f,
                30f,
                paint
            )
        }
    }

    private fun promptEvaluation() {
        paint.setARGB(255, 0, 0, 0)
        canvas!!.drawText(
            "Evaluation: Draw gesture " + evaluationGestures[currentGestureToEvaluate] + ". Done " + currentGestureToEvaluate + " of " + evaluationGestures.size,
            10f,
            30f,
            paint
        )
    }

    private fun promptEvaluationResult(t: Template?) {
        paint.setARGB(255, 0, 0, 0)
        canvas!!.drawText(
            "Requested: " + evaluationGestures[currentGestureToEvaluate] + " Recognized:  " + t!!.gestureName,
            10f,
            60f,
            paint
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            trace.clear()
            clear()
        }
        paint.setARGB(255, 0, 0, 0)
        val x = e.x.toInt()
        val y = e.y.toInt()
        if (e.action == MotionEvent.ACTION_DOWN) {
            canvas!!.drawCircle(x.toFloat(), y.toFloat(), 6f, paint)
        } else {
            canvas!!.drawCircle(x.toFloat(), y.toFloat(), 3f, paint)
        }
        if (e.pointerCount > 1) {
            val x2 = e.getX(1).toInt()
            val y2 = e.getY(1).toInt()
            if (e.action == MotionEvent.ACTION_DOWN) {
                canvas!!.drawCircle(x2.toFloat(), y2.toFloat(), 6f, paint)
            } else {
                canvas!!.drawCircle(x2.toFloat(), y2.toFloat(), 3f, paint)
            }
        }
        trace.add(PointF(e.x, e.y))

        // Log.d("SketchView", "up");
        if (e.action == MotionEvent.ACTION_UP) {
            clear()
            val resampledTrace: Vector<PointF> =
                recognizer.resample(trace, GestureRecognizer.SAMPLE_POINTS_COUNT)
            if (resampledTrace.size >= 2) {
                val c = recognizer.centroid(resampledTrace)
                c.x = -c.x
                c.y = -c.y
                recognizer.translate(resampledTrace, c)
                recognizer.normalize(resampledTrace)
                paint.setARGB(255, 0, 0, 0)
                drawTrace(
                    resampledTrace, 0f, DRAW_SCALE.toFloat(), PointF(
                        DRAW_SCALE.toFloat(), DRAW_SCALE.toFloat()
                    )
                )
            } else {
                return true
            }
            val n = templates.size
            if (state == Modi.TRAINING && n >= GESTURE_SET.size * EXAMPLES_PER_GESTURE) {
                state = Modi.FREERUNNING
            }
            if (state == Modi.TRAINING) {
                // save entered template
                val id = n / EXAMPLES_PER_GESTURE
                val t = Template(id, GESTURE_SET[id], resampledTrace)
                templates.add(t)
                try {
                    t.writeTo(dout!!)
                } catch (ex: IOException) {
                    Log.d("SketchView", ex.message!!)
                }
                prompt()
            } else {
                if (dout != null) {
                    try {
                        dout!!.close()
                        dout = null
                        Log.d("SketchView", "stream closed")
                    } catch (ex: IOException) {
                        Log.d("SketchView", ex.message!!)
                    }
                }
                val m = recognizer.recognize(resampledTrace, templates)
                val t = m!!.template

                // top, left: input gesture (black)
                paint.setARGB(255, 0, 0, 0)
                drawTrace(
                    resampledTrace, 0f, DRAW_SCALE.toFloat(), PointF(
                        DRAW_SCALE.toFloat(), DRAW_SCALE.toFloat()
                    )
                )

                // top, right: template (red)
                paint.setARGB(255, 255, 0, 0)
                drawTrace(
                    t!!.vector,
                    0f,
                    DRAW_SCALE.toFloat(),
                    PointF((2 * DRAW_SCALE).toFloat(), DRAW_SCALE.toFloat())
                )

                // bottom, left: input gesture (black)
                paint.setARGB(255, 0, 0, 0)
                drawTrace(
                    resampledTrace, 0f, DRAW_SCALE.toFloat(), PointF(
                        DRAW_SCALE.toFloat(), (2 * DRAW_SCALE).toFloat()
                    )
                )

                // bottom, left: rotated template (red)
                paint.setARGB(255, 255, 0, 0)
                drawTrace(
                    t.vector, m.theta, DRAW_SCALE.toFloat(), PointF(
                        DRAW_SCALE.toFloat(), (2 * DRAW_SCALE).toFloat()
                    )
                )
                if (state == Modi.EVALUATION) {
                    val correctGesture =
                        t.gestureName == evaluationGestures[currentGestureToEvaluate]
                    if (correctGesture) {
                        playPositiveFeedback()
                    } else {
                        playNegativeFeedback()
                    }
                    promptEvaluationResult(t)
                    currentGestureToEvaluate++
                    if (currentGestureToEvaluate < evaluationGestures.size) {
                        promptEvaluation()
                        //go on to evaluate
                    } else {
                        state = Modi.FREERUNNING
                    }
                } else if (state == Modi.FREERUNNING) {
                    // text
                    paint.setARGB(255, 0, 0, 0)
                    val score = Math.round(1000 * m.score) / 1000.0f
                    val theta = Math.round(1000 * m.theta) / 1000.0f
                    val s = "id = " + m.template!!.id + ", score = " + score + ", theta = " + theta
                    canvas!!.drawText(s, 10f, 30f, paint)
                    canvas!!.drawText(GESTURE_SET[m.template!!.id], 10f, 60f, paint)
                }
            }
        }
        invalidate()
        return true
    }

    /**
     * Draw a trace, first rotate, scale, and translate.
     * @param trace
     * @param rotate
     * @param scale
     * @param translate
     */
    fun drawTrace(trace: Vector<PointF>, rotate: Float, scale: Float, translate: PointF?) {
        val n = trace.size
        if (n <= 0) return
        val drawTrace = recognizer.copy(trace)
        recognizer.rotate(drawTrace, rotate)
        recognizer.scale(drawTrace, scale)
        recognizer.translate(drawTrace, translate!!)
        var x = Math.round(drawTrace[0].x)
        var y = Math.round(drawTrace[0].y)
        var xp = x
        var yp = y
        canvas!!.drawCircle(x.toFloat(), y.toFloat(), 5f, paint)
        for (i in 1 until n) {
            x = Math.round(drawTrace[i].x)
            y = Math.round(drawTrace[i].y)
            canvas!!.drawLine(xp.toFloat(), yp.toFloat(), x.toFloat(), y.toFloat(), paint)
            canvas!!.drawCircle(x.toFloat(), y.toFloat(), 3f, paint)
            xp = x
            yp = y
        }
    }

    fun testGesturesWithNewParticipant() {
        if (state == Modi.TRAINING) {
            return
        }
        state = Modi.EVALUATION

        //Gesture Array
        evaluationGestures = arrayOfNulls(EVALUATIONS_PER_GESTURE * GESTURE_SET.size)
        currentGestureToEvaluate = 0

        //@todo: fill Array WithGesture Names
        for (i in evaluationGestures.indices) {
            evaluationGestures[i] = GESTURE_SET[i % GESTURE_SET.size]
        }


        // end
        evaluationGestures = randomizeGestureArray(evaluationGestures)
        clear()
        promptEvaluation()
        invalidate()
    }

    private fun randomizeGestureArray(gesturesToRandomize: Array<String?>): Array<String?> {
        gesturesToRandomize.shuffle() // shuffles in-place with default randomization
        return gesturesToRandomize
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun playPositiveFeedback() {
        vibrator.vibrate(VibrationEffect.createWaveform(positiveVibrationFeedback, -1))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playNegativeFeedback() {
        vibrator.vibrate(VibrationEffect.createWaveform(negativeVibrationFeedback, -1))
    }


    fun trainGestures() {
        state = Modi.TRAINING
        try {
            if (dout != null) {
                dout!!.close()
                dout = null
            }
            val fout = context.openFileOutput(gestureFileName, Context.MODE_PRIVATE)
            dout = DataOutputStream(fout)
        } catch (ex: IOException) {
            Log.d("SketchView", ex.message!!)
        }
        trace.clear()
        templates.clear()
        clear()
        prompt()
        invalidate()
    }

    companion object {
        // $1 gesture names
        // http://depts.washington.edu/aimgroup/proj/dollar/
        private val dollar1WebGestureNames = arrayOf(
            "triangle",
            "x",
            "rectangle",
            "circle",
            "check",
            "caret",
            "zig-zag",
            "arrow",
            "left square bracket",
            "right square bracket",
            "v",
            "delete",
            "left curly brace",
            "right curly brace",
            "star",
            "pigtail"
        )

        // $1 gesture names
        // gesture names from paper (slightly different from above)
        private val dollar1GestureNames = arrayOf(
            "triangle",
            "x",
            "rectangle",
            "circle",
            "check",
            "caret",
            "question",
            "arrow",
            "left square bracket",
            "right square bracket",
            "v",
            "delete",
            "left curly brace",
            "right curly brace",
            "star",
            "pigtail"
        )

        // test gesture names
        private val testGestureNames = arrayOf(
            "circle",
            "check",
            "triangle"
        )

        // @todo: new gesture set
        private val myGestureNames = arrayOf(
            "swipe_up",
            "swipe_down",
            "swipe_left",
            "swipe_right",
            "double_tap"
        )

        private val GESTURE_SET = testGestureNames // @todo: modify, set to your own gesture set
        private const val EXAMPLES_PER_GESTURE = 3
        private const val EVALUATIONS_PER_GESTURE = 10
        private const val DRAW_SCALE = 300
    }

    init {
        paint.isAntiAlias = false
        paint.setARGB(255, 255, 255, 255)
        paint.textSize = 30f
        try {
            val fin = c.openFileInput(gestureFileName)
            val din = DataInputStream(fin)
            try {
                while (true) {
                    val t = Template.readFrom(din)
                    templates.add(t)
                    Log.d("SketchView", "read template: $t")
                }
            } catch (exeof: EOFException) {
                Log.d("SketchView", "EOFException: " + exeof.message)
                exeof.printStackTrace()
            }
            din.close()
            fin.close()
        } catch (ex: IOException) {
            Log.d("SketchView", ex.message!!)
        }
        if (templates.size < GESTURE_SET.size * EXAMPLES_PER_GESTURE) {
            state = Modi.TRAINING
            try {
                val fout = c.openFileOutput(gestureFileName, Context.MODE_PRIVATE)
                dout = DataOutputStream(fout)
            } catch (ex: IOException) {
                Log.d("SketchView", ex.message!!)
            }
        } else {
            state = Modi.FREERUNNING
        }

        //@todo: instantiate Vibrator
        vibrator = c.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}