package com.example.passwordmanager.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database([AccountPassword::class, Keys::class], version = 21)
abstract class RoomDatabaseObject: RoomDatabase() {
    abstract fun entityDao(): DaoInterface

    companion object{
        @Volatile
        private var INSTANCE: RoomDatabaseObject? = null

        fun getDatabase(context: Context): RoomDatabaseObject{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomDatabaseObject::class.java,
                    "password_manager_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE= instance
                instance
            }
        }
    }
}