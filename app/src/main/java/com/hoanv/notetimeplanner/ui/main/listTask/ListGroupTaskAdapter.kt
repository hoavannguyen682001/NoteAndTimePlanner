package com.hoanv.notetimeplanner.ui.main.listTask

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.ItemTaskGroupBinding

class ListGroupTaskAdapter(
    val context: Context,
    val onClick: (Task) -> Unit
) : ListAdapter<Task, ListGroupTaskAdapter.VH>(DiffUtilGroupTask) {

    object DiffUtilGroupTask : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

    }

    inner class VH(private val binding: ItemTaskGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(task: Task, position: Int) {
            binding.run {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemTaskGroupBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position), position)
    }
}