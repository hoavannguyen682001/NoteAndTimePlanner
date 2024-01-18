package com.hoanv.notetimeplanner.ui.main.home.create.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.ItemMemberBinding
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.extension.gone
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener

class MemberAdapter(
    val context: Context,
    val onRemoveClick: (UserInfo) -> Unit
) : ListAdapter<UserInfo, MemberAdapter.VH>(MemberDiffUtil) {

    object MemberDiffUtil : DiffUtil.ItemCallback<UserInfo>() {
        override fun areItemsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: UserInfo, newItem: UserInfo): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(item: UserInfo) {
            binding.run {
                Glide.with(context)
                    .load(item.photoUrl ?: "")
                    .placeholder(R.drawable.img_user_avatar)
                    .error(R.drawable.img_user_avatar)
                    .into(ivImage)

                if (item.uid == Pref.userId) {
                    ivRemoveImg.gone()
                }

                ivRemoveImg.setOnSingleClickListener {
                    onRemoveClick.invoke(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ItemMemberBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.onBind(getItem(position))
    }
}