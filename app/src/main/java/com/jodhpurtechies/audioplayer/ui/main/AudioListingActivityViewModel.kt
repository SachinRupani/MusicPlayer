@file:Suppress("DEPRECATION")

package com.jodhpurtechies.audioplayer.ui.main

import android.app.Application
import android.database.Cursor
import android.provider.MediaStore
import androidx.core.database.getStringOrNull
import androidx.lifecycle.*
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import com.jodhpurtechies.audioplayer.data.repo.AudioFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AudioListingActivityViewModel(
    private val app: Application,
    private val repository: AudioFileRepository
) : ViewModel() {

    private val arrayAudioFiles: LiveData<List<AudioFileModel>> = repository.allAudioFiles.asLiveData()

    fun getAllAudioFilesOnce():LiveData<List<AudioFileModel>>{
        return Transformations.distinctUntilChanged(arrayAudioFiles)
    }

    fun getAudioFiles() {
        viewModelScope.launch(Dispatchers.Default) {
            repository.deleteAll()
            delay(300)
            val cursor: Cursor? = getCursorForAudio()
            cursor?.apply {
                while (moveToNext()) {
                    val audioFileModel = AudioFileModel(
                        audioFileId = getStringOrNull(0),
                        audioFilePath = getStringOrNull(1),
                        audioFileArtist = getStringOrNull(2),
                        audioFileAlbum = getStringOrNull(3),
                        audioFileAlbumId = getStringOrNull(4),
                        audioFileName = getStringOrNull(5),
                        audioFileDuration = getStringOrNull(7)
                    )
                    //Log.e("FileAudio", audioFileModel.toString())
                    repository.insert(audioFileModel)
                }
                close()
            }

        }
    }

    private fun getCursorForAudio(): Cursor? {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.ArtistColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ALBUM_ID,
            MediaStore.Audio.AudioColumns.DISPLAY_NAME,
            MediaStore.Audio.AudioColumns.DATE_MODIFIED,
            "duration"
        )
        return app.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${MediaStore.Audio.AudioColumns.DATE_MODIFIED} DESC"
        )
    }
}

class AudioListingActivityViewModelFactory(
    private val app: Application,
    private val repository: AudioFileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioListingActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioListingActivityViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}