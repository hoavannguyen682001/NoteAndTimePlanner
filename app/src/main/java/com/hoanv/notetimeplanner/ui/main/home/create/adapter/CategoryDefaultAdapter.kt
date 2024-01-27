package com.hoanv.notetimeplanner.ui.main.home.create.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.databinding.ItemCategoryBinding
import com.hoanv.notetimeplanner.utils.extension.safeClickListener
import fxc.dev.common.extension.resourceColor

class CategoryDefaultAdapter(
    val context: Context,
    val onClick: (Category, Int) -> Unit,
) : ListAdapter<Category, CategoryDefaultAdapter.VH>(CategoryCallBack) {

    object CategoryCallBack : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Category, newItem: Category): Any {
            return oldItem.isSelected != newItem.isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemCategoryBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position), position)
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else if (payloads[0] == true) {
            holder.bindStateItem(getItem(position).isSelected)
        }
    }

    inner class VH(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Category, position: Int) {
            binding.run {
                tvCategoryTitle.text = item.title ?: ""
                Glide.with(context)
                    .load(item.icon.iconUrl)
                    .into(ivIcon)
                ivIcon.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(item.icon.iconColor))
                root.safeClickListener {
                    onClick.invoke(item, position)
                }
            }
            bindStateItem(item.isSelected)
        }

        fun bindStateItem(isSelected: Boolean) {
            binding.cslItemCategory.isSelected = isSelected
            if (isSelected) {
                binding.tvCategoryTitle.setTextColor(context.resourceColor(R.color.white))
            } else {
                binding.tvCategoryTitle.setTextColor(context.resourceColor(R.color.black))
            }
        }
    }
}