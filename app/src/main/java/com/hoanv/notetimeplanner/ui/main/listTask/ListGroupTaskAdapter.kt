package com.hoanv.notetimeplanner.ui.main.listTask

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(
        ItemTaskGroupBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position), position)
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

                    ivNext.setOnSingleClickListener {
                        onClick.invoke(this)
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
}