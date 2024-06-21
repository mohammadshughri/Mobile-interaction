package de.luh.hci.mi.foodfacts.data

import org.json.JSONObject

// A repository is an interface to (a category of) the app's data.
// The repository interface isolates the data layer from the rest of the app.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
interface Repository {
    suspend fun getProduct(ean: String): JSONObject
}
