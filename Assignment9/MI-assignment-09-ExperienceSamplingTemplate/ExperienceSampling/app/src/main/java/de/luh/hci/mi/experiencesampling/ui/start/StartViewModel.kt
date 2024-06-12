package de.luh.hci.mi.experiencesampling.ui.start

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.experiencesampling.ExperienceSampling
import de.luh.hci.mi.experiencesampling.data.Repository
import de.luh.hci.mi.experiencesampling.samplingInterval

// ViewModel for the start screen.
class StartViewModel constructor(
    private val app: ExperienceSampling,
    private val repository: Repository, // the underlying repository (data model)
) : ViewModel() {

    var periodicSampling by app.periodicSampling

    fun togglePeriodicSampling() {
        if (app.periodicSampling.value) {
            log("stop sampling")
            app.periodicSampling.value = false
            app.cancelNotification()
        } else {
            log("start sampling")
            app.scheduleNextNotification(samplingInterval)
            app.periodicSampling.value = true
        }
    }

    // Called when this ViewModel is no longer used and will be destroyed. Can be used for cleanup.
    override fun onCleared() {
        log("onCleared")
    }

    // Logs a debug message.
    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    companion object {
        // Companion object for creating the view model in the right lifecycle scope.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExperienceSampling
                val repository = app.repository
                StartViewModel(app, repository)
            }
        }
    }

}