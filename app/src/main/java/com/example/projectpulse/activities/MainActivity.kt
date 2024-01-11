package com.example.projectpulse.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectpulse.R
import com.example.projectpulse.adapters.BoardItemsAdapter
import com.example.projectpulse.databinding.ActivityMainBinding
import com.example.projectpulse.firebase.FirestoreClass
import com.example.projectpulse.models.Board
import com.example.projectpulse.models.User
import com.example.projectpulse.utlis.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : BaseActivity() , NavigationView.OnNavigationItemSelectedListener {
    private var binding: ActivityMainBinding? = null
    companion object {
        const val MY_PROFILE_REQUEST_CODE: Int = 1
        const val CREATE_BOARD_REQUEST_CODE: Int = 2
        const val DELETE_BOARD_REQUEST_CODE: Int = 3
    }

    private lateinit var mUserName: String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val toolbar:Toolbar = findViewById(R.id.toolbar_main_activity)

        setSupportActionBar(toolbar)
        if(supportActionBar != null) {
                toolbar.setNavigationIcon(R.drawable.ic_action_navigation_menu)
                toolbar.setNavigationOnClickListener {
                    toggleDrawer()
                }
        }

        val fabActionBar: FloatingActionButton = findViewById(R.id.fab_create_board)
        fabActionBar.setOnClickListener {
            val intent = Intent(this@MainActivity , CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME , mUserName)
            startActivityForResult(intent , CREATE_BOARD_REQUEST_CODE)
        }

        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.PROJEMANAG_PREFERENCES , Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED , false)

        if(tokenUpdated) {
            showProgressDialog()
            FirestoreClass().loadUserData(this , true)
        } else {
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener(this@MainActivity) {
                    updateFCMToken(it)
                }
        }
    }



    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {
        hideProgressDialog()

        val rvBoardList: RecyclerView = findViewById(R.id.rv_boards_list)
        val tvNoBoards: TextView = findViewById(R.id.tv_no_boards_available)
        if(boardsList.size > 0) {
            rvBoardList.visibility = View.VISIBLE
            tvNoBoards.visibility = View.GONE

            rvBoardList.layoutManager = LinearLayoutManager(this)
            rvBoardList.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this , boardsList)
            rvBoardList.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID , model.documentId)
                    startActivityForResult(intent , DELETE_BOARD_REQUEST_CODE)
                }
            })
        }else {
            rvBoardList.visibility = View.GONE
            tvNoBoards.visibility = View.VISIBLE
        }
    }

    private fun toggleDrawer() {
        if(binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if(binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    fun updateNavigationUserDetails(user: User , readBoardsList: Boolean) {
        hideProgressDialog()
        mUserName = user.name

        var navProfileImage: ImageView = findViewById(R.id.nav_profile_image)

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navProfileImage)

        var userName: TextView = findViewById(R.id.tv_username)

        userName.text = user.name

        if(readBoardsList) {
            showProgressDialog()
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreClass().loadUserData(this)

        } else if(resultCode == RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this)
            
        } else if (resultCode == RESULT_OK && requestCode == DELETE_BOARD_REQUEST_CODE) {
            FirestoreClass().getBoardsList(this)

        } else {
            Log.e("Cancelled" , "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this , MyProfile::class.java) , MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this , IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }

        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED , true)
        editor.apply()

        showProgressDialog()
        FirestoreClass().loadUserData(this , true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String , Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog()
        FirestoreClass().updateUserProfileData(this , userHashMap)
    }
}