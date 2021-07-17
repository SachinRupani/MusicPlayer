@file:Suppress("DEPRECATION")

package com.jodhpurtechies.audioplayer.ui.audioPlayer

import android.app.Notification
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.jodhpurtechies.audioplayer.MyApplication
import com.jodhpurtechies.audioplayer.R
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import com.jodhpurtechies.audioplayer.databinding.ActivityAudioPlayerBinding
import com.jodhpurtechies.audioplayer.services.MyAudioService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerActivity : AppCompatActivity(), Player.EventListener {

    companion object {
        var activeNotification: Notification? = null

        const val NOTIFICATION_ID_ACTIVE = 101

        private var simpleExoplayer: SimpleExoPlayer? = null
    }

    private lateinit var viewModel: AudioPlayerActivityViewModel

    private var arrayAudioFiles: List<AudioFileModel>? = null
    private var audioFile: AudioFileModel? = null
    private var binding: ActivityAudioPlayerBinding? = null

    //Exo Player
    private var playbackPosition: Long = 0
    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, this.getString(R.string.app_name))
    }

    //Exo Player Notification Player Manager
    private var playerNotificationManager: PlayerNotificationManager? = null
    private val playerNotificationListener =
        object : PlayerNotificationManager.NotificationListener {
            override fun onNotificationCancelled(
                notificationId: Int,
                dismissedByUser: Boolean
            ) {
                Log.d(
                    "OnNotificationCancelled",
                    "Triggered"
                )

                releasePlayer()

            }

            override fun onNotificationPosted(
                notificationId: Int,
                notification: Notification,
                ongoing: Boolean
            ) {
                super.onNotificationPosted(
                    notificationId,
                    notification,
                    ongoing
                )
                notification.apply {

                    Log.d(
                        "NotificationPosted",
                        "Ongoing: $ongoing"
                    )

                    activeNotification = this

                    if (ongoing) {
                        startForegroundServicePlayer(notificationId)
                    } else {
                        stopForegroundServicePlayer(false)
                    }

                }
            }


        }
    private var mediaSession: MediaSessionCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        releasePlayer()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_audio_player)
        binding?.lifecycleOwner = this
        binding?.playerViewAudio?.controllerHideOnTouch=false
        init()
        attachObservers()
    }

    private fun init() {
        viewModel = ViewModelProvider(
            this,
            AudioPlayerActivityViewModelFactory(
                application as MyApplication,
                (application as MyApplication).repository
            )
        )[AudioPlayerActivityViewModel::class.java]


        audioFile = intent?.extras?.getSerializable("AudioFile") as AudioFileModel?
        binding?.model = audioFile

        Log.e(
            "AudioPlayer",
            "AudioFile: $audioFile - ${audioFile?.id} - ${audioFile?.audioFileIsFavorite}"
        )
        initializePlayer()

        /*CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            handleClicks()
        }*/

    }

    private fun attachObservers() {
        viewModel.arrayAudioFiles.observe(this, { arrayAudioFiles ->
            arrayAudioFiles?.let { arrayAudioFilesNotNull ->
                this.arrayAudioFiles = arrayAudioFilesNotNull
                onAudioFilesReceivedInitPreparePlayer()
            }
        })
    }

    private fun onAudioFilesReceivedInitPreparePlayer(playWhenReady: Boolean = true) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(400)
            if (simpleExoplayer == null || binding?.playerViewAudio?.player == null) {
                initializePlayer()
                simpleExoplayer?.playWhenReady = playWhenReady
            }
            this@AudioPlayerActivity.arrayAudioFiles?.let { arrayAudioFilesNotNull ->
                preparePlayer(arrayAudioFilesNotNull)
                val currentAudioSongPosition = arrayAudioFilesNotNull.indexOf(audioFile)
                if (currentAudioSongPosition >= 0) {
                    Log.d("CurPosition", "" + currentAudioSongPosition)
                    simpleExoplayer?.seekTo(currentAudioSongPosition, playbackPosition)
                }
            }

        }
    }

    /*private fun handleClicks() {
        binding?.playerViewAudio?.exo_prev?.setOnClickListener {
            //Prev Button Click
            playPreviousAudio()
        }
        binding?.playerViewAudio?.exo_next?.setOnClickListener {
            //Next Button Click
            playNextAudio()
        }
    }*/

    private fun initializePlayer() {
        if (simpleExoplayer == null) {
            /*simpleExoplayer = ExoPlayerFactory.newSimpleInstance(
                applicationContext,
                DefaultRenderersFactory(applicationContext),
                DefaultTrackSelector(),
                DefaultLoadControl()
            )*/
            simpleExoplayer = SimpleExoPlayer.Builder(this@AudioPlayerActivity).build()
        }

        binding?.playerViewAudio?.player = simpleExoplayer
        simpleExoplayer?.playWhenReady=true
        simpleExoplayer?.addListener(this@AudioPlayerActivity)
    }

    private fun setupParticularAudioFileToPlay() {
        audioFile?.apply {
            audioFilePath?.let { filePathNotNull ->
                val fileAudio = File(filePathNotNull)
                if (fileAudio.exists()) {
                    //preparePlayer(Uri.fromFile(fileAudio))
                    initializeAudioNotificationManager()
                }

            }
        }
    }

    /*private fun preparePlayer(uri: Uri) {
        val mediaSource = buildMediaSource(uri)
        simpleExoplayer?.prepare(mediaSource)
    }*/

    /*private fun buildMediaSource(uri: Uri): MediaSource {
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(uri)
    }*/

    private fun preparePlayer(allAudioFiles: List<AudioFileModel>) {
        val mediaSource = buildConcatenatingMediaSource(allAudioFiles)
        simpleExoplayer?.prepare(mediaSource)

    }

    private fun buildConcatenatingMediaSource(allAudioFiles: List<AudioFileModel>): MediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        allAudioFiles.forEach {
            val fileAudio = File(it.audioFilePath ?: "")
            if (fileAudio.exists()) {
                concatenatingMediaSource.addMediaSource(
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.fromFile(fileAudio))
                )
            }
        }
        return concatenatingMediaSource

    }

    private fun initializeAudioNotificationManager() {
        if (playerNotificationManager == null) {
            playerNotificationManager =
                PlayerNotificationManager.createWithNotificationChannel(
                    applicationContext,
                    getString(R.string.channel_id_audio_player),
                    R.string.channel_name,
                    R.string.channel_description,
                    NOTIFICATION_ID_ACTIVE,
                    object : PlayerNotificationManager.MediaDescriptionAdapter {
                        override fun getCurrentContentTitle(player: Player): CharSequence {
                            return audioFile?.audioFileName ?: ""
                        }

                        override fun createCurrentContentIntent(player: Player): PendingIntent? {
                            val intent = Intent(applicationContext, AudioPlayerActivity::class.java)
                            intent.putExtra("AudioFile", audioFile)
                            return PendingIntent.getActivity(
                                applicationContext, 0, intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        }

                        override fun getCurrentContentText(player: Player): CharSequence? {
                            return audioFile?.audioFileArtist
                        }

                        override fun getCurrentLargeIcon(
                            player: Player,
                            callback: PlayerNotificationManager.BitmapCallback
                        ): Bitmap? {
                            //Get album bitmap (for passing in notification player)
                            val artworkUri = Uri.parse("content://media/external/audio/albumart")
                            val albumArtUri = ContentUris.withAppendedId(
                                artworkUri,
                                audioFile?.audioFileAlbumId?.toLong() ?: 0L
                            )
                            Glide.with(application)
                                .asBitmap()
                                .load(albumArtUri)
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        //Palette API
                                        /*val paletteGenerated = Palette.Builder(resource).generate()
                                        val mainBitmapColor =
                                            paletteGenerated.getDominantColor(defaultColor)*/
                                        callback.onBitmap(resource)

                                    }
                                })
                            return null
                        }

                    },
                    playerNotificationListener
                )

            playerNotificationManager?.apply {
                setUseNavigationActions(true)

                setRewindIncrementMs(10000)
                setFastForwardIncrementMs(10000)
                setUseStopAction(false)
                setSmallIcon(R.drawable.ic_app_logo_notification_white)
                setColorized(true)

                mediaSession?.release()
                mediaSession = MediaSessionCompat(
                    application,
                    getString(R.string.channel_id_audio_player)
                )
                mediaSession?.let { mediaSessionNotNull ->
                    val connector = MediaSessionConnector(mediaSessionNotNull)
                    connector.setPlayer(simpleExoplayer)
                    playerNotificationManager?.setMediaSessionToken(mediaSessionNotNull.sessionToken)
                }


                setPlayer(simpleExoplayer)
            }
        }


    }

    fun startForegroundServicePlayer(notificationId: Int) {

        val intent = Intent(applicationContext, MyAudioService::class.java).apply {
            putExtra("NotificationId", notificationId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopForegroundServicePlayer(removeNotification: Boolean = true) {
        Log.d("StoppingForeground", "ForegroundService -> Stopped")
        val intent = Intent(applicationContext, MyAudioService::class.java).apply {
            putExtra(
                "NotificationId",
                if (removeNotification) 0 else 1
            )
        }

        startService(intent)
    }

    private fun releasePlayer() {
        simpleExoplayer?.release()
        removeNotificationPlayer()
        simpleExoplayer = null

        try {
            val isActivityVisible = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            Log.d("isActivityVisible", "" + isActivityVisible)
            if (isActivityVisible) {
                onAudioFilesReceivedInitPreparePlayer(playWhenReady = false)
            }
        } catch (e: Exception) {

        }

    }

    private fun removeNotificationPlayer() {
        playerNotificationManager?.setPlayer(null)
        stopForegroundServicePlayer()
        playerNotificationManager = null
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        // handle error
        Log.e("ExoException", "" + error)
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> {
                Log.d("ExoPlayerState", "BUFFERING")
            }
            Player.STATE_READY -> {
                Log.d("ExoPlayerState", "READY")
            }
            Player.STATE_IDLE -> {
                Log.d("ExoPlayerState", "IDLE")
            }
            Player.STATE_ENDED -> {
                Log.d("ExoPlayerState", "ENDED")
                playbackPosition = 0L
            }
        }
    }

    override fun onPositionDiscontinuity(reason: Int) {
        super.onPositionDiscontinuity(reason)
        Log.d("onPositionDiscontinuity", "reason: $reason")
        onAudioTrackChanged()
    }

    override fun onTracksChanged(
        trackGroups: TrackGroupArray,
        trackSelections: TrackSelectionArray
    ) {
        super.onTracksChanged(trackGroups, trackSelections)
        Log.d("onTracksChanged", "trackGroups: $trackGroups")
        //onAudioTrackChanged()
    }

    private fun onAudioTrackChanged() {
        val curAudioIndex = simpleExoplayer?.currentWindowIndex
        curAudioIndex?.apply {
            Log.d("onAudioTrackChanged", "Index: $this")
            if (this >= 0) {
                audioFile = arrayAudioFiles?.get(this)
                binding?.model = audioFile
                setupParticularAudioFileToPlay()
            }
        }
    }
}