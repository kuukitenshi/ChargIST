package cmu.group2.chargist.data.model

data class User(
    val id: Long,
    val username: String,
    val name: String,
    val pictureUrl: String?,
    val isGuest: Boolean = false
)

val GuestUser =
    User(-1, "guest", "Guest User", null, true)