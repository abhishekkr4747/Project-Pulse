package com.example.projectpulse.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projectpulse.R
import com.example.projectpulse.databinding.ActivityMyProfileBinding
import com.example.projectpulse.firebase.FirestoreClass
import com.example.projectpulse.models.User
import com.example.projectpulse.utlis.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfile : BaseActivity() {
    lateinit var binding: ActivityMyProfileBinding



    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: User
    private var mProfileURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        binding.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }

        FirestoreClass().loadUserData(this)

        binding.ivUserImage.setOnClickListener {

            if(ContextCompat.checkSelfPermission(
                    this ,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED){

                Constants.showImageChooser(this)

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES) ,
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
            }
        }

        binding.btnUpdate.setOnClickListener {
            if(mSelectedImageFileUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog()
                updateUserProfileData()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Constants.showImageChooser(this)

            } else {
                Toast.makeText(this , "Oops you denied the permission request. You can give it on settings",
                Toast.LENGTH_LONG).show()
            }
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK &&
            requestCode == Constants.PICK_IMAGE_REQUEST_CODE &&
            data!!.data != null) {

            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@MyProfile)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivUserImage)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarMyProfileActivity)
        if(supportActionBar != null)
        {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.toolbar_backbtn_white_24dp)
            supportActionBar?.title = resources.getString(R.string.my_profile)
        }
    }

     fun setUserDataInUI(user: User) {

        mUserDetails = user

        Glide
            .with(this@MyProfile)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        if(user.mobile != 0L) {
            binding.etMobile.setText(user.mobile.toString())
        }
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String , Any>()

        if(mProfileURL.isNotEmpty() && mProfileURL != mUserDetails.image) {
            userHashMap[Constants.IMAGE] = mProfileURL
        }

        if(binding.etName.text.toString() != mUserDetails.name) {
            userHashMap[Constants.NAME] = binding.etName.text.toString()
        }

        if(binding.etMobile.text.toString() != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = binding.etName.text.toString().toLong()
        }

        FirestoreClass().updateUserProfileData(this , userHashMap)
    }

    private fun uploadUserImage() {
        showProgressDialog()

        if(mSelectedImageFileUri != null) {

            val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE"
                        + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(this , mSelectedImageFileUri))

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot->
                Log.e("Firebase Image URL" ,
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri->
                    Log.i("Downloadable Image Uri" , uri.toString())
                    mProfileURL = uri.toString()

                    updateUserProfileData()
                }

            }.addOnFailureListener {
                expection->
                Toast.makeText(this@MyProfile , expection.message , Toast.LENGTH_LONG).show()
                hideProgressDialog()
            }
        }
    }



    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }
}