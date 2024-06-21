package de.luh.hci.mi.wifilocation.data

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.util.Log
import java.io.File

// An SQLite database that stores WiFi fingerprints.
// https://www.sqlite.org/lang.html
// officially: Use Room abstraction layer rather than SQLite directly.
// Room is an object-relational mapping library.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/training/data-storage/room
// https://developer.android.com/reference/android/database/sqlite/package-summary
class LocalDatabase(databaseFile: File) {

    // Represents the underlying SQLite database instance.
    private val db: SQLiteDatabase = openOrCreateDatabase(databaseFile, null)

    // Initializes the database object. Creates the tables, if not yet done.
    init {
        var sql = "CREATE TABLE IF NOT EXISTS fingerprint (" +
                "id INTEGER PRIMARY KEY, " +
                "location TEXT NOT NULL)"
        db.execSQL(sql)
        sql = "CREATE TABLE IF NOT EXISTS measurement (" +
                "id INTEGER PRIMARY KEY, " +
                "fingerprint_id INTEGER NOT NULL REFERENCES fingerprint(id) ON DELETE CASCADE, " +
                "bssid TEXT NOT NULL, " +
                "level INTEGER NOT NULL)"
        db.execSQL(sql)

        // delete all rows in both tables
        // db.delete("fingerprint", null, arrayOf())
        // db.delete("measurement", null, arrayOf())

        log("end LocalDatabase::init")
    }

    // Inserts a fingerprint into the database.
    fun insertFingerprint(fp: Fingerprint) {
        val values = ContentValues()
        values.put("location", fp.location)
        val id = db.insertWithOnConflict("fingerprint", null, values, CONFLICT_REPLACE)
        log("insert into fingerprint: id = $id")
        if (id == -1L) {
            throw android.database.SQLException("could not insert fingerprint")
        }
        for ((bssid, level) in fp.bss2level) {
            val measurementValue = ContentValues()
            measurementValue.put("fingerprint_id", id)
            measurementValue.put("bssid", bssid)
            measurementValue.put("level", level)
            val measurementId =
                db.insertWithOnConflict("measurement", null, measurementValue, CONFLICT_REPLACE)
            log("insert into measurement: id = $measurementId")
            if (measurementId == -1L) {
                throw android.database.SQLException("could not insert measurement")
            }
        }
        log("end LocalDatabase::insertFingerprint")
    }

    // Removes fingerprints for the given location from the database.
    fun deleteLocation(location: String) {
        /*
        var cursor = db.rawQuery("SELECT count(*) FROM fingerprint", arrayOf())
        while (cursor.moveToNext()) {
            val count = cursor.getInt(0)
            log("fp 1: $count")
        }
        cursor.close()
        cursor = db.rawQuery("SELECT count(*) FROM measurement", arrayOf())
        while (cursor.moveToNext()) {
            val count = cursor.getInt(0)
            log("mm 1: $count")
        }
        cursor.close()
        */

        // DELETE FROM measurement WHERE fingerprint_id IN (SELECT id FROM fingerprint WHERE location = ?)
        db.delete(
            "measurement",
            "fingerprint_id IN (SELECT id FROM fingerprint WHERE location = ?)",
            arrayOf(location)
        )

        // DELETE FROM fingerprint WHERE location = ?
        db.delete("fingerprint", "location = ?", arrayOf(location))
        // the state of the database may have changed, inform listeners

        /*
        cursor = db.rawQuery("SELECT count(*) FROM fingerprint", arrayOf())
        while (cursor.moveToNext()) {
            val count = cursor.getInt(0)
            log("fp 2: $count")
        }
        cursor.close()
        cursor = db.rawQuery("SELECT count(*) FROM measurement", arrayOf())
        while (cursor.moveToNext()) {
            val count = cursor.getInt(0)
            log("mm 2: $count")
        }
        cursor.close()
        */
    }

    // Returns the fingerprints in the database.
    fun getFingerprints(): List<Fingerprint> {
        val cursor1 = db.rawQuery("SELECT * FROM fingerprint ORDER BY location", arrayOf())
        // for (col in cursor1.columnNames) log(col)
        val fingerprints = mutableListOf<Fingerprint>()
        while (cursor1.moveToNext()) {
            val id = cursor1.getInt(0)
            val location = cursor1.getString(1)
            val cursor2 =
                db.rawQuery(
                    "SELECT * FROM measurement WHERE fingerprint_id = ? ORDER BY bssid",
                    arrayOf(id.toString())
                )
            // for (col in cursor2.columnNames) log(col)
            val bss2Level = mutableMapOf<String, Int>()
            while (cursor2.moveToNext()) {
                val bss = cursor2.getString(2)
                val level = cursor2.getInt(3)
                // log("$id '$location' $bss $level")
                bss2Level[bss] = level
            }
            cursor2.close()
            fingerprints.add(Fingerprint(location, bss2Level))
        }
        cursor1.close()
        // log("end LocalDatabase::getFingerprints")
        return fingerprints
    }

    // Returns the number of fingerprints for each location.
    fun getFingerprintCounts(): Map<String, Int> {
        val cursor =
            db.rawQuery("SELECT location, count(id) FROM fingerprint GROUP BY location", arrayOf())
        // for (col in cursor.columnNames) log(col)
        val counts = mutableMapOf<String, Int>()
        while (cursor.moveToNext()) {
            val location = cursor.getString(0)
            val count = cursor.getInt(1)
            // log("$location: $count")
            counts[location] = count
        }
        cursor.close()
        return counts
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}
