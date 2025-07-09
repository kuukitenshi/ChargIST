package cmu.group2.chargist.data.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation
import cmu.group2.chargist.data.model.Review
import java.util.Date

@Entity(tableName = "reviews", primaryKeys = ["stationId", "userId"])
data class ReviewEntity(
    val stationId: Long,
    val userId: Long,
    val rating: Int,
    val comment: String,
    val date: Long
)

data class ReviewWithUserEntity(
    @Embedded val review: ReviewEntity,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)

fun ReviewWithUserEntity.toDomain(): Review {
    return Review(
        user = user.toDomain(),
        rating = review.rating,
        comment = review.comment,
        date = Date(review.date)
    )
}

fun Review.toEntity(stationId: Long): ReviewEntity {
    return ReviewEntity(
        stationId = stationId,
        userId = user.id,
        rating = rating,
        comment = comment,
        date = date.time
    )
}
