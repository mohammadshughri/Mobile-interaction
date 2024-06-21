package de.luh.hci.mi.wifilocation.data

import android.util.Log

// Implementation of the repository interface. It relies on a database as a data source,
// which is provided via the constructor (dependency injection).
// https://developer.android.com/topic/architecture/data-layer
// https://developer.android.com/codelabs/basic-android-kotlin-training-repository-pattern
class RepositoryImpl(
    private val localDb: LocalDatabase,
) : Repository {

    // Fixed list of locations.
    private val locations = listOf("Location 1", "Location 2", "Location 3", "Location 4")

    override fun getLocations(): List<String> {
        return locations
    }

    override suspend fun storeFingerprint(fp: Fingerprint) {
        log("storeFingerprint: " + Thread.currentThread().name)
        localDb.insertFingerprint(fp)
    }

    override suspend fun getFingerprints(): List<Fingerprint> {
        val fingerprints = localDb.getFingerprints()
        log("${fingerprints.size} fingerprints")
        // for (fp in fingerprints) log(fp.toString())
        return fingerprints
    }

    override suspend fun deleteLocation(location: String) {
        localDb.deleteLocation(location)
    }

    override suspend fun getFingerprintCounts(): Map<String, Int> {
        return localDb.getFingerprintCounts()
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

}