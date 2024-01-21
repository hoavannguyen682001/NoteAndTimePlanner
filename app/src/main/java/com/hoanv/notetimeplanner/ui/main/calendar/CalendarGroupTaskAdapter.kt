package com.hoanv.notetimeplanner.ui.main.calendar

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Task
import com.hoanv.notetimeplanner.databinding.ItemTaskGroupBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarGroupTaskAdapter(
    val context: Context,
    val onClick: (Task) -> Unit,
    val onMoreOptionClick: (Task, View) -> Unit
) : ListAdapter<Task, CalendarGroupTaskAdapter.VH>(DiffUtilGroupTask) {

    object DiffUtilGroupTask : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Task, newItem: Task): Any {
            return oldItem.taskState != newItem.taskState
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemTaskGroupBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position), position)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else if (payloads[0] == true) {
            holder.bindStateItem(getItem(position))
        }
    }

    inner class VH(private val binding: ItemTaskGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(task: Task, position: Int) {
            val alpha = 191
            binding.run {
                task.run{
                    tvTaskGroupName.text = title
                    tvDate.text = context.getString(
                        R.string.start_to_end_group_task,
                        regexDayMonth(startDay!!),
                        regexDayMonth(endDay!!)
                    )
                    tvTaskNumb.text = context.getString(R.string.number_of_group_tasks, subTask.size)
                    tvMember.text = context.getString(R.string.member_number, listMember.size)

                    Glide.with(context).load(category.icon.iconUrl).into(ivCategory)

                    ivCategory.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor(category.icon.iconColor))

                    cslTaskGroup.backgroundTintList = ColorStateList.valueOf(
                        ColorUtils.setAlphaComponent(
                            Color.parseColor(category.icon.iconColor), alpha
                        )
                    )
                    ivNext.setColorFilter(Color.parseColor(category.icon.iconColor), PorterDuff.Mode.SRC_IN)

                    root.setOnSingleClickListener {
                        onClick.invoke(this)
                    }

                    ivMore.setOnSingleClickListener {
                        onMoreOptionClick.invoke(this, ivMore)
                    }

                    bindStateItem(task)
                }
            }
        }

        fun bindStateItem(task: Task) {
            binding.run {
                if (task.taskState) {
                    tvTaskGroupName.paintFlags = tvTaskGroupName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    tvStateTask.text = context.getString(R.string.text_done)
                    tvStateTask.backgroundTintList = context.getColorStateList(R.color.light_green)
                } else {
                    binding.tvTaskGroupName.paintFlags =
                        tvTaskGroupName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
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

    private fun regexDayMonth(date: String): String {
        // Use regex to get string "dd-MM"
        val result = date.replace(Regex("(\\d{2}-\\d{2}).*"), "$1")
        return result
    }

    private fun expireDay(task: Task): Boolean {
        val expire = "${task.endDay} ${task.timeEnd}"
        val endDay = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).parse(
            expire
        )

        val end = Calendar.getInstance().apply { time = endDay!! }
        val now = Calendar.getInstance().apply { time = Date() }

        end.set(Calendar.SECOND, 59)
        now.set(Calendar.SECOND, 0)

        val endTime = end.timeInMillis
        val timeNow = now.timeInMillis
        Log.d("SimpleDateFormat", "$endTime - $timeNow")
        /* Check if end day before today */
        return timeNow > endTime
    }
}