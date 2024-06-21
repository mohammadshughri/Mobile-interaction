package de.luh.hci.mi.wifilocation.data

// Groups a location and a distance measure.
data class LocationDistance(val location: String, val distance: Double) {
    override fun toString(): String {
        return "$location: $distance"
    }
}