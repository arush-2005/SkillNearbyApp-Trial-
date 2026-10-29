package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookingEntity::class,
        ChatMessageEntity::class,
        FavoriteProviderEntity::class,
        UserReviewEntity::class,
        ProviderSubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SkillNearbyDatabase : RoomDatabase() {
    abstract fun dao(): SkillNearbyDao

    companion object {
        @Volatile
        private var INSTANCE: SkillNearbyDatabase? = null

        fun getDatabase(context: Context): SkillNearbyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkillNearbyDatabase::class.java,
                    "skillnearby_marketplace.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
