package com.jodhpurtechies.audioplayer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jodhpurtechies.audioplayer.data.dao.AudioFileDao
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel

@Database(entities = [AudioFileModel::class], version = 1, exportSchema = false)
abstract class AudioDatabase : RoomDatabase() {

    abstract fun audioFileDao(): AudioFileDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AudioDatabase? = null

        fun getDatabase(context: Context): AudioDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AudioDatabase::class.java,
                    "audio_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}