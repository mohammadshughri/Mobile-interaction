package de.luh.hci.mi.experiencesampling.data

import android.content.Context
import android.util.Log
import de.luh.hci.mi.experiencesampling.R
import de.luh.hci.mi.experiencesampling.experimenter
import org.json.JSONObject
import java.net.URLEncoder

// Implementation of the repository interface. It relies on a database as a data source,
// which is provided via the constructor (dependency injection).
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
class RepositoryImpl(
    context: Context, // application context (not the user's context)
    private val db: CouchDB
) : Repository {

    // Fixed list of contexts a user may be in.
    private val contexts = listOf(
        context.getString(R.string.context_1),
        context.getString(R.string.context_2),
        context.getString(R.string.context_3)
    )

    // Fixed list of questionnaire items.
    private val items = listOf(
        Item(
            context.getString(R.string.effectiveness),
            context.getString(R.string.effectiveness_question),
            context.getString(R.string.very_bad),
            context.getString(R.string.very_good)
        ),
        Item(
            context.getString(R.string.clearness),
            context.getString(R.string.clearness_question),
            context.getString(R.string.very_bad),
            context.getString(R.string.very_good)
        ),
        Item(
            context.getString(R.string.exciting),
            context.getString(R.string.exciting_question),
            context.getString(R.string.very_bad),
            context.getString(R.string.very_good)
        ),
        Item(
            context.getString(R.string.predictable),
            context.getString(R.string.predictable_question),
            context.getString(R.string.very_bad),
            context.getString(R.string.very_good)
        ),
        Item(
            context.getString(R.string.additional),
            context.getString(R.string.additional_question),
            context.getString(R.string.very_bad),
            context.getString(R.string.very_good)
        ),
        Item(
            context.getString(R.string.additional2),
            context.getString(R.string.additional_question2),
            context.getString(R.string.very_bad),
            context.getString(R.string.very_good)
        )
    )

    override fun getContexts(): List<String> {
        return contexts
    }

    override fun getItems(): List<Item> {
        return items
    }

    override suspend fun submitRatings(context: String, ratings: List<Rating>) {
        Log.d(javaClass.simpleName, "submitRatings: " + Thread.currentThread().name)
        // store in online database
        // create JSON document and send to server
        val obj = JSONObject()
        // timestamp
        val time = System.currentTimeMillis()
        obj.put("time", time)
        obj.put("experimenter", experimenter)
        obj.put("context", context)
        for (rating in ratings) {
            obj.put(rating.item.title, rating.value)
        }
        Log.d(javaClass.simpleName, obj.toString())
        val docId = "esm$time"
        val response = db.put(docId, obj)
        Log.d(javaClass.simpleName, response.toString())
    }

    /*
    Example query:
    https://couchdb.hci.uni-hannover.de/mobint/_design/esm/_view/ctxtexp?key=["Mobile Interaktion","MyExperimenterId"]

    Example get response:
    {"total_rows":8,"offset":0,"rows":[
    {"id":"esm1686831673152","key":"Mensa","value":{"_id":"esm1686831673152","_rev":"1-dacc2a8f08698a838d291870f737f282","time":1686831673152,"experimenter":"MyExperimenterId","context":"Mensa","Mood":5,"Understanding":4,"Difficulty":3}},
    {"id":"esm1686834330948","key":"Mensa","value":{"_id":"esm1686834330948","_rev":"1-bceece5c4ae08cdf34dd5f2f2ff0ee5a","time":1686834330948,"experimenter":"MyExperimenterId","context":"Mensa","Mood":1,"Understanding":1,"Difficulty":1}}
    ]}
    */

    override suspend fun getRatings(context: String): Map<Item, Float> {
        // query context for all experimenters
        // val key = URLEncoder.encode("\"$context\"", "UTF-8")
        // val query = "_design/esm/_view/ctxt?key=$key"

        // query context for one experimenter only
        val key = URLEncoder.encode("[\"$context\",\"$experimenter\"]", "UTF-8")
        val query = "_design/esm/_view/ctxtexp?key=$key"
        Log.d(javaClass.simpleName, query)
        val obj = db.get(query)

        // compute the average ratings per item from the set of retrieved values
        val averageRatings: MutableMap<Item, Float> = mutableMapOf()
        val rows = obj.getJSONArray("rows")
        val n = rows.length()
        if (n > 0) {
            for (i in 0 until n) {
                val row = rows.getJSONObject(i).getJSONObject("value")
                Log.d(javaClass.simpleName, "row: $row")
                for (item in getItems()) {
                    if (row.has(item.title)) {
                        val rating = row.getDouble(item.title).toFloat()
                        val sum = averageRatings[item] ?: 0f
                        averageRatings[item] = sum + rating
                    } else {
                        Log.d(javaClass.simpleName, "No value for ${item.title} in JSON data")
                    }
                }
            }
            for (item in getItems()) {
                val sum = averageRatings[item] ?: 0f
                averageRatings[item] = sum / n
            }
        }
        return averageRatings
    }
}