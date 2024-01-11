package com.example.projectpulse.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.projectpulse.R
import com.example.projectpulse.databinding.ItemMembersBinding
import com.example.projectpulse.models.User
import com.example.projectpulse.utlis.Constants

open class MembersListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<User>
    ): RecyclerView.Adapter<ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemMembersBinding): RecyclerView.ViewHolder(binding.root) {
        val memberImage = binding.ivMemberImage
        val memberName = binding.tvMemberName
        val memberEmail = binding.tvMemberEmail
        val ivSelectedMember = binding.ivSelectedMember
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMembersBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is ViewHolder) {

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(holder.memberImage)

            holder.memberName.text = model.name
            holder.memberEmail.text = model.email

            if(model.selected) {
                holder.ivSelectedMember.visibility = View.VISIBLE
            } else {
                holder.ivSelectedMember.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    if(model.selected) {
                        onClickListener!!.onClick(position, model, Constants.UN_SELECT)
                    }else {
                        onClickListener!!.onClick(position, model, Constants.SELECT)
                    }
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }



    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int , user: User , action: String)
    }
}