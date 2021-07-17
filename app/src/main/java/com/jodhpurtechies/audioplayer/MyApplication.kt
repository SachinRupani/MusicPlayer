package com.jodhpurtechies.audioplayer

import android.app.Application
import com.jodhpurtechies.audioplayer.data.AudioDatabase
import com.jodhpurtechies.audioplayer.data.repo.AudioFileRepository

class MyApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { AudioDatabase.getDatabase(this) }
    val repository by lazy { AudioFileRepository(database.audioFileDao()) }
}