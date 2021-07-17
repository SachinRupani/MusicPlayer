package com.jodhpurtechies.audioplayer.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "audio_songs_table")
data class AudioFileModel(
    @ColumnInfo(name = "audio_file_id") val audioFileId: String?,
    @ColumnInfo(name = "audio_file_name") val audioFileName: String?,
    @ColumnInfo(name = "audio_file_artist") val audioFileArtist: String?,
    @ColumnInfo(name = "audio_file_path") val audioFilePath: String?,
    @ColumnInfo(name = "audio_file_album") val audioFileAlbum: String?,
    @ColumnInfo(name = "audio_file_album_id") val audioFileAlbumId: String?,
    @ColumnInfo(name = "audio_file_duration") val audioFileDuration: String?
) : Serializable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "is_favorite")
    var audioFileIsFavorite: Boolean = false

    val getAudioFileDurationToDisplay: String
        get() {
            try {
                val audioDurationMillis = audioFileDuration?.toLongOrNull()
                audioDurationMillis?.apply {
                    val minutes = this / 1000 / 60
                    val seconds = this / 1000 % 60
                    return "${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
                }
                return ""
            } catch (e: Exception) {
                return ""
            }
        }
}
