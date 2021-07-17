package com.jodhpurtechies.audioplayer.data.repo

import androidx.annotation.WorkerThread
import com.jodhpurtechies.audioplayer.data.dao.AudioFileDao
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import kotlinx.coroutines.flow.Flow

class AudioFileRepository(private val audioFileDao: AudioFileDao) {
    val allAudioFiles: Flow<List<AudioFileModel>> = audioFileDao.getAllAudioFiles()

    @WorkerThread
    fun getAudioFile(id: Int): Flow<AudioFileModel> {
        return audioFileDao.getAudioFile(id)
    }

    @WorkerThread
    suspend fun insert(audioFileModel: AudioFileModel) {
        audioFileDao.insert(audioFileModel)
    }

    @WorkerThread
    suspend fun deleteAll() {
        audioFileDao.deleteAll()
    }
}