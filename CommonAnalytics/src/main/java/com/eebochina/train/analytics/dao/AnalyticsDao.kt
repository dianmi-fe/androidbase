package com.eebochina.train.analytics.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.eebochina.train.analytics.entity.ApiAnalytics

@Dao
interface AnalyticsDao {

    @Insert
    fun insert(analytics: ApiAnalytics)

    @Query("SELECT * FROM ApiAnalytics WHERE cn = :cn And ac  = :ac ")
    fun getAll(cn: String, ac: String): List<ApiAnalytics>?

    @get:Query("SELECT * FROM ApiAnalytics")
    val all: List<ApiAnalytics>?

    @get:Query("SELECT COUNT(*) FROM ApiAnalytics")
    val count: Int

    @Query("DELETE FROM ApiAnalytics WHERE id in (:ids)")
    fun deleteByIds(ids: List<Long>)

}