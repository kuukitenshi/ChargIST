package cmu.group2.chargist.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cmu.group2.chargist.data.model.User

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Long,
    val username: String,
    val name: String,
    val pictureUrl: String?,
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        username = username,
        name = name,
        pictureUrl = pictureUrl,
        isGuest = false
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        username = username,
        name = name,
        pictureUrl = pictureUrl
    )
}