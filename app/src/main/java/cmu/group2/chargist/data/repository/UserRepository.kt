package cmu.group2.chargist.data.repository

import android.util.Log
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.database.entity.toDomain
import cmu.group2.chargist.data.database.entity.toEntity
import cmu.group2.chargist.data.model.GuestUser
import cmu.group2.chargist.data.model.User
import cmu.group2.chargist.data.model.UserNotFoundException
import cmu.group2.chargist.data.model.WrongPasswordException
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaFavorite
import cmu.group2.chargist.data.supabase.dto.SupaUser
import cmu.group2.chargist.data.supabase.dto.toDomain
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table
import cmu.group2.chargist.hashPassword
import cmu.group2.chargist.verifyPassword
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

object UserRepository {
    private val userDao by lazy { AppDatabase.getDatabase().userDao() }

    private val _currentUserId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser = _currentUserId.flatMapLatest { userId ->
        userId?.let {
            if (it == GuestUser.id) {
                flow {
                    emit(GuestUser)
                }
            } else {
                userDao.getUserByIdFlow(it).map { it?.toDomain() }
            }
        } ?: flow {
            emit(null)
        }
    }

    suspend fun setLoggedInUser(user: User) {
        userDao.insert(user.toEntity())
        _currentUserId.update { user.id }
        Log.d("UserRepository", "User set in the repository: ${user.username}")
    }

    suspend fun changeName(user: User, newName: String) {
        val supaUser = Supabase.table(SupaTable.Users).update({
            set("name", newName)
        }) {
            filter {
                eq("id", user.id)
            }
            select()
        }.decodeSingle<SupaUser>()
        userDao.update(supaUser.toEntity())
    }

    suspend fun changePicture(user: User, bytes: ByteArray?) {
        val path = "avatars/" + user.id.toString() + ".bmp"
        val bucket = Supabase.storage.from("chargist")
        var url: String? = null
        bytes?.let {
            Log.d("UserPicture", "Uploading to bucket")
            bucket.upload(path, it) {
                upsert = true
            }
            Log.d("UserPicture", "Getting public url")
            url = Supabase.storage.from("chargist").publicUrl(path)
        }
        Log.d("UserPicture", "Updating supa user")
        val result = Supabase.table(SupaTable.Users).update({
            set("pictureUrl", url)
        }) {
            filter {
                eq("id", user.id)
            }
            select()
        }.decodeSingle<SupaUser>()
        Log.d("UserPicture", "Updating local user")
        userDao.update(result.toEntity())
    }

    fun logout() {
        _currentUserId.update { null }
    }

    // Authentication
    suspend fun refreshUserSession(userId: Long) {
        if (userId == GuestUser.id) {
            Log.d("SessionRefresh", "Returning as Guest")
            setLoggedInUser(GuestUser)
            return
        }
        Log.d("SessionRefresh", "Checking local user for id: $userId")
        val localUser = userDao.getUserById(userId)
        if (localUser != null) {
            Log.d("SessionRefresh", "Returning as local user ${localUser.username}")
            setLoggedInUser(localUser.toDomain())
            return
        }
        try {
            Log.d("SessionRefresh", "Checking online user for id: $userId")
            val user = Supabase.table(SupaTable.Users)
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<SupaUser>()
            setLoggedInUser(user.toDomain())
            Log.d("SessionRefresh", "Returning as online fetch user ${user.username}")
        } catch (_: Exception) {
            throw UserNotFoundException()
        }
    }

    suspend fun login(username: String, password: String) {
        return try {
            val user = Supabase.table(SupaTable.Users).select {
                filter {
                    eq("username", username)
                }
            }.decodeSingle<SupaUser>()
            Log.d("Argon", "verify $password and ${user.password}")
            if (!verifyPassword(password, user.password)) {
                throw WrongPasswordException()
            }
            setLoggedInUser(user.toDomain())
        } catch (_: Exception) {
            throw UserNotFoundException()
        }
    }

    suspend fun register(
        username: String,
        name: String,
        password: String,
        favorites: List<Long>
    ) {
        val request = SupaUser(
            username = username,
            name = name,
            password = hashPassword(password),
            pictureUrl = null
        )
        val user = Supabase.table(SupaTable.Users).insert(request) {
            select()
        }.decodeSingle<SupaUser>()
        try {
            val favs = favorites.map {
                SupaFavorite(
                    stationId = it,
                    userId = user.id!!
                )
            }
            if (favs.isNotEmpty()) {
                Supabase.table(SupaTable.Favorites).insert(favs)
            }
        } catch (_: Exception) {
        }
        setLoggedInUser(user.toDomain())
    }
}
