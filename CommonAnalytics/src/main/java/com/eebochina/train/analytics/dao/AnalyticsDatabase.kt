package com.eebochina.train.analytics.dao

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.eebochina.train.analytics.entity.ApiAnalytics

@Database(entities = [ApiAnalytics::class], version = 1)
abstract class AnalyticsDatabase : RoomDatabase() {

    companion object {

        @Volatile
        var instance: AnalyticsDatabase? = null

        fun getInstance(application: Application): AnalyticsDatabase {
            if (instance == null) {
                synchronized(AnalyticsDatabase::class) {
                    if (instance == null) {
                        instance = Room.databaseBuilder(
                            application,
                            AnalyticsDatabase::class.java,
                            "analytics"
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return instance!!
        }

    }


    abstract fun analyticsDao(): AnalyticsDao?
}