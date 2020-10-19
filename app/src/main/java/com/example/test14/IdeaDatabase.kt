package com.example.test14

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The local database
 */

@Database(entities = arrayOf(IdeaObject::class, CategoryObject::class), version = 1, exportSchema = false)
public abstract class IdeaDatabase : RoomDatabase() {

    abstract fun IdeaDao() : IdeaDao
    abstract fun CategoryDao() : CategoryDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: IdeaDatabase? = null

        fun getDatabase(context: Context): IdeaDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IdeaDatabase::class.java,
                    "Database"
                ).allowMainThreadQueries().build()
                INSTANCE = instance
                return instance
            }
        }
    }
}