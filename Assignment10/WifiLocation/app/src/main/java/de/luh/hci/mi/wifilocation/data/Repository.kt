package de.luh.hci.mi.wifilocation.data

// A repository is an interface to (a category of) the app's data.
// The repository interface isolates the data layer from the rest of the app.
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
interface Repository {

    // Locations a user may be at.
    fun getLocations(): List<String>

    // Submits a WiFi fingerprint. May throw an IOException.
    suspend fun storeFingerprint(fp: Fingerprint)

    // Retrieves all fingerprints. May throw an IOException.
    suspend fun getFingerprints(): List<Fingerprint>

    // Deletes the fingerprints for the given location. May throw an IOException.
    suspend fun deleteLocation(location: String)

    // Returns the number of fingerprints for each location.
    suspend fun getFingerprintCounts(): Map<String, Int>
}
