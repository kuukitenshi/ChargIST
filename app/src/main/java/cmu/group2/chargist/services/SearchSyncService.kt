package cmu.group2.chargist.services

import android.util.Log
import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaStation
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchSyncService : AbstractCoroutineService() {
    private val stationDao by lazy { AppDatabase.getDatabase().stationDao() }

    companion object {
        private val _searchQuery = MutableStateFlow("")
        val searchQuery = _searchQuery.asStateFlow()

        fun updateSearchQuery(query: String) {
            _searchQuery.update { query }
        }
    }

    init {
        serviceScope.launch {
            searchQuery.collect { query ->
                if (query.isNotEmpty() && query.isNotBlank()) {
                    try {
                        val stations = Supabase.table(SupaTable.Stations).select {
                            filter {
                                like("name", "%$query%")
                            }
                        }.decodeList<SupaStation>()
                        Log.d("servsearch", stations.toString())
                        stations.forEach { stationDao.insert(it.toEntity()) }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}