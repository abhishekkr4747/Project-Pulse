package com.example.projectpulse.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpulse.R
import com.example.projectpulse.adapters.LabelColorListItemsAdapter
import com.example.projectpulse.adapters.MembersListItemsAdapter
import com.example.projectpulse.models.User

abstract class MemberListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
): Dialog(context) {

   private var adapter: MembersListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list , null)

        setContentView(view)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(view)
    }

    private fun setUpRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tvTitle).text = title
        
        if(list.size > 0) {
            view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)

            adapter = MembersListItemsAdapter(context , list)
            view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter
            
            adapter!!.setOnClickListener(object :
                MembersListItemsAdapter.OnClickListener {
                override fun onClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user , action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User , action: String)

}