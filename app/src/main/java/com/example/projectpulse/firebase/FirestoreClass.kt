package com.example.projectpulse.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projectpulse.activities.CardDetailsActivity
import com.example.projectpulse.activities.CreateBoardActivity
import com.example.projectpulse.activities.MainActivity
import com.example.projectpulse.activities.MembersActivity
import com.example.projectpulse.activities.MyProfile
import com.example.projectpulse.activities.SignInActivity
import com.example.projectpulse.activities.SignUpActivity
import com.example.projectpulse.activities.TaskListActivity
import com.example.projectpulse.models.Board
import com.example.projectpulse.models.User
import com.example.projectpulse.utlis.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity , userInfo: User) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId()).set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
            .addOnFailureListener {
                Log.e("SignUpActivity" , it.toString())
            }
    }

    fun createBoard(activity: CreateBoardActivity , board: Board) {
        mFireStore.collection(Constants.BOARDS)
            .document()
            .set(board , SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity , "Board Created Successfully" , Toast.LENGTH_LONG).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {
                execption->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName,
                    "Error while creating board",
                    execption
                )
            }
    }

    fun deleteBoard(activity: TaskListActivity , boardId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(boardId)
            .delete()
            .addOnSuccessListener {
                Log.i("delete" , "Board deleted successfully")
                Toast.makeText(activity , "Board deleted successfully." , Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                Log.e("Board deletion failed.", "${it.message}")
            }
    }

    fun getBoardsDetails(activity: TaskListActivity , documentId: String) {
        mFireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener {
                    document->
                val board = document.toObject(Board::class.java)!!
                board.documentId = documentId
                activity.boardDetails(board)

            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while creating Board" , e)
            }
    }

    fun getBoardsList(activity: MainActivity) {
        mFireStore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO , getCurrentUserId())
            .get()
            .addOnSuccessListener {
                document->
                val boardsList: ArrayList<Board> = ArrayList()
                for(i in document.documents) {
                    val board = i.toObject(Board::class.java)!!
                    board.documentId = i.id
                    boardsList.add(board)
                }

                activity.populateBoardsListToUI(boardsList)
            }.addOnFailureListener {e->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while creating Board" , e)
            }
    }

    fun addUpdateTaskList(activity: Activity , board: Board) {
        val taskListHashMap = HashMap<String , Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.e(activity.javaClass.simpleName , "TaskList updated successfully")

                if(activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if(activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }
            .addOnFailureListener {
                exception->

                if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if(activity is CardDetailsActivity)
                    activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName , "Error while updating TaskList" , exception)
            }
    }

    fun updateUserProfileData(activity: Activity ,
                              userHashMap: HashMap<String , Any>) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity ,
                    "Profile updated successfully" ,
                    Toast.LENGTH_LONG).show()
                when(activity) {
                    is MainActivity ->{
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfile ->{
                        activity.profileUpdateSuccess()
                    }
                }
            }
            .addOnFailureListener {
                when(activity) {
                    is MainActivity ->{
                        activity.hideProgressDialog()
                    }
                    is MyProfile ->{
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity ,
                    "Profile update Failure!" ,
                    Toast.LENGTH_LONG).show()

            }
    }

    fun loadUserData(activity: Activity , readBoardsList: Boolean = false) {
        mFireStore.collection(Constants.USERS)
            .document(getCurrentUserId())
            .get()
            .addOnSuccessListener {document->
                val loggedInUser = document.toObject(User::class.java)

                when(activity) {

                    is SignInActivity -> {
                        if(loggedInUser != null)
                            activity.signInSuccess(loggedInUser)
                    }

                    is MainActivity -> {
                        if (loggedInUser != null) {
                            activity.updateNavigationUserDetails(loggedInUser , readBoardsList)
                        }
                    }

                    is MyProfile -> {
                        if (loggedInUser != null) {
                            activity.setUserDataInUI(loggedInUser)
                        }
                    }
                }

            }
            .addOnFailureListener {

                when(activity) {

                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e("SignInActivity" , it.toString())
            }
    }

     fun getCurrentUserId(): String {
         var currentUser = FirebaseAuth.getInstance().currentUser
         var currentUserID = ""
         if (currentUser != null) {
             currentUserID = currentUser.uid
         }
         return currentUserID
    }

    fun getAssignedMembersListDetails(
        activity: Activity , assignedTo: ArrayList<String>
    ) {
        mFireStore.collection(Constants.USERS)
            .whereIn(Constants.ID , assignedTo)
            .get()
            .addOnSuccessListener {
                document->

                val userList: ArrayList<User> = ArrayList()

                for(i in document.documents) {
                    val user = i.toObject(User::class.java)!!

                    userList.add(user)
                }
                if(activity is MembersActivity)
                    activity.setupMembersList(userList)
                else if(activity is TaskListActivity)
                    activity.boardMemberDetailList(userList)
            }
            .addOnFailureListener { e->
                if(activity is MembersActivity)
                    activity.hideProgressDialog()
                else if(activity is TaskListActivity)
                    activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while creating a board.",
                    e
                )
            }
    }

    fun getMemberDetails(activity: MembersActivity , email: String) {
        mFireStore
            .collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL , email)
            .get()
            .addOnSuccessListener {
                document->

                if(document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }
            .addOnFailureListener {
                e->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }

    fun assignMembersToBoard(activity: MembersActivity , board: Board, user: User) {

        val assignMemberToHashmap = HashMap<String,Any>()
        assignMemberToHashmap[Constants.ASSIGNED_TO] = board.assignedTo

        mFireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignMemberToHashmap)
            .addOnSuccessListener {
                activity.membersAssignSuccess(user)
            }
            .addOnFailureListener {
                e->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details",
                    e
                )
            }
    }
}