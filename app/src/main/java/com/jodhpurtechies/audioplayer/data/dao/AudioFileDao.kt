package com.jodhpurtechies.audioplayer.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioFileDao {

    @Query("SELECT * FROM audio_songs_table")
    fun getAllAudioFiles(): Flow<List<AudioFileModel>>

    @Query("SELECT * FROM audio_songs_table WHERE id=:id")
    fun getAudioFile(id: Int): Flow<AudioFileModel>

    @Query("SELECT * FROM audio_songs_table WHERE is_favorite=1")
    fun getAllFavoriteAudioFiles(): LiveData<List<AudioFileModel>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioFileModel: AudioFileModel)

    @Query("DELETE FROM audio_songs_table")
    suspend fun deleteAll()
}