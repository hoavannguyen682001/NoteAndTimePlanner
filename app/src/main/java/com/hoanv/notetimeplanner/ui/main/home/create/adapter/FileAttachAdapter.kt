package com.hoanv.notetimeplanner.ui.main.home.create.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.data.models.FileInfo
import com.hoanv.notetimeplanner.databinding.ItemAttachFileBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class FileAttachAdapter(
    val context: Context,
    val onItemClick: (FileInfo) -> Unit,
    val onRemoveClick: (FileInfo) -> Unit
) : ListAdapter<FileInfo, FileAttachAdapter.VH>(AttachFileDiffUtil) {

    object AttachFileDiffUtil : DiffUtil.ItemCallback<FileInfo>() {
        override fun areItemsTheSame(oldItem: FileInfo, newItem: FileInfo): Boolean {
            return oldItem.idFile == newItem.idFile
        }

        override fun areContentsTheSame(oldItem: FileInfo, newItem: FileInfo): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(val binding: ItemAttachFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: FileInfo) {
            binding.run {
                tvFile.text = item.title
                ivRemoveFile.setOnSingleClickListener {
                    onRemoveClick.invoke(item)
                }
                root.setOnSingleClickListener {
                    onItemClick.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemAttachFileBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }
}