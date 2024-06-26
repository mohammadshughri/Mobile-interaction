package de.luh.hci.mi.foodfacts.ui.product

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.luh.hci.mi.foodfacts.FoodFacts
import de.luh.hci.mi.foodfacts.data.Repository
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: Repository, // the underlying repository (data model)
    savedStateHandle: SavedStateHandle // a map that contains the barcode
) : ViewModel() {

    val barcode: String

    var name by mutableStateOf("")
        private set

    var quantity by mutableStateOf("")
        private set

    var nutriments by mutableStateOf("") // variable to hold nutritional information
        private set

    var imageUrl by mutableStateOf("") // new variable to hold image URL
        private set

    var allergens by mutableStateOf("") // new variable to hold allergens
        private set

    init {
        barcode = savedStateHandle.get<String>("barcode") ?: ""
        log("barcode = $barcode")
        if (barcode.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val obj = repository.getProduct(barcode)
                    log(obj.toString(2))
                    val product = obj.getJSONObject("product")
                    name = product.getString("product_name_de").ifEmpty {
                        product.getString("product_name")
                    }
                    quantity = product.getString("quantity")
                    nutriments = product.getJSONObject("nutriments")
                        .toString() // fetch and store nutritional information
                    imageUrl = product.getString("image_front_url") // fetch and store image URL
                    allergens = product.optString("allergens") // fetch and store allergens
                } catch (ex: Exception) {
                    name = ex.toString()
                    quantity = ""
                    nutriments = ""
                    imageUrl = ""
                    allergens = ""
                }
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
                val app = this[APPLICATION_KEY] as FoodFacts
                val repository = app.repository
                val savedStateHandle = this.createSavedStateHandle()
                ProductViewModel(repository, savedStateHandle)
            }
        }
    }
}
