package com.jodhpurtechies.audioplayer.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.jodhpurtechies.audioplayer.MyApplication
import com.jodhpurtechies.audioplayer.R
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import com.jodhpurtechies.audioplayer.databinding.ActivityAudioListingBinding
import com.jodhpurtechies.audioplayer.ui.audioPlayer.AudioPlayerActivity
import com.jodhpurtechies.audioplayer.utils.AndroidPermissions

class AudioListingActivity : AppCompatActivity() {

    private val tag = "AudioListing"

    private var binding: ActivityAudioListingBinding? = null
    private var audioAdapter: AudioListingAdapter? = null

    private lateinit var viewModel: AudioListingActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_listing)
        binding?.lifecycleOwner = this
        binding?.rvAudioFiles?.apply {
            setHasFixedSize(true)
        }

        init()
    }

    private fun init() {
        viewModel = ViewModelProvider(
            this,
            AudioListingActivityViewModelFactory(
                application as MyApplication,
                (application as MyApplication).repository
            )
        )[AudioListingActivityViewModel::class.java]

        setAudioFilesListingAdapter()

        attachObservers()

        askReadWritePermissions()
    }

    /**
     * First ask the user to provide file/audio read/write permissions
     */
    private fun askReadWritePermissions() {
        AndroidPermissions.askReadWritePermissions(
            activity = this,
            onPermissionsGranted = {
                Log.d(tag, "Permissions Granted")
                viewModel.getAudioFiles()
            },
            onPermissionError = { errMsg ->
                Log.e(tag, errMsg)
            })
    }

    /**
     * Function which contains all the livedata observers
     */
    private fun attachObservers() {
        viewModel.getAllAudioFilesOnce().observe(this, { arrayAudioFiles ->
            arrayAudioFiles?.apply {
                Log.e("Observed", "LiveDataChange - Audio Files Size: $size")

                audioAdapter?.setData(this)
            }
        })
    }

    /**
     * Function which sets the retrieved arrayList of audio files
     * into the recycler view
     */
    private fun setAudioFilesListingAdapter() {
        if (audioAdapter == null) {
            audioAdapter = AudioListingAdapter(this::onAudioFileClicked)
            binding?.rvAudioFiles?.adapter = audioAdapter
        }
    }

    /**
     * Function which is triggered when user clicks a particular audio file item
     */
    private fun onAudioFileClicked(audioFile: AudioFileModel) {
        //Navigate to Audio Player Activity
        Intent(applicationContext, AudioPlayerActivity::class.java).apply {
            putExtra("AudioFile", audioFile)
            startActivity(this)
        }

    }
}