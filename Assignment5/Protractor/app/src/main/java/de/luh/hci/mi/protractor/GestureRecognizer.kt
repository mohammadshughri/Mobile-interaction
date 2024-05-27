package de.luh.hci.mi.protractor

import android.graphics.PointF
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GestureRecognizer {

    fun recognize(trace: Vector<PointF>, templates: Vector<Template>): Match? {
        val resampledTrace = resample(trace, SAMPLE_POINTS_COUNT)
        val c = centroid(resampledTrace)
        c.x = -c.x
        c.y = -c.y
        translate(resampledTrace, c) // translate to origin
        normalize(resampledTrace) // not strictly necessary, will be normalized after rotation anyway
        var bestMatch: Match? = null
        for (t in templates) {
            val m = optimalAngle(resampledTrace, t.vector)
            if (bestMatch == null || m.score > bestMatch.score) {
                m.template = t
                bestMatch = m
            }
        }
        return bestMatch
    }

    /** Resample trace to n points. Linear interpolation.  */
    fun resample(trace: Vector<PointF>, n: Int): Vector<PointF> {
        var n = n
        if (n < 0) n = 0
        val newTrace = Vector<PointF>(n)

        // return empty vector if no points in trace
        val m = trace.size
        if (m <= 0) return newTrace

        // trace has only a single point
        if (m == 1) {
            val p = trace[0]
            val i = 0
            while (i < n) {
                newTrace.add(p)
            }
            return newTrace
        }

        // at least 2 points in trace
        val I = pathLength(trace) / (n - 1)
        var D = 0f
        var pp = trace[0]
        newTrace.add(trace[0]) // add first point of original trace
        var i = 1
        while (i < m && newTrace.size < n - 1) {
            var p = trace[i]
            val d = distance(pp, p)
            if (d > 0 && D + d > I) {
                val delta = (I - D) / d
                p = PointF(pp.x + delta * (p.x - pp.x), pp.y + delta * (p.y - pp.y))
                newTrace.add(p)
                D = 0f
                i--
            } else {
                D += d
            }
            pp = p
            i++
        }
        newTrace.add(trace[m - 1]) // add last point of original trace
        return newTrace
    }

    /**
     * Length of the trace as sum of distances between points.
     * @param trace
     * @return length of the trace
     */
    fun pathLength(trace: Vector<PointF>): Float {
        var length = 0f
        for (i in 1 until trace.size) {
            length += distance(trace[i - 1], trace[i])
        }
        return length
    }

    /**
     * Euclidean distance between points a and b.
     * @param a
     * @param b
     * @return distance between points a and b
     */
    fun distance(a: PointF, b: PointF): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    /**
     * The centroid is the point (average x-coordinate, average y-coordinate).
     * @param points
     * @return centroid of points
     */
    fun centroid(points: Vector<PointF>): PointF {
        var cx = 0f
        var cy = 0f
        for (p in points) {
            cx += p.x
            cy += p.y
        }
        val count = points.size
        cx /= count
        cy /= count
        return PointF(cx, cy)
    }


    /**
     * Move every point by the same amount.
     * @param points
     * @param translationVector
     */
    fun translate(points: Vector<PointF>, translationVector: PointF) {
        for (p in points) {
            p.x += translationVector.x
            p.y += translationVector.y
        }
    }


    /**
     * Rotate points around origin by angle theta
     * @param points
     * @param theta
     */
    fun rotate(points: Vector<PointF>, theta: Float) {
        val cosTheta = cos(theta.toDouble()).toFloat()
        val sinTheta = sin(theta.toDouble()).toFloat()

        for (p in points) {
            val x = p.x * cosTheta - p.y * sinTheta
            val y = p.x * sinTheta + p.y * cosTheta
            p.x = x
            p.y = y
        }
    }

    /**
     * Consider the argument vector of n 2D-points as a float vector of 2n floats (R^(2n)).
     * Normalize the argument vector of 2n floats to a unit vector (of dimension 2n).
     * @param points
     */
    fun normalize(points: Vector<PointF>) {
        var mag = 0f
        for (p in points) {
            mag += p.x * p.x + p.y * p.y
        }
        mag = sqrt(mag.toDouble()).toFloat()
        if (mag.toDouble() != 0.0) mag = 1.0f / mag
        for (p in points) {
            p.x *= mag
            p.y *= mag
        }
    }

    /**
     * Scale by factor. Multiply every point by factor.
     * @param points
     * @param factor
     */
    fun scale(points: Vector<PointF>, factor: Float) {
        for (p in points) {
            p.x *= factor
            p.y *= factor
        }
    }

    /**
     * Deep copy of a trace.
     * @param points
     * @return deep copy of argument
     */
    fun copy(points: Vector<PointF>): Vector<PointF> {
        val n = points.size
        val c = Vector<PointF>(n)
        for (p in points) {
            c.add(PointF(p.x, p.y))
        }
        return c
    }

    /**
     * Find optimal angle between normalized vectors t and g, both of dimension n.
     * @param t template vector points, normalized
     * @param g input gesture vector points, normalized
     * @return optimal angle (and score) between g and t
     */
    fun optimalAngle(g: Vector<PointF>, t: Vector<PointF>): Match {
        var a = 0f
        var b = 0f

        for (i in g.indices) {
            val gx = g[i].x
            val gy = g[i].y
            val tx = t[i].x
            val ty = t[i].y

            a += gx * tx + gy * ty  // Cosine component
            b += gy * tx - gx * ty  // Sine component
        }

        val theta = atan2(b.toDouble(), a.toDouble()).toFloat()
        val score = a * cos(theta) + b * sin(theta)  // Maximum dot product

        return Match(score, theta)
    }

    companion object {
        const val SAMPLE_POINTS_COUNT = 16
    }
}