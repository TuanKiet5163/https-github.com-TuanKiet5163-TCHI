package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        FriendshipEntity::class,
        BudgetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TchiDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: TchiDatabase? = null

        fun getDatabase(context: Context): TchiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TchiDatabase::class.java,
                    "tchi_database.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
