package de.luh.hci.mi.experiencesampling.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Stores and retrieves JSON documents on/from the DB server.
 * http://couchdb.apache.org
 * https://www.json.org/json-en.html
 * http://developer.android.com/reference/org/json/JSONObject.html
 */
class CouchDB(private val dbUrl: String) {

    // Gets a JSON object from the server given a resource path.
    // Example path: _design/esm/_view/ctxt?key=%22Programmieren+1%22
    // Throws an exception on failure.
    suspend fun get(resourcePath: String): JSONObject =
        withContext(Dispatchers.IO) {
            Log.d(javaClass.simpleName, "get: " + Thread.currentThread().name)
            // delay(3000L) // simulate delay
            val url = URL(dbUrl + resourcePath)
            val con = url.openConnection() as HttpURLConnection
            try {
                val reader = BufferedReader(InputStreamReader(con.inputStream))
                val obj = JSONObject(reader.readText())
                reader.close()
                return@withContext obj
            } finally {
                con.disconnect()
            }
        }

    // Stores the given JSON object at the indicated location on the server.
    // Returns the server's response. Throws an exception on failure.
    suspend fun put(resourcePath: String, doc: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            Log.d(javaClass.simpleName, "put: " + Thread.currentThread().name)
            // delay(7000L) // simulate delay
            val url = URL(dbUrl + resourcePath)
            val con = url.openConnection() as HttpURLConnection
            try {
                con.doOutput = true
                con.requestMethod = "PUT"
                con.setChunkedStreamingMode(0) // write in chunks
                con.setRequestProperty("Content-type", "application/json")
                con.setRequestProperty("Accept", "application/json")
                val writer = BufferedWriter(OutputStreamWriter(con.outputStream))
                writer.write(doc.toString())
                writer.close()
                val reader = BufferedReader(InputStreamReader(con.inputStream))
                val obj = JSONObject(reader.readText())
                reader.close()
                return@withContext obj
            } finally {
                con.disconnect()
            }
        }
}