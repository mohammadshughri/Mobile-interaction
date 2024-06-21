package de.luh.hci.mi.foodfacts

import android.app.Application
import de.luh.hci.mi.foodfacts.data.OpenFoodFacts
import de.luh.hci.mi.foodfacts.data.Repository
import de.luh.hci.mi.foodfacts.data.RepositoryImpl

// The application instance exists as long as the app executes and is therefore used for app-wide
// data that is used in activities.
class FoodFacts : Application() {
    // officially: use Dagger-Hilt dependency injection, rather than manual dependency injection
    // https://developer.android.com/training/dependency-injection
    // https://developer.android.com/training/dependency-injection/manual

    val repository: Repository = RepositoryImpl(OpenFoodFacts)

}