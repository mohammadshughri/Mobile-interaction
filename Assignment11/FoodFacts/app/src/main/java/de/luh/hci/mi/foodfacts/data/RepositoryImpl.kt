package de.luh.hci.mi.foodfacts.data

import org.json.JSONObject

// Implementation of the repository interface. It relies on the OpenFoodFacts database as a
// data source, which is provided via the constructor (dependency injection).
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
class RepositoryImpl(
    private val openFoodFacts: OpenFoodFacts
) : Repository {

    override suspend fun getProduct(ean: String): JSONObject {
        return openFoodFacts.get(ean)
    }

}