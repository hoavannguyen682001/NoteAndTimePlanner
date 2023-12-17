package com.hoanv.notetimeplanner.ui.main.tasks.list.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.ItemTaskDoneBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class DoneTaskAdapter(
    val context: Context,
    val onClick: (Task) -> Unit,
    val onIconCheckClick: (Task) -> Unit
) : ListAdapter<Task, DoneTaskAdapter.VH>(TaskDiffUtils) {

    object TaskDiffUtils : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemTaskDoneBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class VH(private val binding: ItemTaskDoneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(task: Task) {
            binding.run {
                tvTitleTask.text = task.title
                tvTitleTask.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
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