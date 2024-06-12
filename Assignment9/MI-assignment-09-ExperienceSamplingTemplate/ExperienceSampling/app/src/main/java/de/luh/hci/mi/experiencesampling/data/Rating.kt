package de.luh.hci.mi.experiencesampling.data

// A questionnaire item.
data class Item(
    val title: String,
    val question: String,
    val negativeEnd: String,
    val positiveEnd: String
)

// A rating associated with a questionnaire item.
data class Rating(
    val item: Item,
    val value: Float
)
