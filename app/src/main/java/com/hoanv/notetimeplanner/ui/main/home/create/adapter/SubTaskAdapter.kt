package com.hoanv.notetimeplanner.ui.main.home.create.adapter

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.data.models.SubTask
import com.hoanv.notetimeplanner.databinding.ItemSubTaskBinding

class SubTaskAdapter(
    val context: Context,
    val onCheckedClick: (SubTask) -> Unit,
    val onDeleteClick: (SubTask) -> Unit
) : ListAdapter<SubTask, SubTaskAdapter.VH>(SubTaskDiffUtil) {

    object SubTaskDiffUtil : DiffUtil.ItemCallback<SubTask>() {
        override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
            return oldItem.taskId == newItem.taskId
        }

        override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: SubTask, newItem: SubTask): Any? {
            return oldItem.isDone != newItem.isDone
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemSubTaskBinding.inflate(LayoutInflater.from(context), parent, false))


    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else if (payloads[0] == true) {
            holder.bindStateItem(getItem(position))
        }
    }

    inner class VH(val binding: ItemSubTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: SubTask) {
            binding.run {
                tvSubTask.text = item.title
                ivCheck.setOnClickListener {
                    onCheckedClick.invoke(item)
                }
                ivRemoveTask.setOnClickListener {
                    onDeleteClick.invoke(item)
                }
            }
            bindStateItem(item)
        }

        fun bindStateItem(item: SubTask) {
            binding.run {
                if (item.isDone) {
                    tvSubTask.paintFlags = tvSubTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    ivCheck.isSelected = true
                } else {
                    tvSubTask.paintFlags = tvSubTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    ivCheck.isSelected = false
                }
            }
        }
    }
}