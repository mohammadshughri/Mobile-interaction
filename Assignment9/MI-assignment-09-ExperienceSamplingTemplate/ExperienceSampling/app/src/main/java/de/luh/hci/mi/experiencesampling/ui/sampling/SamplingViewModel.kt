package de.luh.hci.mi.experiencesampling.ui.sampling

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.experiencesampling.ExperienceSampling
import de.luh.hci.mi.experiencesampling.data.Item
import de.luh.hci.mi.experiencesampling.data.Rating
import de.luh.hci.mi.experiencesampling.data.Repository
import de.luh.hci.mi.experiencesampling.samplingInterval
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.math.roundToInt

class SamplingViewModel(
    private val app: ExperienceSampling,
    private val repository: Repository, // the underlying repository (data model)
    private val applicationScope: CoroutineScope,
) : ViewModel() {

    val contexts = repository.getContexts()
    val items = repository.getItems()

    var selectedContext by mutableStateOf(contexts[0])
        private set

    fun onContextSelected(context: String) {
        selectedContext = context
    }

    private val sliderStates = mutableMapOf<Item, MutableState<Float>>()

    fun sliderState(item: Item): MutableState<Float> {
        var slider = sliderStates[item]
        if (slider == null) {
            slider = mutableFloatStateOf(0f)
            sliderStates[item] = slider
        }
        return slider
    }

    fun submit() {
        val sample = items.map {
            Rating(it, sliderState(it).value.roundToInt().toFloat())
        }
        applicationScope.launch {
            try {
                repository.submitRatings(selectedContext, sample)
            } catch (ex: IOException) {
                log("exception trying to submit: $ex")
            }
        }
    }

    override fun onCleared() {
        log("onCleared")
        if (app.periodicSampling.value) {
            app.scheduleNextNotification(samplingInterval)
        }
    }

    private fun log(msg: String) {
        Log.d(this.javaClass.simpleName, msg)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExperienceSampling
                val repository = app.repository
                val scope = app.applicationScope
                SamplingViewModel(app, repository, scope)
            }
        }
    }
}
