package cmu.group2.chargist.data.repository

import cmu.group2.chargist.data.database.AppDatabase
import cmu.group2.chargist.data.database.dao.ReviewDao
import cmu.group2.chargist.data.model.Review
import cmu.group2.chargist.data.supabase.SupaTable
import cmu.group2.chargist.data.supabase.Supabase
import cmu.group2.chargist.data.supabase.dto.SupaReview
import cmu.group2.chargist.data.supabase.dto.toEntity
import cmu.group2.chargist.data.supabase.table

object ReviewsRepository {
    private val reviewDao: ReviewDao by lazy { AppDatabase.getDatabase().reviewDao() }

    suspend fun addReview(review: Review, stationId: Long) {
        val request = SupaReview(
            stationId = stationId,
            userId = review.user.id,
            rating = review.rating,
            comment = review.comment,
            date = review.date.time
        )
        val supaReview = Supabase.table(SupaTable.Reviews).upsert(request) {
            onConflict = "userId,stationId"
            select()
        }.decodeSingle<SupaReview>()
        reviewDao.insert(supaReview.toEntity())
    }
}