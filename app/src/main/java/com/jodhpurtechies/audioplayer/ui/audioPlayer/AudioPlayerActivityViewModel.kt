package com.jodhpurtechies.audioplayer.ui.audioPlayer

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import com.jodhpurtechies.audioplayer.data.repo.AudioFileRepository

class AudioPlayerActivityViewModel(
    private val app: Application,
    private val repository: AudioFileRepository
) : ViewModel() {

    val arrayAudioFiles: LiveData<List<AudioFileModel>> = repository.allAudioFiles.asLiveData()

    fun getAudioFile(id:Int):LiveData<AudioFileModel>{
        return repository.getAudioFile(id).asLiveData()
    }

}

class AudioPlayerActivityViewModelFactory(
    private val app: Application,
    private val repository: AudioFileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioPlayerActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AudioPlayerActivityViewModel(app, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}