package de.luh.hci.mi.foodfacts.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

// Singleton object to access OpenFoodFacts Web database.
// https://world.openfoodfacts.org/api/v0/product/8715700110103.json
// https://wiki.openfoodfacts.org/API/Full_JSON_example
// https://github.com/openfoodfacts/openfoodfacts-go/blob/develop/product.go
object OpenFoodFacts {

    // The URL of the server.
    private const val URL = "https://world.openfoodfacts.org/api/v0/product/"

    // Looked up products are cached to avoid unnecessary server requests.
    // Maps EANs (as strings) to JSON objects.
    private val cache = mutableMapOf<String, JSONObject>()

    // Creates a URL from an EAN.
    private fun makeURL(ean: String): URL {
        return URL("$URL$ean.json")
    }

    // Gets the JSON document of a product from the server.
    // Throws an exception on failure.
    suspend fun get(ean: String): JSONObject =
        withContext(Dispatchers.IO) {
            // is the EAN in the cache?
            val cached = cache[ean]
            if (cached != null) {
                return@withContext cached
            } else {
                // if not in cache, ask server
                log("get from server with thread " + Thread.currentThread().name)
                val url = makeURL(ean)
                log(url.toString())
                val con = url.openConnection() as HttpURLConnection
                try {
                    val reader = BufferedReader(InputStreamReader(con.inputStream))
                    val obj = JSONObject(reader.readText())
                    reader.close()
                    cache[ean] = obj // insert into cache
                    return@withContext obj
                } finally {
                    con.disconnect()
                }
            }
        }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }
}