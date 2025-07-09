package cmu.group2.chargist.data.supabase.dto

import cmu.group2.chargist.data.database.entity.UserEntity
import cmu.group2.chargist.data.model.User
import kotlinx.serialization.Serializable

@Serializable
data class SupaUser(
    val id: Long? = null,
    val username: String,
    val name: String,
    val password: String,
    val pictureUrl: String?
)

fun SupaUser.toEntity(): UserEntity {
    return UserEntity(
        id = id!!,
        username = username,
        name = name,
        pictureUrl = pictureUrl
    )
}

fun SupaUser.toDomain(): User {
    return User(
        id = id!!,
        username = username,
        name = name,
        pictureUrl = pictureUrl
    )
}
