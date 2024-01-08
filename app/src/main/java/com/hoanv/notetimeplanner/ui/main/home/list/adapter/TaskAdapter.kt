package com.hoanv.notetimeplanner.ui.main.home.list.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.ItemTaskBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                tvEstDay.text = context.getString(R.string.date_display, task.endDay)
                tvTimeEnd.text = task.timeEnd
                tvCategory.text = task.category.title

                Glide.with(context)
                    .load(task.category.icon.iconUrl)
                    .into(ivIcon)
                ivIcon.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(task.category.icon.iconColor))

                root.setOnSingleClickListener {
                    onClick.invoke(task)
                }

                ivIcon.setOnSingleClickListener {
                    onIconCheckClick.invoke(task)
                }
            }
            bindStateItem(task)
        }

        fun bindStateItem(task: Task) {
            binding.run {
                if (task.taskState) {
                    tvTitleTask.paintFlags = tvTitleTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvStateTask.text = context.getString(R.string.text_done)
                    tvStateTask.backgroundTintList = context.getColorStateList(R.color.light_green)
                } else {
                    binding.tvTitleTask.paintFlags =
                        tvTitleTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    if (expireDay(task)) {
                        tvStateTask.text = context.getString(R.string.text_expire)
                        tvStateTask.backgroundTintList =
                            context.getColorStateList(R.color.orange_crayola)
                    } else {
                        tvStateTask.text = context.getString(R.string.text_todo)
                        tvStateTask.backgroundTintList =
                            context.getColorStateList(R.color.tufts_blue)
                    }
                }
            }
        }
    }

    private fun expireDay(task: Task): Boolean {
        val endDay = task.endDay?.let { day ->
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(
                day
            )
        }
        val timeEnd = task.timeEnd?.let { time ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).parse(
                time
            )
        }
        /* Check if end day before today or if timeEnd before current time */
        return (Date() == endDay && Date().after(timeEnd)) || Date() > endDay
    }
}