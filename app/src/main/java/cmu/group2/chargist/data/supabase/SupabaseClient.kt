package cmu.group2.chargist.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.PostgrestQueryBuilder
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

val Supabase by lazy {
    createSupabaseClient(
        supabaseUrl = "https://fobiyjwzhsdchzzixdea.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZvYml5and6aHNkY2h6eml4ZGVhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDg1NDUzNDMsImV4cCI6MjA2NDEyMTM0M30.9vNhSiyM9cFe7Qr7JVQBxOfiMutPauYJwibR-nW6zCA"
    ) {
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}

fun SupabaseClient.table(table: SupaTable): PostgrestQueryBuilder {
    return from(table.name)
}

sealed class SupaTable(val name: String) {
    data object Chargers : SupaTable("chargers")
    data object Favorites : SupaTable("favorite_stations")
    data object Reviews : SupaTable("reviews")
    data object Stations : SupaTable("stations")
    data object Users : SupaTable("users")
}