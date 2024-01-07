package com.hoanv.notetimeplanner.ui.main.home.category

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.databinding.ItemIconCategoryBinding

class IconCategoryAdapter(
    val context: Context,
    private val onClick: (String) -> Unit
) : ListAdapter<String, IconCategoryAdapter.VH>(CategoryCallBack) {

    object CategoryCallBack : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.toCharArray().contentEquals(newItem.toCharArray())
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemIconCategoryBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position), position)
    }

    inner class VH(private val binding: ItemIconCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: String, position: Int) {
            binding.run {
                Glide.with(context)
                    .load(item)
                    .into(ivIcon)
                val color = listColors[position]
                ivIcon.backgroundTintList = context.getColorStateList(color)
                root.setOnClickListener {
                    onClick.invoke(item)
                }
            }
        }
    }

    private val listColors = mutableListOf(
        R.color.aero_blue,
        R.color.brilliant_lavender,
        R.color.maximum_blue_purple,
        R.color.colorSecondPrimary,
        R.color.orange_crayola,
        R.color.yellow100,
        R.color.ghost_gray,
        R.color.lavender_blue,
        R.color.picton_blue,
        R.color.unbleached_silk,
        R.color.aero_blue,
        R.color.aero_blue,
        R.color.aero_blue,
        R.color.aero_blue,
        R.color.aero_blue,
    )
}