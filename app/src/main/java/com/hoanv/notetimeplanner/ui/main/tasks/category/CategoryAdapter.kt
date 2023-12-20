package com.hoanv.notetimeplanner.ui.main.tasks.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.databinding.ItemAddCategoryBinding
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class CategoryAdapter(
    val context: Context,
    val onOptionClick: (item: Category, view: View) -> Unit
) : ListAdapter<Category, CategoryAdapter.VH>(CategoryCallBack) {

    object CategoryCallBack : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemAddCategoryBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }

    inner class VH(private val binding: ItemAddCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: Category) {
            binding.run {
                tvTitleCat.text = item.title ?: "a"
                tvNumber.text = item.listTask.toString()
                ivOption.setOnSingleClickListener {
                    onOptionClick.invoke(item, it)
                }
            }
        }
    }
}