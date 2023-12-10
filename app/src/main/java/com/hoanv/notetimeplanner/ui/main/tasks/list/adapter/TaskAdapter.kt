package com.hoanv.notetimeplanner.ui.main.tasks.list.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Todo
import com.hoanv.notetimeplanner.databinding.ItemTaskBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class TaskAdapter(
    val context: Context,
    val onClick: (Todo) -> Unit,
    val onIconCheckClick: (Todo) -> Unit
) : ListAdapter<Todo, TaskAdapter.VH>(TaskDiffUtils) {

    object TaskDiffUtils : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemTaskBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class VH(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(todo: Todo) {
            binding.run {
                tvTitleTask.text = todo.title
                tvEstDay.text = context.getString(R.string.date_display, todo.startDay, todo.endDay)
                root.setOnSingleClickListener {
                    onClick.invoke(todo)
                }
                ivCheck.setOnSingleClickListener {
                    onIconCheckClick.invoke(todo)
                }
            }
        }
    }
}