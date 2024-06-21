package de.luh.hci.mi.wifilocation.ui.start

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.wifilocation.WifiLocation
import de.luh.hci.mi.wifilocation.data.Repository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

// ViewModel for the start screen.
class StartViewModel(
    private val repository: Repository, // the underlying repository (data model)
    private val context: Context // add context to the constructor
) : ViewModel() {

    fun logFingerprints() {
        viewModelScope.launch {
            val fingerprints = repository.getFingerprints()
            val logFile = File(context.getExternalFilesDir(null), "fingerprints.txt")
            try {
                FileOutputStream(logFile).use { output ->
                    for (fp in fingerprints) {
                        output.write((fp.toString() + "\n").toByteArray())
                    }
                }
                log("Fingerprints logged to ${logFile.absolutePath}")
            } catch (e: Exception) {
                log("Error logging fingerprints: ${e.message}")
            }
        }
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as WifiLocation
                val repository = app.repository
                StartViewModel(repository, app.applicationContext)
            }
        }
    }
}
