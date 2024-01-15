package com.hoanv.notetimeplanner.ui.main.home.create.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.data.models.ImageInfo
import com.hoanv.notetimeplanner.databinding.ItemAttachImageBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class ImageAttachAdapter(
    val context: Context,
    val onItemClick: (ImageInfo) -> Unit,
    val onRemoveClick: (ImageInfo) -> Unit
) : ListAdapter<ImageInfo, ImageAttachAdapter.VH>(AttachFileDiffUtil) {

    object AttachFileDiffUtil : DiffUtil.ItemCallback<ImageInfo>() {
        override fun areItemsTheSame(oldItem: ImageInfo, newItem: ImageInfo): Boolean {
            return oldItem.idImage == newItem.idImage
        }

        override fun areContentsTheSame(oldItem: ImageInfo, newItem: ImageInfo): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(val binding: ItemAttachImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: ImageInfo) {
            binding.run {
                Glide.with(context)
                    .load(item.imageUrl)
                    .into(ivImage)

                root.setOnSingleClickListener {
                    onItemClick.invoke(item)
                }

                ivRemoveImg.setOnSingleClickListener {
                    onRemoveClick.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemAttachImageBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }
}