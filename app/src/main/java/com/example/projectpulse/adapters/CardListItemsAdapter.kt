package com.example.projectpulse.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.projectpulse.R
import com.example.projectpulse.activities.TaskListActivity
import com.example.projectpulse.databinding.ItemCardBinding
import com.example.projectpulse.models.Card
import com.example.projectpulse.models.SelectedMembers

class CardListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<Card>
): RecyclerView.Adapter<ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemCardBinding): RecyclerView.ViewHolder(binding.root) {
        val tvCardName = binding.tvCardName
        val viewLabelColor = binding.viewLabelColor
        val rvCardSelectedMemberList = binding.rvCardSelectedMembersList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCardBinding.inflate(
            LayoutInflater.from(parent.context) ,
            parent , false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if(holder is ViewHolder) {

            if(model.labelColor.isNotEmpty()) {
                holder.viewLabelColor.visibility = View.VISIBLE
                holder.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.viewLabelColor.visibility = View.GONE
            }

            holder.tvCardName.text = model.name

            if((context as TaskListActivity).mAssignedMemberDetailList.size > 0) {

                val selectedMemberList: ArrayList<SelectedMembers> = ArrayList()

                for(i in context.mAssignedMemberDetailList.indices) {
                    for(j in model.assignedTo) {
                        if(context.mAssignedMemberDetailList[i].id == j) {

                            val selectedMember = SelectedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )
                            selectedMemberList.add(selectedMember)
                        }
                    }
                }
                if(selectedMemberList.size > 0) {
                    if(selectedMemberList.size == 1 && selectedMemberList[0].id == model.createdBy) {
                        holder.rvCardSelectedMemberList.visibility = View.GONE
                    } else {
                        holder.rvCardSelectedMemberList.visibility = View.VISIBLE

                        holder.rvCardSelectedMemberList.layoutManager = GridLayoutManager(context , 4)
                        val adapter = CardMemberListItemAdapter(context , selectedMemberList, false)
                        holder.rvCardSelectedMemberList.adapter = adapter

                        adapter.setOnClickListener(object : CardMemberListItemAdapter.OnClickListener{
                            override fun onClick() {
                                if(onClickListener != null) {
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })
                    }
                } else {
                    holder.rvCardSelectedMemberList.visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener!!.onClick(position)
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
        fun onClick(position: Int)
    }
}