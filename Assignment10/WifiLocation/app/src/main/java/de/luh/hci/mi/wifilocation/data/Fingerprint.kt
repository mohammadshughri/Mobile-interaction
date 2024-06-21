package de.luh.hci.mi.wifilocation.data

import android.net.wifi.ScanResult

// Represents a vector of pairs (BSS MAC address, received signal strength) for a specific location.
class Fingerprint {

    // Location of this fingerprint.
    val location: String

    // Map of base station (BSS) IDs to signal strength levels.
    val bss2level: Map<String, Int>

    constructor(location: String, scanResults: List<ScanResult>) {
        this.location = location
        val bss2level = mutableMapOf<String, Int>()
        for (sr in scanResults) {
            bss2level[sr.BSSID] = sr.level
        }
        this.bss2level = bss2level
    }

    constructor(location: String, bss2level: Map<String, Int>) {
        this.location = location
        this.bss2level = bss2level
    }

    // Computes the (squared) distance of this fingerprint to another fingerprint. Ignores IDs that
    // are in the other fingerprint, but not in this fingerprint. For IDs that are in this
    // fingerprint but not in the other one, sets a hypothetic level of -100 (just outside range).
    fun distance(other: Fingerprint): Double {
        var d = 0.0
        for ((id, level) in bss2level) {
            val otherLevel = other.bss2level[id] ?: -100
            val diff = level - otherLevel
            d += (diff * diff).toDouble()
        }
        return d
    }

    // String representation: "location, id1 : level1, id2 : level2, ..., idn : leveln"
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(location)
        for ((id, level) in bss2level) {
            sb.append(", ")
            sb.append(id)
            sb.append(" : ")
            sb.append(level)
        }
        return sb.toString()
    }
}