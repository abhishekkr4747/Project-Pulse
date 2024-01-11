package com.example.projectpulse.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.example.projectpulse.R
import com.example.projectpulse.databinding.ActivitySignUpBinding
import com.example.projectpulse.firebase.FirestoreClass
import com.example.projectpulse.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {
    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupToolbar()
        binding?.toolbarSignup?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.btnSignup?.setOnClickListener {
            registerUser()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding?.toolbarSignup)
        if(supportActionBar != null)
        {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this , "you have " +
                "successfully registered the email",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun registerUser() {
        val name = binding?.etName?.text.toString().trim{it <= ' '}
        val email = binding?.etEmailAddress?.text.toString().trim{it <= ' '}
        val password = binding?.etPassword?.text.toString().trim{it <= ' '}

        if(validateForm(name , email , password)) {
            showProgressDialog()
            FirebaseAuth.getInstance().
            createUserWithEmailAndPassword(email , password).addOnCompleteListener{
                task->
                if(task.isSuccessful) {

                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid , name, registeredEmail)
                    FirestoreClass().registerUser(this , user)

                } else {
                    hideProgressDialog()
                    Toast.makeText(this ,
                    task.exception!!.message , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateForm(name: String , email: String , password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar("Please enter a name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showErrorSnackBar("Please enter a email address")
                false
            }
            TextUtils.isEmpty(password) -> {
                showErrorSnackBar("Please enter a password")
                false
            }
            else -> {
                true
            }
        }
    }
}