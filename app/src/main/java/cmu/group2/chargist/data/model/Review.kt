package cmu.group2.chargist.data.model

import java.util.Date

data class Review(
    val user: User,
    val rating: Int,
    val comment: String,
    val date: Date
)