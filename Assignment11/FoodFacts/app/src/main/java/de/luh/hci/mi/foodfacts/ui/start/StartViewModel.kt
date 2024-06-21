package de.luh.hci.mi.foodfacts.ui.start

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.foodfacts.FoodFacts
import de.luh.hci.mi.foodfacts.data.Repository

// ViewModel for the start screen.
class StartViewModel(
    private val repository: Repository, // the underlying repository (data model)
) : ViewModel() {

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
                val app = this[APPLICATION_KEY] as FoodFacts
                val repository = app.repository
                StartViewModel(repository)
            }
        }
    }

}