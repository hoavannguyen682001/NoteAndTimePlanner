package com.hoanv.notetimeplanner.ui.main.home.category

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.Icon
import com.hoanv.notetimeplanner.databinding.ItemIconCategoryBinding
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.invisible
import com.hoanv.notetimeplanner.utils.extension.visible
import fxc.dev.common.extension.getHexColorFromResource

class IconCategoryAdapter(
    val context: Context,
    private val onClick: (Icon, Int) -> Unit
) : ListAdapter<Icon, IconCategoryAdapter.VH>(CategoryCallBack) {

    object CategoryCallBack : DiffUtil.ItemCallback<Icon>() {
        override fun areItemsTheSame(oldItem: Icon, newItem: Icon): Boolean {
            return oldItem.iconId == newItem.iconId
        }

        override fun areContentsTheSame(oldItem: Icon, newItem: Icon): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: Icon, newItem: Icon): Any {
            return oldItem.isSelected != newItem.isSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemIconCategoryBinding.inflate(LayoutInflater.from(context), parent, false))
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

    inner class VH(private val binding: ItemIconCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Icon, position: Int) {
            binding.run {

                Glide.with(context)
                    .load(item.iconUrl)
                    .into(ivIcon)

                val color = listColors[position]
                val hexColor = context.getHexColorFromResource(color)
                ivIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hexColor))
                item.iconColor = hexColor
                root.setOnClickListener {
                    onClick.invoke(item, position)
                }
            }
            bindStateItem(item.isSelected)
        }

        fun bindStateItem(isSelected: Boolean) {
            binding.cslItemIcon.isSelected = isSelected
            if (isSelected) {
                binding.viewCheck.visible()
            } else {
                binding.viewCheck.invisible()
            }
        }
    }

    private val listColors = mutableListOf(
        R.color.light_green,
        R.color.brilliant_lavender,
        R.color.maximum_blue_purple,
        R.color.maximum_green_yellow,
        R.color.orange_crayola,
        R.color.teal_200,
        R.color.ghost_gray,
        R.color.lavender_blue,
        R.color.picton_blue,
        R.color.unbleached_silk,
        R.color.tufts_blue,
        R.color.awesome,
        R.color.brown600,
        R.color.bittersweet,
        R.color.medium_purple,
    )
}