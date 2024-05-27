package de.luh.hci.mi.protractor

import android.graphics.PointF
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*
import kotlin.Throws

class Template(val id: Int, val gestureName: String, var vector: Vector<PointF>) {

    @Throws(IOException::class)
    fun writeTo(out: DataOutputStream) {
        out.writeInt(id)
        out.writeInt(vector.size)
        for (p in vector) {
            out.writeFloat(p.x)
            out.writeFloat(p.y)
        }
        out.writeInt(gestureName.toByteArray().size)
        out.write(gestureName.toByteArray())
    }

    override fun toString(): String {
        val sb = StringBuffer()
        sb.append("[id = $id, name = $gestureName, vector = ")
        if (vector.size <= 0) {
            sb.append("(null)")
        } else {
            sb.append(vector[0])
            for (i in 1 until vector.size) {
                sb.append(", " + vector[i])
            }
        }
        sb.append("]")
        return sb.toString()
    }

    companion object {
        @Throws(IOException::class)
        fun readFrom(din: DataInputStream): Template {
            val id = din.readInt()
            var n = din.readInt()
            val points = Vector<PointF>()
            for (i in 0 until n) {
                val p = PointF()
                p.x = din.readFloat()
                p.y = din.readFloat()
                points.add(p)
            }
            n = din.readInt()
            val gestureName = ByteArray(n)
            for (i in 0 until n) {
                gestureName[i] = din.readByte()
            }
            return Template(id, String(gestureName), points)
        }
    }
}