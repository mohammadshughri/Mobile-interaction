package de.luh.hci.mi.experiencesampling.data

// A repository is an interface to (a category of) the app's data.
// The repository interface isolates the data layer from the rest of the app.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
interface Repository {

    // Contexts a user may be in.
    fun getContexts(): List<String>

    // Questionnaire items.
    fun getItems(): List<Item>

    // Submits the list of ratings. May throw an IOException.
    suspend fun submitRatings(context: String, ratings: List<Rating>)

    // Retrieves the ratings for the given context. May throw an IOException.
    suspend fun getRatings(context: String): Map<Item, Float>
}
