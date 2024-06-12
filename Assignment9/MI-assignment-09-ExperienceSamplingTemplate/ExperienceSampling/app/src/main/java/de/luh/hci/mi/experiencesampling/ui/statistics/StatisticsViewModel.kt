package de.luh.hci.mi.experiencesampling.ui.statistics

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.experiencesampling.ExperienceSampling
import de.luh.hci.mi.experiencesampling.data.Item
import de.luh.hci.mi.experiencesampling.data.Repository
import kotlinx.coroutines.launch
import java.io.IOException

class StatisticsViewModel constructor(
    private val repository: Repository, // the underlying repository (data model)
) : ViewModel() {

    val contexts = repository.getContexts()
    val items = repository.getItems()

    var selectedContext by mutableStateOf(contexts[0])
        private set

    private var ratings: Map<Item, Float> = mapOf()
    private val ratingStates = mutableMapOf<Item, MutableState<Float>>()

    init {
        viewModelScope.launch {
            try {
                ratings = repository.getRatings(selectedContext)
                for (item in items) {
                    val rs = ratingState(item)
                    rs.value = ratings[item] ?: 0f
                }
            } catch (ex: IOException) {
                log("exception trying to get ratings (init): $ex")
            }
        }
    }

    fun onContextSelected(context: String) {
        if (context != selectedContext) {
            selectedContext = context
            viewModelScope.launch {
                try {
                    ratings = repository.getRatings(context)
                    for (item in items) {
                        val rs = ratingState(item)
                        rs.value = ratings[item] ?: 0f
                    }
                } catch (ex: IOException) {
                    log("exception trying to get ratings (onContextSelected): $ex")
                }
            }
        }
    }

    fun ratingState(item: Item): MutableState<Float> {
        var rating = ratingStates[item]
        if (rating == null) {
            rating = mutableFloatStateOf(0f)
            ratingStates[item] = rating
        }
        return rating
    }

    override fun onCleared() {
        log("onCleared")
        ratings = mapOf()
        ratingStates.clear()
    }

    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExperienceSampling
                val repository = app.repository
                StatisticsViewModel(repository)
            }
        }
    }
}
