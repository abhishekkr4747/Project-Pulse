package com.example.projectpulse.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectpulse.R
import com.example.projectpulse.adapters.TaskListItemsAdapter
import com.example.projectpulse.databinding.ActivityTaskListBinding
import com.example.projectpulse.firebase.FirestoreClass
import com.example.projectpulse.models.Board
import com.example.projectpulse.models.Card
import com.example.projectpulse.models.Task
import com.example.projectpulse.models.User
import com.example.projectpulse.utlis.Constants

class TaskListActivity : BaseActivity() {
    lateinit var binding: ActivityTaskListBinding
    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentId: String
    lateinit var mAssignedMemberDetailList: ArrayList<User>

    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if(intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID).toString()
        }

        showProgressDialog()
        FirestoreClass().getBoardsDetails(this , mBoardDocumentId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog()
            FirestoreClass().getBoardsDetails(this , mBoardDocumentId)
        }
        else {
            Log.e("Cancelled" , "Cancelled")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this , MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL , mBoardDetails)
                startActivityForResult(intent , MEMBERS_REQUEST_CODE)
                return true
            }
            R.id.action_delete_board -> {
                alertDialogForDeleteBoard(mBoardDetails.name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun alertDialogForDeleteBoard(boardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))

        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_board,
                boardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)


        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, which ->
            dialogInterface.dismiss()
            showProgressDialog()
            FirestoreClass().deleteBoard(this , mBoardDocumentId)
            setResult(Activity.RESULT_OK)
            finish()
        }

        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarTaskListActivity)
        if(supportActionBar != null)
        {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_backbtn_24dp)
            supportActionBar?.title = mBoardDetails.name
        }
        binding.toolbarTaskListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board

        hideProgressDialog()
        setupToolbar()

        showProgressDialog()
        FirestoreClass().getAssignedMembersListDetails(this , mBoardDetails.assignedTo)
    }

    fun boardMemberDetailList(list: ArrayList<User>) {
        mAssignedMemberDetailList = list

        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        mBoardDetails.taskList.add(addTaskList)

        binding.rvTaskList.layoutManager = LinearLayoutManager(this@TaskListActivity ,
            LinearLayoutManager.HORIZONTAL , false)
        binding.rvTaskList.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this , mBoardDetails.taskList)
        binding.rvTaskList.adapter = adapter
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog()
        FirestoreClass().getBoardsDetails(this , mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName , FirestoreClass().getCurrentUserId())
        mBoardDetails.taskList.add(0 , task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this , mBoardDetails)
    }

    fun updateTaskList(position: Int , listName: String , model: Task) {
        val task = Task(listName , model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this , mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)

        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this , mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreClass().getCurrentUserId())

        val card = Card(cardName , FirestoreClass().getCurrentUserId() , cardAssignedUsersList)

        val cardList = mBoardDetails.taskList[position].cards
        cardList.add(card)

        val task = Task(mBoardDetails.taskList[position].title,
            mBoardDetails.taskList[position].createdBy,
            cardList)

        mBoardDetails.taskList[position] = task

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this , mBoardDetails)
    }

    fun updateCardsInTaskList(taskListPosition: Int , cards: ArrayList<Card>) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        mBoardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog()
        FirestoreClass().addUpdateTaskList(this , mBoardDetails)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this , CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL , mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST , mAssignedMemberDetailList)
        startActivityForResult(intent , CARD_DETAILS_REQUEST_CODE)
    }
}