package com.jodhpurtechies.audioplayer.ui.main

import android.content.ContentUris
import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.jodhpurtechies.audioplayer.R
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel

@BindingAdapter("audioImage")
fun setAudioImage(img: ImageView, model: AudioFileModel?) {

    model?.apply {
        try {
            audioFileAlbumId?.let {
                val artworkUri = Uri.parse("content://media/external/audio/albumart")
                val albumArtUri = ContentUris.withAppendedId(artworkUri, it.toLong())
                Glide.with(img.context)
                    .load(albumArtUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_app_logo)
                    .error(R.drawable.ic_app_logo)
                    .into(img)
            }

        } catch (e: Exception) {
            Glide.with(img.context).load(R.drawable.ic_app_logo).into(img)
        }
    }
}

@BindingAdapter("audioSubtitle")
fun setAudioSubTitle(textView: AppCompatTextView, model: AudioFileModel?) {
    model?.apply {
        textView.text =
            if (audioFileArtist == null) getAudioFileDurationToDisplay else "$audioFileArtist | $getAudioFileDurationToDisplay"
    }
}