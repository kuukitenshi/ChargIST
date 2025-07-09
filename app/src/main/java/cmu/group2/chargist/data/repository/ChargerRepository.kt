package cmu.group2.chargist.data.repository

import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.model.Charger
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaCharger
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table

object ChargerRepository {
    private val chargerDao by lazy { AppDatabase.getDatabase().chargerDao() }

    suspend fun updateCharger(charger: Charger) {
        val supaCharger = Supabase.table(SupaTable.Chargers).update({
            set("type", charger.type.name)
            set("power", charger.power.name)
            set("price", charger.price)
            set("issue", charger.issue.name)
            set("status", charger.status.name)
        }) {
            filter {
                eq("id", charger.id)
            }
            select()
        }.decodeSingle<SupaCharger>()
        chargerDao.update(supaCharger.toEntity())
    }

}