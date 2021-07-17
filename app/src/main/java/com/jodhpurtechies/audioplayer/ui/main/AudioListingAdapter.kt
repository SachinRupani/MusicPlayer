package com.jodhpurtechies.audioplayer.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.jodhpurtechies.audioplayer.R
import com.jodhpurtechies.audioplayer.data.entities.AudioFileModel
import com.jodhpurtechies.audioplayer.databinding.RowItemAudioBinding

class AudioListingAdapter(private val itemClick: (course: AudioFileModel) -> Unit) :
    RecyclerView.Adapter<AudioListingAdapter.ViewHolder>() {

    /**
     * The audio files that our adapter will show
     */
    private var arrayAudioFiles: ArrayList<AudioFileModel> = ArrayList()

    fun setData(arrayUpdatedAudioFiles: List<AudioFileModel>) {
        val diffCallback = RatingDiffCallback(arrayAudioFiles, arrayUpdatedAudioFiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        arrayAudioFiles.clear()
        arrayAudioFiles.addAll(arrayUpdatedAudioFiles)
        diffResult.dispatchUpdatesTo(this)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.from(parent)

    override fun getItemCount(): Int = arrayAudioFiles.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(arrayAudioFiles[position], itemClick)
    }

    class ViewHolder private constructor(private val viewDataBinding: RowItemAudioBinding) :
        RecyclerView.ViewHolder(viewDataBinding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val dataBindingView: RowItemAudioBinding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.row_item_audio,
                    parent,
                    false
                )
                return ViewHolder(dataBindingView)
            }
        }

        fun bindData(model: AudioFileModel, itemClick: (model: AudioFileModel) -> Unit) {
            viewDataBinding.model = model
            viewDataBinding.itemLayout.setOnClickListener { itemClick(model) }
            viewDataBinding.executePendingBindings()
        }
    }

    class RatingDiffCallback(
        private val oldList: List<AudioFileModel>,
        private val newList: List<AudioFileModel>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].audioFileId === newList[newItemPosition].audioFileId
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            val (_, oldValue, oldName) = oldList[oldPosition]
            val (_, newValue, newName) = newList[newPosition]

            return oldName == newName && oldValue == newValue
        }

        @Nullable
        override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
            return super.getChangePayload(oldPosition, newPosition)
        }
    }
}