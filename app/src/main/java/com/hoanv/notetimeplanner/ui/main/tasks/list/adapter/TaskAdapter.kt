package com.hoanv.notetimeplanner.ui.main.tasks.list.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.ItemTaskBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class TaskAdapter(
    val context: Context,
    val onClick: (Task) -> Unit,
    val onIconCheckClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(TaskDiffUtils) {

    object TaskDiffUtils : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position), position)
    }

    inner class VH(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(task: Task, position: Int) {
            binding.run {
                tvTitleTask.text = task.title
                tvEstDay.text = context.getString(R.string.date_display, task.startDay, task.endDay)
                tvTimeEnd.text = task.timeEnd
                root.setOnSingleClickListener {
                    onClick.invoke(task)
                }
                ivCheck.setOnSingleClickListener {
                    onIconCheckClick.invoke(task)
                }
            }
        }
    }
}